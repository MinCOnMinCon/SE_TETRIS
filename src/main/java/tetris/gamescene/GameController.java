package tetris.gamescene;

import javafx.animation.AnimationTimer;
import javafx.stage.Stage;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueueController;
import tetris.block.data.CurrentBlock;
import tetris.settingData.InputSettingData;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final BlockQueueController blockQueueController;
	private final BlockHolding blockHolding;
	private final CurrentBlock currentBlock;
	private final SceneRenderer renderer;
	private final PlayerInput playerInput;

	
	
    private long previousFrameTime;

    public GameController(){
        previousFrameTime = 0;
		board = new GameBoard();
		score = new GameScore();
		blockHolding = new BlockHolding();
		blockQueueController = new BlockQueueController();
		
		currentBlock = new CurrentBlock(blockQueueController.GetNextBlock(), blockQueueController, blockHolding);

		renderer = new SceneRenderer();
		playerInput = new PlayerInput(new InputSettingData());
        GameStart();
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            double deltaTime = (now - previousFrameTime) / 1_000_000_000.0;
            // 매프레임마다 호출할 함수 작성


			SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueueController.GetBlockQueue().GetBlockQueue(), blockHolding.GetBlockHolding());
			// TODO: 저거 GetBlockQueue 함수명 고쳐야 할듯?
			renderer.UpdateScene(state);
		}
	};

	public void GameStart() {

		SceneRenderState state = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(), blockQueueController.GetBlockQueue().GetBlockQueue(), blockHolding.GetBlockHolding());
		Stage stage = new Stage();
		stage.setTitle("Tetris");
		stage.setScene(renderer.CreateScene(state));
		stage.show();
		
		gameLoop.start();
	}
}
