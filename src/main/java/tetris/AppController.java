package tetris;

import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tetris.gamescene.GameController;
import tetris.pausescene.PauseScene;
import tetris.settings.SettingsStore;
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
    private PauseScene pauseScene;

    public AppController(StartScreen root) {
        this.startScreen = Objects.requireNonNull(root);
        this.stage = Objects.requireNonNull(root.getStage());
        this.scene = Objects.requireNonNull(root.getScene());
    }

    public void ShowStartScreen() {
        scene.setOnKeyPressed(null);
        scene.setRoot(startScreen.createRoot(this::ShowGame, this::ShowScoreBoard, this::ShowSettings));
        scene.getRoot().requestFocus();
    }

    public void ShowGame() {
        // TODO: 게임 종료 콜백 연결
        gameController = new GameController(this::ShowPauseScreen, SettingsStore.getGameSettings());
        // 기존 시작 화면이 Scene에 등록한 메뉴 키 입력을 제거한다.
        scene.setOnKeyPressed(null);
        scene.setRoot(gameController.GetRoot());
        gameController.GetRoot().requestFocus();
        gameController.GameStart();
    }

    public void ShowSettings() {
        scene.setOnKeyPressed(null);
        new SettingsScreen().show(stage, scene, this::ShowStartScreen);
    }

    public void ShowScoreBoard() {
        scene.setOnKeyPressed(null);
        new ScoreBoardWindow().show(stage, scene, this::ShowStartScreen);
    }

    public void ShowPauseScreen() {
        
        if (pauseScene == null) {
            pauseScene = new PauseScene(this::ResumeGame, Platform::exit);
        }

        scene.setOnKeyPressed(null);
        scene.setRoot(pauseScene.GetRoot());
        pauseScene.GetRoot().requestFocus();
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
