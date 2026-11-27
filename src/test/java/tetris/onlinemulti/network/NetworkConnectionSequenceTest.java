package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

// 빈 네트워크 데이터 전송, 점수로 순서 확인 (NetworkData(DATA)로 생성)
class NetworkConnectionSequenceTest {
    private static final int MESSAGE_COUNT = 1_000;

    @Test
    void deliversOneThousandMessagesInSequenceAndRecordsThroughput() throws Exception {
        CountDownLatch receivedLatch = new CountDownLatch(MESSAGE_COUNT);
        ConcurrentLinkedQueue<Long> receivedSequences = new ConcurrentLinkedQueue<>();
        AtomicReference<MessageType> unexpectedMessageType = new AtomicReference<>();

        try (NetworkTestSupport.Session session = NetworkTestSupport.connect(data -> {
            if (data.GetMessageType() != MessageType.DATA) {
                unexpectedMessageType.compareAndSet(null, data.GetMessageType());
            }
            receivedSequences.add(data.GetVersusScore());
            receivedLatch.countDown();
        }, data -> {})) {
            long start = System.nanoTime();
            for (int sequence = 0; sequence < MESSAGE_COUNT; sequence++) {
                NetworkData data = new NetworkData(MessageType.DATA);
                data.SetScore(sequence);
                session.clientController.send(data);
            }

            assertTrue(receivedLatch.await(10, TimeUnit.SECONDS),
                    "Server did not receive all 1,000 messages within 10 seconds");
            long elapsedNanos = System.nanoTime() - start;
            List<Long> actualSequences = new ArrayList<>(receivedSequences);
            assertEquals(MESSAGE_COUNT, actualSequences.size(), "Message count differs");
            assertNull(unexpectedMessageType.get(), "Received an unexpected message type");
            for (int sequence = 0; sequence < MESSAGE_COUNT; sequence++) {
                assertEquals((long) sequence, actualSequences.get(sequence),
                        "Message sequence differs at index " + sequence);
            }

            double elapsedMillis = elapsedNanos / 1_000_000.0;
            double messagesPerSecond = MESSAGE_COUNT / (elapsedNanos / 1_000_000_000.0);
            NetworkTestSupport.writeReport("sequence.txt", String.format(Locale.ROOT,
                    "Result: PASS%n"
                    + "Direction: client -> server%n"
                    + "Messages sent and received: %d%n"
                    + "Transfer time: %.3f ms%n"
                    + "Throughput: %.2f messages/second%n"
                    + "Sequence: verified in order, no missing messages%n",
                    MESSAGE_COUNT, elapsedMillis, messagesPerSecond));
            System.out.printf(Locale.ROOT,
                    "Network sequence: %d messages in %.3f ms (%.2f messages/s)%n",
                    MESSAGE_COUNT, elapsedMillis, messagesPerSecond);
        }
    }
}
