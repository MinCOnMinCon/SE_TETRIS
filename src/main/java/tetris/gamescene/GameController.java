package tetris.gamescene;

import java.util.Objects;

import javafx.animation.AnimationTimer;
import javafx.stage.Stage;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueueController;
import tetris.block.data.CurrentBlock;
import tetris.block.move.AutoMove;
import tetris.settingData.InputSettingData;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final BlockQueueController blockQueueController;
	private final BlockHolding blockHolding;
	private final CurrentBlock currentBlock;
	private final AutoMove autoMove;
	private final SceneRenderer renderer;
	private final PlayerInput playerInput;

	
	
    private long previousFrameTime;
    private boolean paused;
    private final Runnable onPauseRequested;

    public GameController(Runnable onPauseRequested){
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested);
        previousFrameTime = 0;
		board = new GameBoard();
		score = new GameScore();
		blockHolding = new BlockHolding();
		blockQueueController = new BlockQueueController();
		
		currentBlock = new CurrentBlock(blockQueueController.GetNextBlock(), blockQueueController, blockHolding);
		autoMove = new AutoMove(currentBlock, board.GetBoard());

		renderer = new SceneRenderer(new RenderConfig(blockQueueController.GetBlockQueue().GetMaxQueueSize()));// TODO: 저거 GetBlockQueue 함수명 고쳐야 할듯?
		// renderconfig에 블럭 큐 최대 사이즈 필요해 이렇게 전달함.
		playerInput = new PlayerInput(new InputSettingData(), currentBlock, board, this::PauseGame);
		//TODO: 이거 SettingData 확정되면 그때 수정할 것.
        GameStart();
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            if (previousFrameTime == 0) { // 첫 프레임에는 업데이트 동작을 하지 않고 이전 프레임 타임만 업데이트 함. 그 다음부터 이전 프레임 타임을 사용해 델타 타임을 계산해 사용
                previousFrameTime = now;
                return;
            }

            double deltaTime = (now - previousFrameTime) / 1_000_000_000.0;
            previousFrameTime = now;
            autoMove.Update(deltaTime);


			SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueueController.GetBlockQueue().GetBlockQueue(), blockHolding.GetBlockHolding());
			// TODO: 저거 GetBlockQueue 함수명 고쳐야 할듯?
			renderer.UpdateScene(state);
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

		SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueueController.GetBlockQueue().GetBlockQueue(), blockHolding.GetBlockHolding());
		Stage stage = new Stage();
		stage.setTitle("Tetris");
		stage.setScene(renderer.CreateScene(state));
		renderer.GetGameScene().setOnKeyPressed(event -> playerInput.HandleKeyCode(event.getCode()));//TODO: appcontroller로 옮겨야 한다.
		stage.show();
		
		previousFrameTime = 0;
		gameLoop.start();
	}
}
