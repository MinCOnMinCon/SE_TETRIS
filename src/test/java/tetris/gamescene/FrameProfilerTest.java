package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FrameProfilerTest {
    @TempDir Path directory;

    @Test
    void countsBoundariesAndAssociatesMaximumWithPreviousHandler() {
        AtomicLong time = new AtomicLong();
        FrameProfiler profiler = new FrameProfiler(time::get, directory.resolve("report.txt"));
        frame(profiler, time, 0, 1_000_000);
        frame(profiler, time, 16_700_000, 2_000_000);
        frame(profiler, time, 66_700_000, 3_000_000);
        frame(profiler, time, 166_700_000, 9_000_000);
        String report = profiler.summary();
        assertTrue(report.contains("측정 호출 간격 수: 3"));
        assertTrue(report.contains("<= 16.7ms: 33.333%"));
        assertTrue(report.contains("> 16.7ms: 66.667%"));
        assertTrue(report.contains("> 50ms: 33.333%"));
        assertTrue(report.contains("최대 호출 간격: 100.000ms"));
        assertTrue(report.contains("직전 핸들러 실행 시간: 3.000ms"));
        assertFalse(report.contains("주요 호출"));
    }

    @Test
    void excludesPauseGapAndWritesUtf8Report() throws Exception {
        AtomicLong time = new AtomicLong();
        Path file = directory.resolve("nested/report.txt");
        FrameProfiler profiler = new FrameProfiler(time::get, file);
        frame(profiler, time, 0, 1_000_000);
        profiler.resetContinuity();
        frame(profiler, time, 10_000_000_000L, 1_000_000);
        assertTrue(profiler.summary().contains("측정 자료 없음"));
        frame(profiler, time, 10_016_000_000L, 2_000_000);
        profiler.saveReport();
        assertEquals(profiler.summary(), Files.readString(file));
        assertTrue(profiler.summary().contains("최대 호출 간격: 16.000ms"));
    }

    @Test
    void defersSaveUntilHandlerFinishes() throws Exception {
        AtomicLong time = new AtomicLong();
        Path file = directory.resolve("report.txt");
        FrameProfiler profiler = new FrameProfiler(time::get, file);
        profiler.measure(() -> {
            profiler.saveReport();
            assertFalse(Files.exists(file));
            time.set(5_000_000);
        });
        assertTrue(Files.exists(file));
        frame(profiler, time, 100_000_000, 1_000_000);
        assertTrue(profiler.summary().contains("직전 핸들러 실행 시간: 5.000ms"));
    }

    @Test
    void recordsExecutionAndPropagatesException() {
        AtomicLong time = new AtomicLong();
        FrameProfiler profiler = new FrameProfiler(time::get, directory.resolve("report.txt"));
        RuntimeException failure = new IllegalStateException("test");
        assertSame(failure, assertThrows(IllegalStateException.class, () -> profiler.measure(() -> {
            time.set(7_000_000);
            throw failure;
        })));
        frame(profiler, time, 100_000_000, 1_000_000);
        assertTrue(profiler.summary().contains("직전 핸들러 실행 시간: 7.000ms"));
    }

    private void frame(FrameProfiler profiler, AtomicLong time, long start, long duration) {
        time.set(start);
        profiler.measure(() -> time.set(start + duration));
    }
}
