package tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tetris.settings.SettingsStore;

class AppControllerTest {
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
    void requiresStage() {
        assertThrows(NullPointerException.class,
                () -> new AppController(null, null, new SettingsStore()));
    }

    @Test
    void requiresSceneAndSettingsStore() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            Scene scene = new Scene(new BorderPane(), 960, 540);
            SettingsStore store = new SettingsStore();

            NullPointerException missingScene = assertThrows(NullPointerException.class,
                    () -> new AppController(stage, null, store));
            NullPointerException missingStore = assertThrows(NullPointerException.class,
                    () -> new AppController(stage, scene, null));

            assertEquals("scene cannot be null", missingScene.getMessage());
            assertEquals("settingsStore cannot be null", missingStore.getMessage());
        });
    }

    @Test
    void switchesStartScoreAndSettingsWithoutSaving(@TempDir Path tempDir) throws Exception {
        Path settingsFile = tempDir.resolve("settings.properties");
        runOnFxThread(() -> {
            Stage stage = new Stage();
            Scene scene = new Scene(new BorderPane(), 960, 540);
            stage.setScene(scene);
            AppController controller = new AppController(stage, scene, new SettingsStore(settingsFile));

            controller.ShowStartScreen();
            Parent startRoot = scene.getRoot();

            assertEquals("Tetris", stage.getTitle());
            assertEquals("TETRIS", ((Label) ((BorderPane) startRoot).getTop()).getText());

            controller.ResumeGame();
            assertSame(startRoot, scene.getRoot());

            controller.ShowScoreBoard();
            controller.ShowScoreBoard();
            assertEquals("Top 10 Scores", stage.getTitle());

            controller.ShowSettings();
            BorderPane settingsRoot = (BorderPane) scene.getRoot();
            Button back = (Button) ((VBox) settingsRoot.getBottom()).getChildren().get(1);

            assertEquals("Tetris - 설정", stage.getTitle());
            assertEquals("설정", ((Label) settingsRoot.getTop()).getText());

            back.fire();

            assertEquals("Tetris", stage.getTitle());
            assertEquals("TETRIS", ((Label) ((BorderPane) scene.getRoot()).getTop()).getText());
            assertFalse(Files.exists(settingsFile));
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
