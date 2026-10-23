package tetris.gamescene;

import java.util.Objects;
import java.util.Arrays;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.gamescene.scenerender.SceneRenderer;
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
    private boolean stopped;
    private final int lineClearEffectDuration = 1000; // 밀리초
    private int lineClearEffectTime = 0;
    private boolean lineClearEffectRunning = false;

    private final Runnable onPauseRequested;
    private final Consumer<GameResult> onGameEnded;

    

    // 화면 테스트에서 누적 삭제 줄 수를 지정한다.
    public GameController(Runnable onPauseRequested, GameSettings settings, int initialClearedLineCount){
        this(onPauseRequested, result -> {}, settings, initialClearedLineCount);
    }

    // 앱 컨트롤러에서 사용하는 생성자 함수
    public GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings){
        this(onPauseRequested, onGameEnded, settings, 0);
    }

    public GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings, int initialClearedLineCount){
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested);
        this.onGameEnded = Objects.requireNonNull(onGameEnded);

		gameSettings = settings;
        previousFrameTime = 0;
		board = new GameBoard();
		progress = new GameProgress();
        progress.OnLinesCleared(initialClearedLineCount);
		score = new GameScore(progress);
		
		BlockQueue blockQueue = new BlockQueue(settings.colorMode(), progress, settings.difficulty(), settings.gameMode());
		BlockHolding blockHolding = new BlockHolding();
		
		currentBlock = new CurrentBlock(blockQueue, blockHolding, this::StopGame);

		autoMove = new AutoMove(currentBlock, board.GetBoard(), gameSettings.difficulty(), progress, board.GetClearedRows()); 


		
		RenderConfig renderConfig = new RenderConfig(gameSettings, blockQueue.GetMaxQueueSize());
		renderer = new SceneRenderer(renderConfig);
		// renderconfig에 블럭 큐 최대 사이즈 필요해 이렇게 전달함.
		playerInput = new PlayerInput(gameSettings.keyBindings(), currentBlock, board, score, progress, this::PauseGame);
        SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueue.GetBlocksInQueueOrder(), blockHolding.GetBlockHolding());
        gameRoot = renderer.CreateRoot(state);
        gameRoot.setOnKeyPressed(event -> playerInput.HandleKeyCode(event.getCode()));
        playerInput.SetInputEnabled(false);
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            if (stopped || paused) {
                return;
            }
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
                if (stopped) { //autoMove로 게임이 종료될 수 있으므로 검증
                    return;
                }

                SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(),
                    currentBlock.GetBlockQueue().GetBlocksInQueueOrder(), currentBlock.GetBlockHolding().GetBlockHolding());
                renderer.UpdateRoot(state);
            }
		}
	};

    // 종료 결과를 확보하고 게임을 정지한 뒤 화면 전환을 요청한다.
    public void StopGame() {
        if (stopped) {
            return;
        }
        stopped = true;
        GameResult result = new GameResult(
            score.GetGameScore(), gameSettings.difficulty(), gameSettings.gameMode());

        gameLoop.stop();
        playerInput.SetInputEnabled(false);
        gameRoot.setOnKeyPressed(null);
        onGameEnded.accept(result);
    }

	public void PauseGame() {
		if (stopped || paused) {
			return;
		}

		paused = true;
		playerInput.SetInputEnabled(false);
		gameLoop.stop();
		onPauseRequested.run();
	}

	public void ResumeGame() {
		if (stopped || !paused) {
			return;
		}

		previousFrameTime = 0;
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

	public void GameStart() {
        if (stopped) {
            return;
        }
		previousFrameTime = 0;
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

	public Parent GetRoot() {
		return gameRoot;
	}

	
}
