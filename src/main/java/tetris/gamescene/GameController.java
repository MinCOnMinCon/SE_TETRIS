package tetris.gamescene;

import java.util.Objects;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.block.data.CurrentBlock;
import tetris.block.move.AutoMove;
import tetris.settingData.InputSettingData;
import tetris.settings.GameSettings;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final BlockQueue blockQueue;
	private final BlockHolding blockHolding;
	private final CurrentBlock currentBlock;

	private final AutoMove autoMove;
	private final PlayerInput playerInput;

	private final SceneRenderer renderer;
	private final Parent gameRoot;

	private final GameSettings gameSettings;

	
	
    private long previousFrameTime;
    private boolean paused;
    private final Runnable onPauseRequested;

    public GameController(Runnable onPauseRequested, GameSettings settings){
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested);

		gameSettings = settings;
        previousFrameTime = 0;
		board = new GameBoard();
		score = new GameScore();
		blockHolding = new BlockHolding();
		blockQueue = new BlockQueue();
		
		currentBlock = new CurrentBlock(blockQueue.GetNextBlock(), blockQueue, blockHolding);
		autoMove = new AutoMove(currentBlock, board.GetBoard(), gameSettings.difficulty());

		renderer = new SceneRenderer(new RenderConfig(blockQueue.GetMaxQueueSize()));
		// renderconfig에 블럭 큐 최대 사이즈 필요해 이렇게 전달함.
		playerInput = new PlayerInput(settings.keyBindings(), currentBlock, board, score, this::PauseGame);
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
            autoMove.TimeUpdate(deltaTime, score);


			SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueue.GetBlocksInQueueOrder(), blockHolding.GetBlockHolding());
			renderer.UpdateRoot(state);
		}
	};

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
		playerInput.SetInputEnabled(true);
		gameLoop.start();
	}

	public void GameStart() {
		previousFrameTime = 0;
		paused = false;
		playerInput.SetInputEnabled(true);
		gameLoop.start();
	}

	public Parent GetRoot() {
		return gameRoot;
	}

	
}
