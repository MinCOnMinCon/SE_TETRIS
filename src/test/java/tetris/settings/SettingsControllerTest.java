package tetris.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tetris.settings.GameSettings.ColorBlindMode;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.KeyBindingSettings.ActionType;

class SettingsControllerTest {
    @BeforeAll
    static void initializeJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX 초기화 시간 초과");
    }

    @Test
    void colorModeNamesAndIndicesMatchGameModes() {
        assertEquals(0, GameSettings.ColorBlindMode.NORMAL.getColorMode());
        assertEquals(1, GameSettings.ColorBlindMode.RED_GREEN.getColorMode());
        assertEquals(2, GameSettings.ColorBlindMode.BLUE_YELLOW.getColorMode());
        assertEquals("일반 모드", GameSettings.ColorBlindMode.NORMAL.toString());
        assertEquals("쉬움", GameSettings.Difficulty.EASY.toString());
        assertEquals("보통", GameSettings.Difficulty.NORMAL.toString());
        assertEquals("어려움", GameSettings.Difficulty.HARD.toString());
    }

    @Test
    void initializeShowsDefaultsAndAssignsOnlyUnusedKeys(@TempDir Path tempDir) throws Exception {
        Path settingsFile = tempDir.resolve("settings.properties");
        runOnFxThread(() -> {
            SettingsStore store = new SettingsStore(settingsFile);
            Stage stage = new Stage();
            Scene scene = new Scene(new BorderPane(), 960, 540);
            stage.setScene(scene);
            ComboBox<String> resolution = new ComboBox<>();
            ComboBox<ColorBlindMode> color = new ComboBox<>();
            ComboBox<Difficulty> difficulty = new ComboBox<>();
            VBox rows = new VBox();
            for (int index = 0; index < ActionType.values().length; index++) {
                rows.getChildren().add(new HBox(new Label(), new Button()));
            }
            Label status = new Label();
            SettingsController controller = new SettingsController(
                    stage, scene, resolution, color, difficulty, store.getInputSettingData(), rows, status,
                    new Button("설정 저장"), new Button("설정 초기화"), new Button("스코어보드 초기화"),
                    new Button("뒤로가기"), store);

            controller.initialize();

            assertEquals("960x540", resolution.getValue());
            assertEquals(ColorBlindMode.NORMAL, color.getValue());
            assertEquals(Difficulty.NORMAL, difficulty.getValue());
            assertEquals(960, stage.getWidth(), 0.1);
            assertEquals(540, stage.getHeight(), 0.1);

            resolution.setValue("1200x675");
            color.setValue(ColorBlindMode.RED_GREEN);
            difficulty.setValue(Difficulty.HARD);
            resolution.fireEvent(new ActionEvent());
            color.fireEvent(new ActionEvent());
            difficulty.fireEvent(new ActionEvent());

            assertEquals("1200x675", store.getResolutionPreset());
            assertEquals(ColorBlindMode.RED_GREEN, store.getColorBlindMode());
            assertEquals(Difficulty.HARD, store.getDifficulty());
            assertEquals(1200, stage.getWidth(), 0.1);
            assertEquals(675, stage.getHeight(), 0.1);

            Button leftKey = (Button) ((HBox) rows.getChildren().get(ActionType.MOVE_LEFT.ordinal()))
                    .getChildren().get(1);
            assertEquals("Left", leftKey.getText());
            controller.activate();
            leftKey.fire();
            scene.getRoot().fireEvent(keyPressed(KeyCode.RIGHT));

            assertEquals("Left", leftKey.getText());
            assertTrue(status.getText().contains("이미 배정"));

            scene.getRoot().fireEvent(keyPressed(KeyCode.A));
            leftKey.fire();
            controller.dispose();
            scene.getRoot().fireEvent(keyPressed(KeyCode.Q));

            assertEquals("A", leftKey.getText());
            assertEquals(KeyCode.A, store.getInputSettingData().getKeyCode(ActionType.MOVE_LEFT));
            assertFalse(Files.exists(settingsFile));
        });
    }

    private static KeyEvent keyPressed(KeyCode keyCode) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false);
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
