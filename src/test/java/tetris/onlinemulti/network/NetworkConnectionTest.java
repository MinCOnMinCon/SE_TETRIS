package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

// 서버/클라이언트의 isConnected()로 연결 여부 확인
class NetworkConnectionTest {
    @Test
    void connectsAndRecordsConnectionTime() throws Exception {
        try (NetworkTestSupport.Session session = NetworkTestSupport.connect(data -> {}, data -> {})) {
            assertTrue(session.client.isConnected(), "Client is not connected");
            assertTrue(session.server.isConnected(), "Server is not connected");

            double connectionTimeMillis = session.connectionTimeNanos / 1_000_000.0;
            NetworkTestSupport.writeReport("connection.txt", String.format(Locale.ROOT,
                    "Result: PASS%n"
                    + "Connection: localhost client -> server%n"
                    + "Connection acceptance time: %.3f ms%n",
                    connectionTimeMillis));
            System.out.printf(Locale.ROOT, "Network connection accepted in %.3f ms%n",
                    connectionTimeMillis);
        }
    }
}
