package tetris.onlinemulti.network;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

// 송수신 지연, 상대의 응답 확인, 핑퐁 여부를 독립적으로 검사한다.
public final class ConnectionMonitor {
    // 나중에 조정하기 쉽도록 지연 시간 기준값을 메서드가 아닌 이 위치에 명시한다.
    private static final long DEFAULT_DEGRADED_LATENCY_MILLIS = 100;
    private static final long DEFAULT_DISCONNECTED_LATENCY_MILLIS = 500;
    private static final long DEFAULT_ACK_DEGRADED_MILLIS = 100;
    private static final long DEFAULT_ACK_TIMEOUT_MILLIS = 500;
    private static final long DEFAULT_PINGPONG_TIMEOUT_MILLIS = 5_000;

    private final LongSupplier clock;
    private final long degradedLatencyNanos;
    private final long disconnectedLatencyNanos;
    private final long ackDegradedNanos;
    private final long ackTimeoutNanos;
    private final long pingPongTimeoutNanos;
    private final Deque<Long> pendingPingSentNanos = new ArrayDeque<>();

    private volatile long latestLatencyNanos;
    private volatile long latestPingRttNanos = -1;
    private volatile long lastPingPongNanos;
    private volatile long pendingDataSentNanos;
    private volatile boolean pendingAcknowledgement;
    private volatile boolean peerAcknowledged = true;
    private volatile boolean pingPongActive;

    public ConnectionMonitor() {
        this(System::nanoTime,
                DEFAULT_DEGRADED_LATENCY_MILLIS,
                DEFAULT_DISCONNECTED_LATENCY_MILLIS,
                DEFAULT_ACK_DEGRADED_MILLIS,
                DEFAULT_ACK_TIMEOUT_MILLIS,
                DEFAULT_PINGPONG_TIMEOUT_MILLIS);
    }

    ConnectionMonitor(LongSupplier clock,
            long degradedLatencyMillis,
            long disconnectedLatencyMillis,
            long ackDegradedMillis,
            long ackTimeoutMillis,
            long pingPongTimeoutMillis) {
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        if (degradedLatencyMillis <= 0
                || disconnectedLatencyMillis <= degradedLatencyMillis
                || ackDegradedMillis <= 0
                || ackTimeoutMillis <= ackDegradedMillis
                || pingPongTimeoutMillis <= 0) {
            throw new IllegalArgumentException("Connection thresholds are invalid.");
        }
        this.degradedLatencyNanos = TimeUnit.MILLISECONDS.toNanos(degradedLatencyMillis);
        this.disconnectedLatencyNanos = TimeUnit.MILLISECONDS.toNanos(disconnectedLatencyMillis);
        this.ackDegradedNanos = TimeUnit.MILLISECONDS.toNanos(ackDegradedMillis);
        this.ackTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(ackTimeoutMillis);
        this.pingPongTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(pingPongTimeoutMillis);
        this.lastPingPongNanos = clock.getAsLong();
        this.pingPongActive = true;
    }

    // 응답 확인이 필요한 데이터의 전송 시점을 기록한다.
    public synchronized void recordDataSent() {
        pendingDataSentNanos = clock.getAsLong();
        pendingAcknowledgement = true;
        peerAcknowledged = false;
        latestLatencyNanos = 0;
    }

    // 상대의 응답 확인을 수신했을 때 전송부터 응답까지의 지연 시간을 계산한다.
    public synchronized ConnectionStatus recordPeerAcknowledgement() {
        long now = clock.getAsLong();
        if (pendingDataSentNanos != 0) {
            latestLatencyNanos = now - pendingDataSentNanos;
        }
        pendingAcknowledgement = false;
        pendingDataSentNanos = 0;
        peerAcknowledged = true;
        return checkLatency(latestLatencyNanos);
    }

    // PING을 보낸 시점을 기록한다.
    public synchronized void recordPingSent() {
        pendingPingSentNanos.addLast(clock.getAsLong());
    }

