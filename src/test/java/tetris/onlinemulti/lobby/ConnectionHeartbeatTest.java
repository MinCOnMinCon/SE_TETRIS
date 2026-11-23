package tetris.onlinemulti.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import tetris.onlinemulti.network.PingLatency;

class ConnectionHeartbeatTest {
    @Test
    void answersPingAndIgnoresNonHeartbeatMessages() {
        AtomicInteger pongsSent = new AtomicInteger();
        ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                () -> {}, pongsSent::incrementAndGet, () -> {}, 100, 500);
        try {
            assertTrue(heartbeat.handleMessage("PING"));
            assertEquals(1, pongsSent.get());
            assertTrue(heartbeat.handleMessage("PONG"));
            assertFalse(heartbeat.handleMessage("CHAT hello"));
        } finally {
            heartbeat.close();
        }
    }

    @Test
    void timesOutWhenPeerDoesNotRespond() throws InterruptedException {
        CountDownLatch timedOut = new CountDownLatch(1);
        AtomicInteger pingsSent = new AtomicInteger();
        ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                pingsSent::incrementAndGet, () -> {}, timedOut::countDown, 10, 70);
        try {
            heartbeat.start();
            assertTrue(timedOut.await(1, TimeUnit.SECONDS));
            assertTrue(pingsSent.get() > 0);
        } finally {
            heartbeat.close();
        }
    }

    @Test
    void measuresLatencyWhenPongArrives() throws InterruptedException {
        PingLatency pingLatency = new PingLatency();
        AtomicReference<String> latencyText = new AtomicReference<>();
        ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                () -> {}, () -> {}, () -> {}, 20, 200, pingLatency, latencyText::set);
        try {
            heartbeat.start();
            Thread.sleep(40);
            assertTrue(heartbeat.handleMessage("PONG"));
            assertTrue(pingLatency.getLatencyMs() >= 0);
            assertEquals(pingLatency.formatLatency(), latencyText.get());
        } finally {
            heartbeat.close();
        }
    }

    @Test
    void pongResponsesKeepConnectionAlive() throws InterruptedException {
        CountDownLatch timedOut = new CountDownLatch(1);
        AtomicInteger pingsSent = new AtomicInteger();
        ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                pingsSent::incrementAndGet, () -> {}, timedOut::countDown, 10, 70);
        ScheduledExecutorService peer = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> responses = peer.scheduleAtFixedRate(
                () -> heartbeat.handleMessage("PONG"), 0, 20, TimeUnit.MILLISECONDS);
        try {
            heartbeat.start();
            assertFalse(timedOut.await(200, TimeUnit.MILLISECONDS));
            assertTrue(pingsSent.get() > 0);
        } finally {
            responses.cancel(true);
            peer.shutdownNow();
            heartbeat.close();
        }
    }
}
