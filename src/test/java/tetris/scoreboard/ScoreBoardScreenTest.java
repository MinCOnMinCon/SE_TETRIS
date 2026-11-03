package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ScoreBoardScreenTest {
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
    void showsScoreTableAndRunsBackAction() throws Exception {
        runOnFxThread(() -> {
            int[] backCount = {0};
            ScoreBoardScreen screen = new ScoreBoardScreen(() -> backCount[0]++);

            BorderPane root = screen.showSB();
            VBox content = (VBox) root.getCenter();
            Label title = (Label) content.getChildren().get(0);
            Button back = (Button) content.getChildren().get(2);

            assertEquals("Top 10 Scores", title.getText());
            assertEquals("-fx-background-color: black;", root.getStyle());
            assertSame(content.getChildren().get(1), screen.getTable());
            assertEquals(List.of("순위", "이름", "점수", "난이도", "모드"),
                    screen.getTable().getColumns().stream().map(TableColumn::getText).toList());
            assertTrue(screen.getTable().getItems().size() <= 10);
            assertEquals("뒤로가기", back.getText());
            assertEquals(180, back.getPrefWidth());
            assertEquals(50, back.getPrefHeight());

            back.fire();
            screen.refreshScores();

            assertEquals(1, backCount[0]);
            assertTrue(screen.getTable().getItems().size() <= 10);
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
