package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tetris.block.blocks.BlockI;
import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;
import tetris.settings.GameSettings;
import tetris.settings.KeyBindingSettings;

/**
 * 실제 JavaFX 창과 GameController를 실행하는 명시적 성능 테스트.
 * PowerShell: $env:TETRIS_FRAME_PERFORMANCE='true'; .\gradlew.bat test --tests tetris.gamescene.FramePerformanceTest --rerun-tasks
 * TETRIS_FRAME_WARMUP_SECONDS(기본 5), TETRIS_FRAME_DURATION_SECONDS(기본 60),
 * TETRIS_FRAME_MAX_SLOW_PERCENT(기본 1), TETRIS_FRAME_MINIMUM_FPS(기본 30)으로 조정한다.
 * 일반 test 실행에서는 건너뛰며, 실행 시 실제 그래픽 환경이 필요하다.
 */
@EnabledIfEnvironmentVariable(named = "TETRIS_FRAME_PERFORMANCE", matches = "(?i)true")
class FramePerformanceTest {
    private static final Path REPORT_DIRECTORY = Path.of("performance-reports", "frame-performance-test");

    @BeforeAll
    static void initializeJavaFx() throws Exception {
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
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX initialization timed out");
    }

    @Test
    void actualGameFramesStayWithinBudget() throws Exception {
        double warmup = option("TETRIS_FRAME_WARMUP_SECONDS", 5, 0, 3600);
        double duration = option("TETRIS_FRAME_DURATION_SECONDS", 60, 3, 3600);
        double allowedSlowPercent = option("TETRIS_FRAME_MAX_SLOW_PERCENT", 1, 0, 100);
        double minimumFps = option("TETRIS_FRAME_MINIMUM_FPS", 30, 1, 1000);
        Files.createDirectories(REPORT_DIRECTORY);
        Files.writeString(REPORT_DIRECTORY.resolve("result.txt"), "RUNNING\n", StandardCharsets.UTF_8);
        Session session = new Session(warmup, duration);
        try {
            runOnFx(session::start);
            Measurement measurement = session.completion.get((long) Math.ceil(warmup + duration + 30), TimeUnit.SECONDS);
            var result = measurement.frames();
            boolean enoughFrames = result.intervalCount() >= Math.ceil(duration * minimumFps);
            boolean passed = measurement.seconds() >= duration && enoughFrames && measurement.lineClears() > 0
                    && result.over100msPercent() <= allowedSlowPercent;
            String report = String.format(Locale.ROOT,
                    "%s%nScenario: normal-I-block-line-clear-v1%nResolution: 1280x720; difficulty: NORMAL%n"
                    + "Warmup: %.3fs%nMeasurement: %.3fs (required %.3fs)%nIntervals: %d (minimum %.0f)%n"
                    + "Over 100ms: %d / %.3f%% (allowed %.3f%%)%nMaximum interval: %.3fms%n"
                    + "Completed line-clear actions: %d%nThis measures update cadence, not display FPS.%n",
                    passed ? "PASS" : "FAIL", warmup, measurement.seconds(), duration, result.intervalCount(),
                    Math.ceil(duration * minimumFps), result.over100msCount(), result.over100msPercent(),
                    allowedSlowPercent, result.maxIntervalMillis(), measurement.lineClears());
            Files.writeString(REPORT_DIRECTORY.resolve("result.txt"), report, StandardCharsets.UTF_8);
            System.out.println(report);
            assertAll(
                    () -> assertTrue(measurement.seconds() >= duration, "Measurement ended early"),
                    () -> assertTrue(enoughFrames, "Insufficient frame samples"),
                    () -> assertTrue(measurement.lineClears() > 0, "No line-clear action was measured"),
                    () -> assertTrue(result.over100msPercent() <= allowedSlowPercent,
                            "Intervals over 100ms exceeded the allowed percentage"));
        } catch (Exception error) {
            Files.writeString(REPORT_DIRECTORY.resolve("result.txt"),
                    "FAIL\n" + error + "\n", StandardCharsets.UTF_8);
            throw error;
        } finally {
            runOnFx(session::close);
        }
    }

    private record Measurement(FrameProfiler.PerformanceTestResult frames, double seconds, int lineClears) {}

    // FramePerformanceTest 시나리오 전체는 이 테스트 파일에서만 관리한다.
    private static final class Session {
        private final double warmup, duration;
        private final CompletableFuture<Measurement> completion = new CompletableFuture<>();
        private FrameProfiler profiler;
        private GameController controller;
        private Stage stage;
        private AnimationTimer driver;
        private long start, measurementStart, lastInput, lastDrop;
        private int inputStep, lineClears;
        private boolean closing;
        private Thread.UncaughtExceptionHandler previousHandler;

