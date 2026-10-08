package tetris;

import java.util.Objects;

import javafx.scene.Scene;
import javafx.stage.Stage;
import tetris.gamescene.GameController;
import tetris.scoreboard.ScoreBoardWindow;
import tetris.settings.SettingsScreen;

/**
 * Scene과 Stage를 가진 시작 화면 루트를 받아, 하나의 창에서 화면을 전환한다.
 */
public class AppController {
    private final Stage stage;
    private final Scene scene;
    private final StartScreen startScreen;
    private GameController gameController;

    public AppController(StartScreen root) {
        this.startScreen = Objects.requireNonNull(root);
        this.stage = Objects.requireNonNull(root.getStage());
        this.scene = Objects.requireNonNull(root.getScene());
    }

    public void showStartScreen() {
        scene.setOnKeyPressed(null);
        scene.setRoot(startScreen.createRoot(this::showGame, this::showScoreBoard, this::showSettings));
        scene.getRoot().requestFocus();
    }

    public void showGame() {
        gameController = new GameController(this::showPauseScreen);
        scene.setOnKeyPressed(null);
        scene.setRoot(gameController.GetRoot());
        gameController.GetRoot().requestFocus();
        gameController.GameStart();
    }

    public void showSettings() {
        scene.setOnKeyPressed(null);
        new SettingsScreen().show(stage, scene, this::showStartScreen);
    }

    public void showScoreBoard() {
        scene.setOnKeyPressed(null);
        new ScoreBoardWindow().show(stage, scene, this::showStartScreen);
    }

    public void showPauseScreen() {
        // GameController가 게임을 멈춘 뒤 호출할 화면 전환 콜백
        // TODO: 일시정지 화면이 만들어지면 해당 루트를 표시
    }

    public void ResumeGame() {
        if (gameController == null) {
            return;
        }

        scene.setOnKeyPressed(null);
        scene.setRoot(gameController.GetRoot());
        gameController.GetRoot().requestFocus();
        gameController.ResumeGame();
    }

    public void EndGame() {
        // TODO: 결과를 받는 파라미터 추가
        // TODO: 게임 정지, 점수 저장 요청, 시작 화면에 결과 전달
        // TODO: 시작 화면으로 전환한 뒤 게임 참조 정리
    }
}
