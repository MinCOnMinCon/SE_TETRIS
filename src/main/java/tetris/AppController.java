package tetris;

import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import tetris.gameoverscene.GameOverRoot;
import tetris.gamescene.GameController;
import tetris.gamescene.GameResult;
import tetris.pausescene.PauseScene;
import tetris.settings.SettingsStore;
import tetris.scoreboard.ScoreBoardWindow;
import tetris.scoreboard.ScoreStorage;
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
    private GameOverRoot gameOverRoot;

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
        gameController = new GameController(
                this::ShowPauseScreen, this::ShowEndGame, SettingsStore.getGameSettings());
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
        Parent pauseSceneRoot = pauseScene.GetRoot();
        scene.setRoot(pauseSceneRoot);
        pauseSceneRoot.requestFocus();
    }

    public void ResumeGame() {
        if (gameController == null) {
            return;
        }

        scene.setOnKeyPressed(null);
        Parent gameSceneRoot = gameController.GetRoot();
        scene.setRoot(gameSceneRoot);
        gameSceneRoot.requestFocus();
        gameController.ResumeGame();
    }

    public void ShowEndGame(GameResult result) {
        scene.setOnKeyPressed(null);
        gameOverRoot = new GameOverRoot(result, new ScoreStorage()); // TODO:스코어 스토리지는 임시로 생성. 나중에 결정되면 실제 스코어 스토리지를 제공할 것
        scene.setRoot(gameOverRoot);
        gameOverRoot.requestFocus();
        gameController = null;
        pauseScene = null;
    }
}