    // PONG을 받으면 가장 오래된 PING의 왕복 시간을 기록한다.
    public synchronized void recordPingPong() {
        long now = clock.getAsLong();
        if (!pendingPingSentNanos.isEmpty()) {
            latestPingRttNanos = now - pendingPingSentNanos.removeFirst();
        }
        pingPongActive = true;
        lastPingPongNanos = now;
    }

    // 보내고 상대가 응답할 때까지 걸린 시간을 별도로 검사한다.
    public ConnectionStatus checkLatency(long latencyNanos) {
        if (latencyNanos < 0) {
            throw new IllegalArgumentException("Latency cannot be negative.");
        }
        if (latencyNanos >= disconnectedLatencyNanos) {
            return ConnectionStatus.DISCONNECTED;
        }
        if (latencyNanos >= degradedLatencyNanos) {
            return ConnectionStatus.DEGRADED;
        }
        return ConnectionStatus.GOOD;
    }

    // 상대가 내 데이터를 받았는지 응답 확인 여부와 응답 제한 시간을 검사한다.
    public synchronized ConnectionStatus checkPeerAcknowledgement(boolean acknowledgementReceived,
            long elapsedSinceSendNanos) {
        if (elapsedSinceSendNanos < 0) {
            throw new IllegalArgumentException("Elapsed time cannot be negative.");
        }
        if (acknowledgementReceived) {
            peerAcknowledged = true;
            return checkLatency(latestLatencyNanos);
        }
        peerAcknowledged = false;
        if (elapsedSinceSendNanos >= ackTimeoutNanos) {
            return ConnectionStatus.DISCONNECTED;
        }
        if (elapsedSinceSendNanos >= ackDegradedNanos) {
            return ConnectionStatus.DEGRADED;
        }
        return ConnectionStatus.GOOD;
    }

    // 핑퐁 메시지가 제한 시간 안에 왕복했는지 검사한다.
    public synchronized ConnectionStatus checkPingPong(boolean pingPongActive,
            long elapsedSinceLastPingPongNanos) {
        if (elapsedSinceLastPingPongNanos < 0) {
            throw new IllegalArgumentException("Elapsed time cannot be negative.");
        }
        this.pingPongActive = pingPongActive;
        if (!pingPongActive || elapsedSinceLastPingPongNanos >= pingPongTimeoutNanos) {
            return ConnectionStatus.DISCONNECTED;
        }
        return ConnectionStatus.GOOD;
    }

    // 세 판단을 한 번에 합산하며 가장 심각한 상태를 우선 반환한다.
    public synchronized ConnectionStatus evaluateNow() {
        long now = clock.getAsLong();
        ConnectionStatus latencyStatus = checkLatency(latestLatencyNanos);
        ConnectionStatus acknowledgementStatus =
                checkPeerAcknowledgement(peerAcknowledged,
                        pendingAcknowledgement ? now - pendingDataSentNanos : 0);
        ConnectionStatus pingPongStatus =
                checkPingPong(pingPongActive, now - lastPingPongNanos);

        if (latencyStatus == ConnectionStatus.DISCONNECTED
                || acknowledgementStatus == ConnectionStatus.DISCONNECTED
                || pingPongStatus == ConnectionStatus.DISCONNECTED) {
            return ConnectionStatus.DISCONNECTED;
        }
        if (latencyStatus == ConnectionStatus.DEGRADED
                || acknowledgementStatus == ConnectionStatus.DEGRADED
                || pingPongStatus == ConnectionStatus.DEGRADED) {
            return ConnectionStatus.DEGRADED;
        }
        return ConnectionStatus.GOOD;
    }

    // RTT time 반환
    public synchronized long GetPingTime() {
        return latestPingRttNanos < 0
                ? -1
                : TimeUnit.NANOSECONDS.toMillis(latestPingRttNanos);
    }
}
