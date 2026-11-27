package tetris.onlinemulti.lobby;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import tetris.onlinemulti.network.PingLatency;

final class ConnectionHeartbeat implements AutoCloseable {
    private static final String PING = "PING";
    private static final String PONG = "PONG";
    private static final long DEFAULT_INTERVAL_MILLIS = 1_000;
    private static final long DEFAULT_TIMEOUT_MILLIS = 5_000;

    private final Runnable sendPing;
    private final Runnable sendPong;
    private final Runnable onTimeout;
    private final PingLatency pingLatency;
    private final Consumer<String> onLatencyText;
    private final long intervalMillis;
    private final long timeoutNanos;
    private final AtomicLong lastPongNanos = new AtomicLong(System.nanoTime());
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, task -> {
        Thread thread = new Thread(task, "online-connection-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    ConnectionHeartbeat(Runnable sendPing, Runnable sendPong, Runnable onTimeout) {
        this(sendPing, sendPong, onTimeout, DEFAULT_INTERVAL_MILLIS, DEFAULT_TIMEOUT_MILLIS, null, null);
    }

    ConnectionHeartbeat(Runnable sendPing, Runnable sendPong, Runnable onTimeout,
            long intervalMillis, long timeoutMillis) {
        this(sendPing, sendPong, onTimeout, intervalMillis, timeoutMillis, null, null);
    }

    ConnectionHeartbeat(Runnable sendPing, Runnable sendPong, Runnable onTimeout,
            PingLatency pingLatency, Consumer<String> onLatencyText) {
        this(sendPing, sendPong, onTimeout, DEFAULT_INTERVAL_MILLIS, DEFAULT_TIMEOUT_MILLIS,
                pingLatency, onLatencyText);
    }

    ConnectionHeartbeat(Runnable sendPing, Runnable sendPong, Runnable onTimeout,
            long intervalMillis, long timeoutMillis,
            PingLatency pingLatency, Consumer<String> onLatencyText) {
        this.sendPing = Objects.requireNonNull(sendPing, "sendPing cannot be null");
        this.sendPong = Objects.requireNonNull(sendPong, "sendPong cannot be null");
        this.onTimeout = Objects.requireNonNull(onTimeout, "onTimeout cannot be null");
        if (intervalMillis <= 0 || timeoutMillis <= intervalMillis) {
            throw new IllegalArgumentException("Invalid heartbeat interval or timeout");
        }
        this.intervalMillis = intervalMillis;
        this.timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        this.pingLatency = pingLatency;
        this.onLatencyText = onLatencyText;
    }

    synchronized void start() {
        if (!started.compareAndSet(false, true) || closed.get()) {
            return;
        }
        long timeoutCheckIntervalMillis = Math.min(intervalMillis, 100);
        scheduler.scheduleAtFixedRate(this::checkTimeout,
                timeoutCheckIntervalMillis, timeoutCheckIntervalMillis, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::sendPing,
                intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    boolean handleMessage(String message) {
        if (closed.get()) {
            return false;
        }
        if (PING.equals(message)) {
            sendPong.run();
            return true;
        }
        if (PONG.equals(message)) {
            lastPongNanos.set(System.nanoTime());
            if (pingLatency != null) {
                pingLatency.markPongReceived();
                if (onLatencyText != null) {
                    onLatencyText.accept(pingLatency.formatLatency());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized void close() {
        if (closed.compareAndSet(false, true)) {
            scheduler.shutdownNow();
        }
    }

    private void checkTimeout() {
        if (closed.get()) {
            return;
        }
        if (System.nanoTime() - lastPongNanos.get() >= timeoutNanos) {
            if (closed.compareAndSet(false, true)) {
                scheduler.shutdownNow();
                onTimeout.run();
            }
        }
    }

    private void sendPing() {
        if (closed.get()) {
            return;
        }
        if (pingLatency != null) {
            pingLatency.markPingSent();
        }
        sendPing.run();
    }
}
