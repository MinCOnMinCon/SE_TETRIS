package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import tetris.block.data.CurrentBlock;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

// 실제 데이터 연속 전송 상태 확인 (난이도, 모드 조합 전체 확인)
class NetworkConnectionBoardSequenceTest {
    private static final int FRAME_COUNT = 100;
    private static final long FRAME_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1) / 60;

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("difficultyAndGameModeCombinations")
    void deliversOneHundredGameBoardsAtSixtyFramesPerSecond(
            Difficulty difficulty, GameMode gameMode) throws Exception {
        long testStartNanos = System.nanoTime();
        AtomicInteger receivedCount = new AtomicInteger();
        AtomicLong lastReceiveTime = new AtomicLong();
        long transferNanos = -1;
        String reportName = String.format(Locale.ROOT, "board-sequence-%s-%s.txt",
                difficulty.name().toLowerCase(Locale.ROOT), gameMode.name().toLowerCase(Locale.ROOT));

        try {
            CountDownLatch receivedLatch = new CountDownLatch(FRAME_COUNT);
            ConcurrentLinkedQueue<NetworkData> receivedData = new ConcurrentLinkedQueue<>();
            CurrentBlock currentBlock = NetworkTestSupport.createCurrentBlock(difficulty, gameMode);

            try (NetworkTestSupport.Session session = NetworkTestSupport.connect(data -> {
                receivedData.add(data);
                if (receivedCount.incrementAndGet() == FRAME_COUNT) {
                    lastReceiveTime.set(System.nanoTime());
                }
                receivedLatch.countDown();
            }, data -> {})) {
                long scheduleStart = System.nanoTime();
                for (int frame = 0; frame < FRAME_COUNT; frame++) {
                    waitUntil(scheduleStart + frame * FRAME_INTERVAL_NANOS);
                    NetworkData data = new NetworkData(currentBlock, frame);
                    data.SetMessageType(MessageType.DATA);
                    session.clientController.send(data);
                }

                assertTrue(receivedLatch.await(10, TimeUnit.SECONDS),
                        "Server did not receive all 100 frame boards within 10 seconds");
                transferNanos = lastReceiveTime.get() - scheduleStart;
                List<NetworkData> actualData = new ArrayList<>(receivedData);
                assertEquals(FRAME_COUNT, actualData.size(), "Received board count differs");
                for (int frame = 0; frame < FRAME_COUNT; frame++) {
                    NetworkData received = actualData.get(frame);
                    NetworkTestSupport.assertCurrentBlockDataEquals(currentBlock, received, frame);
                }

                double elapsedMillis = transferNanos / 1_000_000.0;
                double framesPerSecond = (FRAME_COUNT - 1) / (transferNanos / 1_000_000_000.0);
                double scheduledSendMillis =
                        (FRAME_COUNT - 1) * FRAME_INTERVAL_NANOS / 1_000_000.0;
                double testTimeMillis = (System.nanoTime() - testStartNanos) / 1_000_000.0;
                NetworkTestSupport.writeReport(reportName, String.format(Locale.ROOT,
                        "Difficulty: %s%n"
                        + "Game mode: %s%n"
                        + "Result: PASS%n"
                        + "Frame interval: %.3f ms (60 FPS target)%n"
                        + "Full game-state packets sent and received: %d%n"
                        + "Scheduled send duration: %.3f ms%n"
                        + "End-to-end transfer time: %.3f ms%n"
                        + "Receive throughput: %.2f packets/second%n"
                        + "Sequence, board, current block, and queue contents: verified%n"
                        + "Total test time: %.3f ms%n",
                        difficulty.name(), gameMode.name(),
                        FRAME_INTERVAL_NANOS / 1_000_000.0, FRAME_COUNT,
                        scheduledSendMillis, elapsedMillis, framesPerSecond, testTimeMillis));
                System.out.printf(Locale.ROOT,
                        "Frame-paced game states [%s / %s]: %d packets in %.3f ms (%.2f packets/s)%n",
                        difficulty.name(), gameMode.name(), FRAME_COUNT, elapsedMillis, framesPerSecond);
            }
        } catch (Exception | AssertionError failure) {
            double testTimeMillis = (System.nanoTime() - testStartNanos) / 1_000_000.0;
            String transferTime = transferNanos < 0
                    ? "not completed"
                    : String.format(Locale.ROOT, "%.3f ms", transferNanos / 1_000_000.0);
            try {
                NetworkTestSupport.writeReport(reportName, String.format(Locale.ROOT,
                        "Difficulty: %s%n"
                        + "Game mode: %s%n"
                        + "Result: FAIL%n"
                        + "Frames received before failure: %d / %d%n"
                        + "Total time before failure: %.3f ms%n"
                        + "End-to-end transfer time: %s%n"
                        + "Failure: %s: %s%n",
                        difficulty.name(), gameMode.name(), receivedCount.get(), FRAME_COUNT, testTimeMillis,
                        transferTime, failure.getClass().getSimpleName(),
                        String.valueOf(failure.getMessage())));
            } catch (IOException reportFailure) {
                failure.addSuppressed(reportFailure);
            }
            throw failure;
        }
    }

    static Stream<Arguments> difficultyAndGameModeCombinations() {
        return Stream.of(Difficulty.values())
                .flatMap(difficulty -> Stream.of(GameMode.values())
                        .map(gameMode -> Arguments.of(difficulty, gameMode)));
    }

    private static void waitUntil(long deadlineNanos) throws InterruptedException {
        long remainingNanos;
        while ((remainingNanos = deadlineNanos - System.nanoTime()) > 0) {
            LockSupport.parkNanos(remainingNanos);
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted while waiting for the next frame");
            }
        }
    }
}
