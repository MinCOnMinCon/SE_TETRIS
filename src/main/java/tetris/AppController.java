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
import tetris.settings.GameSettings;
import tetris.settings.SettingsStore;
import tetris.settings.GameSettings.GameMode;
import tetris.scoreboard.ScoreBoardScreen;
import tetris.settings.SettingsScreen;

/**
 * Scene과 Stage를 가진 시작 화면 루트를 받아, 하나의 창에서 화면을 전환한다.
 */
public class AppController {
    private final Stage stage;
    private final Scene scene;
    private ScoreBoardScreen scoreBoardScreen;
    private GameController gameController;
    private PauseScene pauseScene;
    private GameOverRoot gameOverRoot;

    public AppController(Stage stage, Scene scene) {
        this.stage = Objects.requireNonNull(stage, "stage cannot be null");
        this.scene = Objects.requireNonNull(scene, "scene cannot be null");
    }

    public void ShowStartScreen() {
        StartScreen startScreen = new StartScreen(stage, scene);
        startScreen.makeStartScreen(
            this::ShowGame, this::ShowItemGame, this::ShowScoreBoard, this::ShowSettings);
        scene.setOnKeyPressed(null);
        stage.setTitle("Tetris");
        scene.setRoot(startScreen.showStartScreen());
        scene.getRoot().requestFocus();
    }

    public void ShowGame() {
        startGame(GameMode.NORMAL);
    }

    public void ShowItemGame() {
        startGame(GameMode.ITEM);
    }

    private void startGame(GameMode gameMode) {
        GameSettings settings = SettingsStore.setGameMode(gameMode);
        gameController = new GameController(
                this::ShowPauseScreen, this::ShowEndGame, settings);
        // 기존 시작 화면이 Scene에 등록한 메뉴 키 입력을 제거한다.
        scene.setOnKeyPressed(null);
        scene.setRoot(gameController.GetRoot());
        gameController.GetRoot().requestFocus();
        gameController.GameStart();
    }

    public void ShowSettings() {
        SettingsScreen settingsScreen = new SettingsScreen();
        settingsScreen.makeSS(stage, scene, this::ShowStartScreen);
        scene.setOnKeyPressed(null);
        stage.setTitle("Tetris - 설정");
        scene.setRoot(settingsScreen.showSS());
        settingsScreen.activate();
    }

    public void ShowScoreBoard() {
        if (scoreBoardScreen == null) {
            scoreBoardScreen = new ScoreBoardScreen(this::ShowStartScreen);
        } else {
            scoreBoardScreen.refreshScores();
        }
        scene.setOnKeyPressed(null);
        stage.setTitle("Top 10 Scores");
        scene.setRoot(scoreBoardScreen.showSB());
        scoreBoardScreen.focusBackButton();
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
        gameOverRoot = new GameOverRoot(result);
        scene.setRoot(gameOverRoot);
        gameOverRoot.requestFocus();
        gameController = null;
        pauseScene = null;
    }
}
