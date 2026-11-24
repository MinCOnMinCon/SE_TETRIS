package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class ConnectionMonitorTest {
    @Test
    void checksLatencyThresholdsSeparately() {
        ConnectionMonitor monitor = monitorWith(new AtomicLong(0));

        assertEquals(ConnectionStatus.GOOD, monitor.checkLatency(99_000_000));
        assertEquals(ConnectionStatus.DEGRADED, monitor.checkLatency(100_000_000));
        assertEquals(ConnectionStatus.DISCONNECTED, monitor.checkLatency(500_000_000));
    }

    @Test
    void checksPeerAcknowledgementTimeout() {
        ConnectionMonitor monitor = monitorWith(new AtomicLong(0));

        assertEquals(ConnectionStatus.DEGRADED,
                monitor.checkPeerAcknowledgement(false, 100_000_000));
        assertEquals(ConnectionStatus.GOOD,
                monitor.checkPeerAcknowledgement(false, 99_999_999));
        assertEquals(ConnectionStatus.DEGRADED,
                monitor.checkPeerAcknowledgement(false, 499_000_000));
        assertEquals(ConnectionStatus.DISCONNECTED,
                monitor.checkPeerAcknowledgement(false, 500_000_000));
        assertEquals(ConnectionStatus.GOOD,
                monitor.checkPeerAcknowledgement(true, 1));
    }

    @Test
    void checksPingPongContinuity() {
        ConnectionMonitor monitor = monitorWith(new AtomicLong(0));

        assertEquals(ConnectionStatus.GOOD,
                monitor.checkPingPong(true, 4_999_000_000L));
        assertEquals(ConnectionStatus.DISCONNECTED,
                monitor.checkPingPong(false, 0));
        assertEquals(ConnectionStatus.DISCONNECTED,
                monitor.checkPingPong(true, 5_000_000_000L));
    }

    @Test
    void measuresPingRoundTripInMilliseconds() {
        AtomicLong clock = new AtomicLong(0);
        ConnectionMonitor monitor = monitorWith(clock);

        assertEquals(-1, monitor.GetPingTime());
        monitor.recordPingSent();
        clock.set(42_500_000L);
        monitor.recordPingPong();

        assertEquals(42, monitor.GetPingTime());
    }

    @Test
    void matchesPongsToPingSendOrder() {
        AtomicLong clock = new AtomicLong(0);
        ConnectionMonitor monitor = monitorWith(clock);

        monitor.recordPingSent();
        clock.set(10_000_000L);
        monitor.recordPingSent();
        clock.set(30_000_000L);
        monitor.recordPingPong();
        assertEquals(30, monitor.GetPingTime());

        clock.set(35_000_000L);
        monitor.recordPingPong();
        assertEquals(25, monitor.GetPingTime());
    }

    @Test
    void combinesStatusesAndPrioritizesDisconnected() {
        AtomicLong clock = new AtomicLong(0);
        ConnectionMonitor monitor = monitorWith(clock);

        monitor.recordPingPong();
        monitor.recordDataSent();
        clock.set(700_000_000L);

        assertEquals(ConnectionStatus.DISCONNECTED, monitor.evaluateNow());
    }

    private static ConnectionMonitor monitorWith(AtomicLong clock) {
        return new ConnectionMonitor(clock::get,
                100, 500, 100, 500, 5_000);
    }
}
