package tetris.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.gameoverscene.VersusOverRoot;
import tetris.pausescene.PauseScene;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.GameSettings.PlayMode;
import tetris.settings.SettingsStore;
import tetris.settings.GameSettings.ColorBlindMode;
import tetris.versusmode.VersusManager;
import tetris.versusmode.VersusManager.Player;
import tetris.versusmode.VersusResult;

// 로컬 2인 대전 모드를 직접 생성해 아이템 블록과 대전 동작을 확인한다.
public class VersusGameSceneTest {
    // 10줄당 아이템 하나: 테스트 시작 시 누적 삭제 줄 수를 지정한다.
    private static final int INITIAL_CLEARED_LINE_COUNT = 10;
    // EASY, NORMAL, HARD 중 테스트할 난이도를 지정한다.
    private static final Difficulty TEST_DIFFICULTY = Difficulty.HARD;
    // NORMAL, RED_GREEN, BLUE_YELLOW 중 테스트할 색상 모드를 지정한다.
    private static final ColorBlindMode TEST_COLOR_MODE = ColorBlindMode.RED_GREEN;
    private static final GameMode TEST_GAMEMODE = GameMode.TIMELIMIT;

    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        Application.launch(TestApplication.class, args);
    }

    // 실행 진입점과 JavaFX Application을 같은 파일 안에서 분리한다.
    public static class TestApplication extends Application {
        private VersusManager versusManager;

        @Override
        public void start(Stage stage) {
            SettingsStore settingsStore = new SettingsStore();
            settingsStore.load();
            GameSettings savedSettings = settingsStore.getGameSettings();
            GameSettings testSettings = new GameSettings(
                    savedSettings.resolutionPreset(), savedSettings.screenWidth(), savedSettings.screenHeight(),
                    TEST_COLOR_MODE.getColorMode(), savedSettings.keyBindings(), TEST_DIFFICULTY, TEST_GAMEMODE);
            testSettings = testSettings.withPlayerKeyBindings(
                    settingsStore.gameSettingsFor(PlayMode.LOCAL).playerKeyBindings());

            Scene scene = new Scene(new BorderPane(),
                    savedSettings.screenWidth(), savedSettings.screenHeight());
            PauseScene pauseScene = new PauseScene(() -> {
                scene.setRoot(versusManager.GetRoot());
                versusManager.GetRoot().requestFocus();
                versusManager.ResumeGame();
            }, Platform::exit);
            versusManager = new VersusManager(testSettings, () -> {
                scene.setRoot(pauseScene.GetRoot());
                pauseScene.GetRoot().requestFocus();
            }, result -> {
                VersusOverRoot versusOverRoot =
                        new VersusOverRoot(result, Platform::exit);
                scene.setRoot(versusOverRoot.getRoot());
                versusOverRoot.getRoot().requestFocus();
            }, INITIAL_CLEARED_LINE_COUNT);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.DIGIT1) {
                    versusManager.AddTestAttack(Player.PLAYER2, 5);
                } else if (event.getCode() == KeyCode.DIGIT2) {
                    versusManager.AddTestAttack(Player.PLAYER2, 10);
                } else if (event.getCode() == KeyCode.DIGIT3) {
                    versusManager.AddTestAttack(Player.PLAYER1, 10);
                } else if (event.getCode() == KeyCode.DIGIT4) {
                    versusManager.AddTestAttack(Player.PLAYER1, 5);
                }
            });
            scene.setRoot(versusManager.GetRoot());
            stage.setTitle("VersusGameSceneTest");
            stage.setScene(scene);
            stage.show();
            versusManager.GetRoot().requestFocus();
            versusManager.GameStart();
        }
    }
}
