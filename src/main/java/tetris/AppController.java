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
import tetris.scoreboard.ScoreBoardScreen;
import tetris.settings.SettingsScreen;

/**
 * Scene과 Stage를 가진 시작 화면 루트를 받아, 하나의 창에서 화면을 전환한다.
 */
public class AppController {
    private final Stage stage;
    private final Scene scene;
    private final StartScreen startScreen;
    private final ScoreBoardScreen scoreBoardScreen;
    private final SettingsScreen settingsScreen;
    private GameController gameController;
    private PauseScene pauseScene;
    private GameOverRoot gameOverRoot;

    public AppController(StartScreen startScreen) {
        this.startScreen = Objects.requireNonNull(startScreen, "startScreen cannot be null");//생성자에 전달된 startScreen이 null인지 확인후 저장
        this.stage = Objects.requireNonNull(startScreen.getStage());//StartScreen의 stage를 Appcontroller의 stage에 저장
        this.scene = Objects.requireNonNull(startScreen.getScene());//StartScreen의 scene을 Appcontroller의 scene에 저장
        this.startScreen.makeStartScreen(this::ShowGame, this::ShowScoreBoard, this::ShowSettings);//StartScreen의 makeStartScreen에 콜백함수 전달, 버튼 클릭시 화면 전환

        //ScoreBoardScreen과 SettingsScreen도 StartScreen과 동일하게 생성하고 콜백함수 전달
        this.scoreBoardScreen = new ScoreBoardScreen();
        this.scoreBoardScreen.makeSB(this::ShowStartScreen);

        this.settingsScreen = new SettingsScreen();
        this.settingsScreen.makeSS(stage, scene, this::ShowStartScreen);
    }

    public void ShowStartScreen() {
        scene.setOnKeyPressed(null);
        stage.setTitle("Tetris");
        startScreen.resetSelection();
        scene.setRoot(startScreen.showStartScreen());
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
        settingsScreen.activate();
        stage.setTitle("Tetris - 설정");
        scene.setRoot(settingsScreen.showSS());
    }

    public void ShowScoreBoard() {
        scene.setOnKeyPressed(null);
        stage.setTitle("Top 10 Scores");
        scene.setRoot(scoreBoardScreen.showSB());
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
        gameOverRoot = new GameOverRoot(result, null); // TODO:스코어 스토리지는 임시로 생성. 나중에 결정되면 실제 스코어 스토리지를 제공할 것
        scene.setRoot(gameOverRoot);
        gameOverRoot.requestFocus();
        gameController = null;
        pauseScene = null;
    }
}
