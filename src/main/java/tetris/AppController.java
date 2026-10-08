package tetris;

import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tetris.gamescene.GameController;
import tetris.pausescene.PauseScene;
import tetris.settings.SettingsStore;

/**
 * 화면 전환과 앱의 전체 흐름을 조율한다.
 * 게임 화면 전환을 처리하며, 나머지 화면과의 연결은 이후 구현한다.
 */
public class AppController {
    private final Stage stage;
    private final Scene scene;
    private StartScreen startScreen;
    private GameController gameController;
    private PauseScene pauseScene;

    public AppController(Stage stage, Scene scene, StartScreen startScreen) {
        this.stage = Objects.requireNonNull(stage);
        this.scene = Objects.requireNonNull(scene);
        this.startScreen = Objects.requireNonNull(startScreen);
    }

    public void ShowStartScreen() {
        // TODO: StartScreen이 루트를 제공하도록 변경한 뒤 scene.setRoot(...)로 표시
    }

    public void StartGame() {
        // TODO: 게임 종료 콜백 연결
        gameController = new GameController(this::ShowPauseScreen, SettingsStore.getGameSettings());
        // 기존 시작 화면이 Scene에 등록한 메뉴 키 입력을 제거한다.
        scene.setOnKeyPressed(null);
        scene.setRoot(gameController.GetRoot());
        gameController.GetRoot().requestFocus(); // 해당 루트에 등록된 핸들러로 키 입력이 가도록 조정함.
        gameController.GameStart();
    }

    public void ShowSettingsScreen() {
        // TODO: 설정 화면의 루트를 표시
    }

    public void ShowScoreBoard() {
        // TODO: 스코어 보드 화면의 루트를 표시
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