        Session(double warmup, double duration) {
            this.warmup = warmup;
            this.duration = duration;
        }

        void start() {
            previousHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> fail(error));
            profiler = new FrameProfiler(System::nanoTime, REPORT_DIRECTORY.resolve("frame-profile.txt"));
            GameSettings settings = new GameSettings("1280x720", 1280, 720, 0,
                    new KeyBindingSettings().getAllBindings(), GameSettings.Difficulty.NORMAL, GameSettings.GameMode.NORMAL);
            controller = new GameController(() -> fail(new IllegalStateException("Unexpected pause")),
                    result -> { if (!closing) fail(new IllegalStateException("Unexpected game over")); }, settings, 0, profiler);
            controller.accessForFramePerformanceTest().currentBlock().SetCurrentBlock(new BlockI(0).GetBlockData());
            fixQueue();
            stage = new Stage();
            stage.setTitle("FramePerformanceTest - automated scenario");
            stage.setScene(new Scene(controller.GetRoot(), 1280, 720));
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> {
                event.consume();
                fail(new IllegalStateException("Performance test window was closed"));
            });
            stage.show();
            start = lastInput = lastDrop = System.nanoTime();
            controller.GameStart();
            driver = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    try {
                        advance();
                    } catch (Throwable error) {
                        fail(error);
                    }
                }
            };
            driver.start();
        }

        void advance() {
            long now = System.nanoTime();
            if (measurementStart == 0 && (now - start) / 1e9 >= warmup) {
                profiler.resetForFramePerformanceTest();
                measurementStart = now;
                lineClears = 0;
            }
            if (measurementStart != 0 && (now - measurementStart) / 1e9 >= duration) {
                close();
                completion.complete(new Measurement(profiler.resultForFramePerformanceTest(),
                        (now - measurementStart) / 1e9, lineClears));
                return;
            }
            var access = controller.accessForFramePerformanceTest();
            if (!access.ready()) return;
            if (now - lastDrop >= 1_500_000_000L) {
                prepareLineClear(access);
                access.input().HandleKeyCode(KeyCode.SPACE);
                if (access.board().GetClearedRows()[0] == -1) {
                    throw new IllegalStateException("Scripted drop did not clear a line");
                }
                if (measurementStart != 0) lineClears++;
                fixQueue();
                lastDrop = now;
            } else if (now - lastInput >= 120_000_000L) {
                KeyCode[] keys = {KeyCode.LEFT, KeyCode.RIGHT, KeyCode.X, KeyCode.Z, KeyCode.DOWN};
                access.input().HandleKeyCode(keys[inputStep++ % keys.length]);
                lastInput = now;
            }
        }

        // 난수 큐를 고정해 매 실행에 I 블록 이동·회전·1~4줄 삭제를 반복한다.
        private void fixQueue() {
            var queue = controller.accessForFramePerformanceTest().currentBlock().GetBlockQueue();
            for (int i = 0; i < queue.GetMaxQueueSize(); i++) queue.SetBlock(new BlockI(0).GetBlockData(), i);
        }

        // 게임오버 없이 측정 시간을 확보하기 위한 인공 보드다. 실제 사용자 플레이 전체를 재현하지는 않는다.
        private void prepareLineClear(GameController.FramePerformanceTestAccess access) {
            BoardElement[][] board = access.board().GetBoard();
            BlockData block = access.currentBlock().GetCurrentBlock();
            boolean[][] shape = block.GetShape();
            int firstRow = shape.length, lastRow = -1;
            for (int row = 0; row < shape.length; row++) {
                for (boolean occupied : shape[row]) {
                    if (occupied) {
                        firstRow = Math.min(firstRow, row);
                        lastRow = Math.max(lastRow, row);
                    }
                }
            }
            if (lastRow < 0) throw new IllegalStateException("Empty block shape");
            block.SetX(3);
            block.SetY(board.length - 1 - lastRow);
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    boolean filled = row >= block.GetY() + firstRow;
                    board[row][col] = new BoardElement(filled ? Color.GRAY : Color.BLACK, filled);
                }
            }
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col]) board[block.GetY() + row][block.GetX() + col] = new BoardElement();
                }
            }
        }

        void fail(Throwable error) {
            completion.completeExceptionally(error);
            close();
        }

        void close() {
            if (closing) return;
            closing = true;
            try {
                if (driver != null) driver.stop();
                if (controller != null) controller.StopGame();
            } finally {
                if (stage != null) stage.close();
                Thread.currentThread().setUncaughtExceptionHandler(previousHandler);
            }
        }
    }

    private static void runOnFx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static double option(String name, double fallback, double min, double max) {
        String configured = System.getenv(name);
        double value = configured == null ? fallback : Double.parseDouble(configured);
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }
}
