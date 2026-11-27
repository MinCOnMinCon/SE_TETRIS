package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import tetris.block.data.CurrentBlock;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

// 실제 데이터 전송 상태 확인 (난이도, 모드 조합 전체 확인)
class NetworkConnectionBoardTest {
    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("difficultyAndGameModeCombinations")
    void sendsAndReceivesGameBoardAndRecordsTransferTime(
            Difficulty difficulty, GameMode gameMode) throws Exception {
        long testStartNanos = System.nanoTime();
        double transferTimeMillis = Double.NaN;
        String reportName = String.format(Locale.ROOT, "board-%s-%s.txt",
                difficulty.name().toLowerCase(Locale.ROOT), gameMode.name().toLowerCase(Locale.ROOT));

        try {
            CountDownLatch receivedLatch = new CountDownLatch(1);
            AtomicReference<NetworkData> receivedData = new AtomicReference<>();
            CurrentBlock currentBlock = NetworkTestSupport.createCurrentBlock(difficulty, gameMode);

            try (NetworkTestSupport.Session session = NetworkTestSupport.connect(data -> {
                receivedData.set(data);
                receivedLatch.countDown();
            }, data -> {})) {
                NetworkData data = new NetworkData(currentBlock, 0);
                data.SetMessageType(MessageType.DATA);
                long transferStartNanos = System.nanoTime();
                session.clientController.send(data);

                assertTrue(receivedLatch.await(5, TimeUnit.SECONDS),
                        "Server did not receive the game state within 5 seconds");
                transferTimeMillis = (System.nanoTime() - transferStartNanos) / 1_000_000.0;
                NetworkTestSupport.assertCurrentBlockDataEquals(currentBlock, receivedData.get(), 0);

                double testTimeMillis = (System.nanoTime() - testStartNanos) / 1_000_000.0;
                NetworkTestSupport.writeReport(reportName, String.format(Locale.ROOT,
                        "Difficulty: %s%n"
                        + "Game mode: %s%n"
                        + "Result: PASS%n"
                        + "Direction: client -> server%n"
                        + "Game board dimensions: %d rows x %d columns%n"
                        + "Current block, next queue, hold, and cleared rows: verified%n"
                        + "Board transfer and validation time: %.3f ms%n"
                        + "Total test time: %.3f ms%n",
                        difficulty.name(), gameMode.name(), currentBlock.GetBoard().length,
                        currentBlock.GetBoard()[0].length, transferTimeMillis, testTimeMillis));
                System.out.printf(Locale.ROOT,
                        "Network game state [%s / %s] transferred and verified in %.3f ms%n",
                        difficulty.name(), gameMode.name(), transferTimeMillis);
            }
        } catch (Exception | AssertionError failure) {
            double testTimeMillis = (System.nanoTime() - testStartNanos) / 1_000_000.0;
            String transferTime = Double.isNaN(transferTimeMillis)
                    ? "not completed"
                    : String.format(Locale.ROOT, "%.3f ms", transferTimeMillis);
            try {
                NetworkTestSupport.writeReport(reportName, String.format(Locale.ROOT,
                        "Difficulty: %s%n"
                        + "Game mode: %s%n"
                        + "Result: FAIL%n"
                        + "Total time before failure: %.3f ms%n"
                        + "Board transfer time: %s%n"
                        + "Failure: %s: %s%n",
                        difficulty.name(), gameMode.name(), testTimeMillis, transferTime,
                        failure.getClass().getSimpleName(), String.valueOf(failure.getMessage())));
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
}
