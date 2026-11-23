package tetris.onlinemulti.network;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// PING 송신과 PONG 수신 시각으로 왕복 지연(RTT)을 ms 단위로 계산한다.
public final class PingLatency {
    public static final long UNKNOWN_MS = -1L;

    private final AtomicLong lastPingSentNanos = new AtomicLong(UNKNOWN_MS);
    private final AtomicLong lastLatencyMs = new AtomicLong(UNKNOWN_MS);

    // PING을 보낸 시각을 기록한다.
    public void markPingSent() {
        lastPingSentNanos.set(System.nanoTime());
    }

    // PONG을 받은 시각으로 RTT(ms)를 갱신하고 반환한다.
    public long markPongReceived() {
        long sentNanos = lastPingSentNanos.getAndSet(UNKNOWN_MS);
        if (sentNanos < 0) {
            return lastLatencyMs.get();
        }
        long latencyMs = nanosToMillis(System.nanoTime() - sentNanos);
        lastLatencyMs.set(latencyMs);
        return latencyMs;
    }

    // 가장 최근 측정한 ping(ms)을 반환한다. 아직 없으면 -1.
    public long getLatencyMs() {
        return lastLatencyMs.get();
    }

    // 게임 UI용 문자열 (예: "42 ms", 미측정 시 "-- ms").
    public String formatLatency() {
        long latencyMs = lastLatencyMs.get();
        if (latencyMs < 0) {
            return "-- ms";
        }
        return latencyMs + " ms";
    }

    public void reset() {
        lastPingSentNanos.set(UNKNOWN_MS);
        lastLatencyMs.set(UNKNOWN_MS);
    }

    // 나노초 구간을 ms로 변환한다. 음수 구간은 0으로 보정한다.
    public static long nanosToMillis(long elapsedNanos) {
        if (elapsedNanos <= 0) {
            return 0L;
        }
        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
    }
}
