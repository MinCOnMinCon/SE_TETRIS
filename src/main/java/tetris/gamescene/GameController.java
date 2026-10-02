package tetris.gamescene;

import javafx.animation.AnimationTimer;
import javafx.stage.Stage;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.block.data.BlockData;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final BlockQueue blockQueue;
	private final BlockHolding blockHolding;
	private final SceneRenderer renderer;

	
	
    private long previousFrameTime;

    public GameController(){
        previousFrameTime = 0;
		board = new GameBoard();
		score = new GameScore();
		blockQueue = new BlockQueue();
		blockHolding = new BlockHolding();

		renderer = new SceneRenderer();
        GameStart();
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            double deltaTime = (now - previousFrameTime) / 1_000_000_000.0;
            // 매프레임마다 호출할 함수 작성


			SceneRenderState state = new SceneRenderState(board.GetBoard(), score.GetGameScore(), blockQueue.GetBlockQueue(), blockHolding.GetBlockHolding());
			renderer.UpdateScene(state);
		}
	};

	public void GameStart() {

		SceneRenderState state = new SceneRenderState(board.GetBoard(), score.GetGameScore(), blockQueue.GetBlockQueue(), blockHolding.GetBlockHolding());
		Stage stage = new Stage();
		stage.setTitle("Tetris");
		stage.setScene(renderer.CreateScene(state));
		stage.show();
		
		gameLoop.start();
	}
}
