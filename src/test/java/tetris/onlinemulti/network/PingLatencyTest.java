package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PingLatencyTest {
    @Test
    void formatsUnknownLatency() {
        PingLatency ping = new PingLatency();
        assertEquals(PingLatency.UNKNOWN_MS, ping.getLatencyMs());
        assertEquals("-- ms", ping.formatLatency());
    }

    @Test
    void measuresRoundTripInMillis() throws InterruptedException {
        PingLatency ping = new PingLatency();
        ping.markPingSent();
        Thread.sleep(25);
        long latencyMs = ping.markPongReceived();

        assertTrue(latencyMs >= 20, "latencyMs=" + latencyMs);
        assertEquals(latencyMs, ping.getLatencyMs());
        assertEquals(latencyMs + " ms", ping.formatLatency());
    }

    @Test
    void nanosToMillisConvertsAndClampsNegative() {
        assertEquals(0L, PingLatency.nanosToMillis(0));
        assertEquals(0L, PingLatency.nanosToMillis(-100));
        assertEquals(2L, PingLatency.nanosToMillis(2_000_000L));
    }
}
