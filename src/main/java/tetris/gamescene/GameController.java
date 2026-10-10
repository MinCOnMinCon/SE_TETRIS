package tetris.gamescene;

import java.util.Objects;
import java.util.Arrays;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.block.data.CurrentBlock;
import tetris.block.move.AutoMove;

import tetris.settings.GameSettings;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final GameProgress progress;
	
	private final CurrentBlock currentBlock;

	private final AutoMove autoMove;
	private final PlayerInput playerInput;

	private final SceneRenderer renderer;
	private final Parent gameRoot;

	private final GameSettings gameSettings;

	
	
    private long previousFrameTime;
    private boolean paused;
    private final int lineClearEffectDuration = 1000; // 밀리초
    private int lineClearEffectTime = 0;
    private boolean lineClearEffectRunning = false;
    private final Runnable onPauseRequested;

    public GameController(Runnable onPauseRequested, GameSettings settings){
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested);

		gameSettings = settings;
        previousFrameTime = 0;
		board = new GameBoard();
		progress = new GameProgress();
		score = new GameScore(progress);
		
		BlockQueue blockQueue = new BlockQueue(progress);
		BlockHolding blockHolding = new BlockHolding();
		
		currentBlock = new CurrentBlock(blockQueue, blockHolding);

		autoMove = new AutoMove(currentBlock, board.GetBoard(), gameSettings.difficulty(), progress, board.GetClearedRows()); // TODO: 난이도 받아오기. 현재는 임시 함수로 받음


		
		renderer = new SceneRenderer(new RenderConfig(blockQueue.GetMaxQueueSize()));
		// renderconfig에 블럭 큐 최대 사이즈 필요해 이렇게 전달함.
		playerInput = new PlayerInput(gameSettings.keyBindings(), currentBlock, board, score, this::PauseGame);
        SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueue.GetBlocksInQueueOrder(), blockHolding.GetBlockHolding());
        gameRoot = renderer.CreateRoot(state);
        gameRoot.setOnKeyPressed(event -> playerInput.HandleKeyCode(event.getCode()));
        playerInput.SetInputEnabled(false);
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            if (previousFrameTime == 0) { // 첫 프레임에는 업데이트 동작을 하지 않고 이전 프레임 타임만 업데이트 함. 그 다음부터 이전 프레임 타임을 사용해 델타 타임을 계산해 사용
                previousFrameTime = now;
                return;
            }

            int deltaTime = (int) ((now - previousFrameTime) / 1_000_000);
            previousFrameTime = now;
            int[] clearedRows = board.GetClearedRows();

            if (clearedRows[0] != -1 || lineClearEffectRunning) {
                if (!lineClearEffectRunning) {
                    lineClearEffectRunning = true;
                    lineClearEffectTime = 0;
                    playerInput.SetInputEnabled(false);
                }

                renderer.DrawLineClearEffect(clearedRows, lineClearEffectTime);
                lineClearEffectTime += deltaTime;

                if (lineClearEffectTime >= lineClearEffectDuration) {
                    Arrays.fill(clearedRows, -1);
                    lineClearEffectRunning = false;
                    playerInput.SetInputEnabled(true);
                }
            } else {
                autoMove.TimeUpdate(deltaTime, score);

                SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(),
                    currentBlock.GetBlockQueue().GetBlocksInQueueOrder(), currentBlock.GetBlockHolding().GetBlockHolding());
                renderer.UpdateRoot(state);
            }
		}
	};

    // 종료 판정과 종료 화면 전환은 호출하는 쪽에서 처리한다.
    public GameResult StopGame() {
        gameLoop.stop();
        playerInput.SetInputEnabled(false);
        gameRoot.setOnKeyPressed(null);
        // TODO: 일반·아이템 모드 구현 후 종료 결과에 게임 모드도 전달한다.
        return new GameResult(score.GetGameScore(), gameSettings.difficulty());
    }

	public void PauseGame() {
		if (paused) {
			return;
		}

		paused = true;
		playerInput.SetInputEnabled(false);
		gameLoop.stop();
		onPauseRequested.run();
	}

	public void ResumeGame() {
		if (!paused) {
			return;
		}

		previousFrameTime = 0;
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

	public void GameStart() {
		previousFrameTime = 0;
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

	public Parent GetRoot() {
		return gameRoot;
	}

	
}
