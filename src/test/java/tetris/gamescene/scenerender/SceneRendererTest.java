package tetris.gamescene.scenerender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import tetris.block.blocks.BlockO;
import tetris.block.data.BlockData;
import tetris.gamescene.board.GameBoard;
import tetris.settings.GameSettings;

class SceneRendererTest {
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
        org.junit.jupiter.api.Assertions.assertTrue(
                ready.await(10, TimeUnit.SECONDS), "JavaFX 초기화 시간 초과");
    }

    @ParameterizedTest(name = "CreateRoot: {0}x{1}")
    @CsvSource({"1280, 720", "1920, 1080", "2560, 1440"})
    void createsAllCanvasesWithConfiguredSizes(int width, int height) throws Exception {
        runOnFxThread(() -> {
            GameSettings settings = new GameSettings(
                    width + "x" + height, width, height, 0,
                    java.util.Map.of(), GameSettings.Difficulty.NORMAL);
            RenderConfig config = new RenderConfig(settings, 3);
            GameBoard board = new GameBoard();
            BlockData[] queue = {
                new BlockO(0).GetBlockData(),
                new BlockO(0).GetBlockData(),
                new BlockO(0).GetBlockData()
            };
            SceneRenderState state = new SceneRenderState(
                    board.GetBoard(), new BlockO(0).GetBlockData(),
                    100, queue, new BlockO(0).GetBlockData());

            BorderPane root = assertInstanceOf(
                    BorderPane.class, new SceneRenderer(config).CreateRoot(state));
            // 반환된 화면의 계층을 조회하고 private 필드에는 접근하지 않는다.
            HBox content = assertInstanceOf(HBox.class, root.getCenter());
            assertEquals(3, content.getChildren().size(), "왼쪽 패널·보드·오른쪽 패널");
            VBox left = assertInstanceOf(VBox.class, content.getChildren().get(0));
            Canvas boardCanvas = assertInstanceOf(Canvas.class, content.getChildren().get(1));
            VBox right = assertInstanceOf(VBox.class, content.getChildren().get(2));
            assertEquals(1, left.getChildren().size(), "큐 Canvas 하나");
            assertEquals(2, right.getChildren().size(), "홀딩과 점수 Canvas");

            Canvas queueCanvas = assertInstanceOf(Canvas.class, left.getChildren().get(0));
            Canvas holdingCanvas = assertInstanceOf(Canvas.class, right.getChildren().get(0));
            Canvas scoreCanvas = assertInstanceOf(Canvas.class, right.getChildren().get(1));
            assertCanvasSize(boardCanvas,
                    board.GetBoard()[0].length * config.blockSide,
                    board.GetBoard().length * config.blockSide);
            assertCanvasSize(queueCanvas, config.blockQueueCanvasWidth, config.blockQueueCanvasHeight);
            assertCanvasSize(holdingCanvas, config.blockHoldingCanvasWidth, config.blockHoldingCanvasHeight);
            assertCanvasSize(scoreCanvas, config.scoreCanvasWidth, config.scoreCanvasHeight);
        });
    }

    @Test
    void createRootRejectsNullState() throws Exception {
        runOnFxThread(() -> {
            GameSettings settings = new GameSettings(
                    "1280x720", 1280, 720, 0, java.util.Map.of(), GameSettings.Difficulty.NORMAL);
            SceneRenderer renderer = new SceneRenderer(new RenderConfig(settings, 3));

            NullPointerException exception = assertThrows(
                    NullPointerException.class, () -> renderer.CreateRoot(null));
            assertEquals("state must not be null", exception.getMessage());
        });
    }

    private static void assertCanvasSize(Canvas canvas, double width, double height) {
        assertEquals(width, canvas.getWidth(), "Canvas 너비");
        assertEquals(height, canvas.getHeight(), "Canvas 높이");
    }

    // JavaFX 스레드에서 실행하고, 내부 검증 실패도 테스트 결과에 반영한다.
    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @Test
    void acceptsRenderConfigAndRejectsNullConfig() {
        GameSettings settings = new GameSettings(
                "1280x720", 1280, 720, 0, java.util.Map.of(), GameSettings.Difficulty.NORMAL);
        assertDoesNotThrow(() -> new SceneRenderer(new RenderConfig(settings, 2)));
        assertThrows(NullPointerException.class, () -> new SceneRenderer(null));
    }
}
