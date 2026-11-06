package tetris.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.gameoverscene.GameOverRoot;
import tetris.gamescene.GameController;
import tetris.pausescene.PauseScene;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.SettingsStore;
import tetris.settings.GameSettings.ColorBlindMode;

// GameController를 직접 생성해 아이템 블록을 확인한다.
public class GameSceneTest {
    // 10줄당 아이템 하나: 테스트 시작 시 누적 삭제 줄 수를 지정한다.
    private static final int INITIAL_CLEARED_LINE_COUNT = 10;
    // EASY, NORMAL, HARD 중 테스트할 난이도를 지정한다.
    private static final Difficulty TEST_DIFFICULTY = Difficulty.HARD;
    // NORMAL, RED_GREEN, BLUE_YELLOW 중 테스트할 색상 모드를 지정한다.
    private static final ColorBlindMode TEST_COLOR_MODE = ColorBlindMode.RED_GREEN;
    private static final GameMode TEST_GAMEMODE = GameMode.ITEM;

    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        Application.launch(TestApplication.class, args);
    }

    // 실행 진입점과 JavaFX Application을 같은 파일 안에서 분리한다.
    public static class TestApplication extends Application {
        private GameController gameController;

        @Override
        public void start(Stage stage) {
            SettingsStore settingsStore = new SettingsStore();
            settingsStore.load();
            GameSettings savedSettings = settingsStore.getGameSettings();
            GameSettings testSettings = new GameSettings(
                    savedSettings.resolutionPreset(), savedSettings.screenWidth(), savedSettings.screenHeight(),
                    TEST_COLOR_MODE.getColorMode(), savedSettings.keyBindings(), TEST_DIFFICULTY, TEST_GAMEMODE);

            Scene scene = new Scene(new BorderPane(),
                    savedSettings.screenWidth(), savedSettings.screenHeight());
            PauseScene pauseScene = new PauseScene(() -> {
                scene.setRoot(gameController.GetRoot());
                gameController.GetRoot().requestFocus();
                gameController.ResumeGame();
            }, Platform::exit);
            gameController = new GameController(() -> {
                scene.setRoot(pauseScene.GetRoot());
                pauseScene.GetRoot().requestFocus();
            }, result -> {
                GameOverRoot gameOverRoot = new GameOverRoot(result, Platform::exit);
                scene.setRoot(gameOverRoot);
                gameOverRoot.requestFocus();
            }, testSettings, INITIAL_CLEARED_LINE_COUNT);
            scene.setRoot(gameController.GetRoot());
            stage.setTitle("GameSceneTest");
            stage.setScene(scene);
            stage.show();
            gameController.GetRoot().requestFocus();
            gameController.GameStart();
        }
    }
}
