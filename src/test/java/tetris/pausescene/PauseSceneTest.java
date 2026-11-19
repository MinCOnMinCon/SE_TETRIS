package tetris.pausescene;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PauseSceneTest {
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
    void requiresBothPauseActions() throws Exception {
        runOnFxThread(() -> {
            assertThrows(NullPointerException.class, () -> new PauseScene(null, () -> {}));
            assertThrows(NullPointerException.class, () -> new PauseScene(() -> {}, null));
        });
    }

    @Test
    void defaultPauseHasNoChat() throws Exception {
        runOnFxThread(() -> {
            PauseScene pauseScene = new PauseScene(() -> {}, () -> {});
            assertFalse(pauseScene.isChatEnabled());
            assertTrue(pauseScene.GetRoot() instanceof VBox);
        });
    }

    @Test
    void onlinePauseEnablesChatAndSendsMessages() throws Exception {
        runOnFxThread(() -> {
            List<String> sent = new ArrayList<>();
            PauseScene pauseScene = new PauseScene(() -> {}, () -> {}, "Server", sent::add);
            assertTrue(pauseScene.isChatEnabled());
            assertTrue(pauseScene.GetRoot() instanceof VBox);

            VBox root = (VBox) pauseScene.GetRoot();
            HBox content = (HBox) root.getChildren().get(0);
            VBox chat = (VBox) content.getChildren().get(1);
            TextField input = null;
            for (Node node : chat.getChildren()) {
                if (node instanceof TextField textField) {
                    input = textField;
                    break;
                }
            }
            assertTrue(input != null);
            input.setText("hello");
            input.fireEvent(new javafx.event.ActionEvent());

            assertTrue(sent.contains("Server: hello"));
            pauseScene.addIncomingMessage("Client: hi");
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(10, TimeUnit.SECONDS), "JavaFX 작업 시간 초과");
        if (error.get() != null) {
            if (error.get() instanceof Exception exception) {
                throw exception;
            }
            if (error.get() instanceof Error err) {
                throw err;
            }
            throw new RuntimeException(error.get());
        }
    }
}
