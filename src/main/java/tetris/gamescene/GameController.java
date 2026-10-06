package tetris.gamescene;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.block.data.BlockData;
import tetris.settings.GameSettings; // 저장 버튼에서 만들어진 실행 설정
import java.util.Objects; // 필수 설정값 검사


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final BlockQueue blockQueue;
	private final BlockHolding blockHolding;
	private final SceneRenderer renderer;
	private final GameSettings settings; // 현재 게임에서 사용하는 저장 설정
	private final PlayerInput playerInput; // 설정에서 저장한 키 배정 처리기

	
	
    private long previousFrameTime;

	public GameController(Stage stage, GameSettings settings){
		this.settings = Objects.requireNonNull(settings); // 게임 시작 시 설정 객체를 받아 보관
        previousFrameTime = 0;
		board = new GameBoard();
		score = new GameScore();
		blockQueue = new BlockQueue();
		blockHolding = new BlockHolding();

		renderer = new SceneRenderer(new RenderConfig(settings)); // 화면 크기와 색상 모드를 렌더러에 전달
		playerInput = new PlayerInput(settings); // 저장된 키 배정으로 입력 처리기를 생성
		GameStart(stage); // 메뉴에서 사용하던 창을 게임 화면으로 전환
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

	public void GameStart(Stage stage) {

		SceneRenderState state = new SceneRenderState(board.GetBoard(), score.GetGameScore(), blockQueue.GetBlockQueue(), blockHolding.GetBlockHolding());
		stage.setTitle("Tetris");
		Scene scene = renderer.CreateScene(state); // 현재 상태로 게임 화면 구성
		scene.setOnKeyPressed(event -> playerInput.HandleKeyCode(event.getCode())); // 저장된 키 배정으로 게임 입력을 전달
		stage.setScene(scene);
		stage.setWidth(settings.screenWidth()); // 저장된 창 너비 적용
		stage.setHeight(settings.screenHeight()); // 저장된 창 높이 적용
		stage.show();
		
		gameLoop.start();
	}
}
