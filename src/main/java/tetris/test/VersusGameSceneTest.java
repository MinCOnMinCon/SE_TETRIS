package tetris.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.gameoverscene.VersusOverRoot;
import tetris.gameoverscene.VersusResult;
import tetris.pausescene.PauseScene;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.GameSettings.PlayMode;
import tetris.settings.SettingsStore;
import tetris.settings.GameSettings.ColorBlindMode;
import tetris.versusmode.VersusManager;
import tetris.versusmode.VersusManager.MatchOutcome;
import tetris.versusmode.VersusManager.MatchResult;

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
                VersusResult.LocalWinLose localWinLose = toLocalWinLose(result);
                VersusResult versusResult = new VersusResult(
                        result.player1Score(), result.player2Score(), localWinLose);
                VersusOverRoot versusOverRoot =
                        new VersusOverRoot(versusResult, Platform::exit);
                scene.setRoot(versusOverRoot.getRoot());
                versusOverRoot.getRoot().requestFocus();
            }, INITIAL_CLEARED_LINE_COUNT);
            scene.setRoot(versusManager.GetRoot());
            stage.setTitle("VersusGameSceneTest");
            stage.setScene(scene);
            stage.show();
            versusManager.GetRoot().requestFocus();
            versusManager.GameStart();
        }

        private VersusResult.LocalWinLose toLocalWinLose(MatchResult result) {
            if (result.outcome() == MatchOutcome.PLAYER1_WIN) {
                return VersusResult.LocalWinLose.LOCALWIN;
            }
            if (result.outcome() == MatchOutcome.PLAYER2_WIN) {
                return VersusResult.LocalWinLose.LOCALLOSE;
            }
            return result.player1Score() >= result.player2Score()
                    ? VersusResult.LocalWinLose.LOCALWIN
                    : VersusResult.LocalWinLose.LOCALLOSE;
        }
    }
}
