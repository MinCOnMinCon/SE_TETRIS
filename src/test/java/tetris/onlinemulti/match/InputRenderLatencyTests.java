package tetris.onlinemulti.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;

import tetris.onlinemulti.network.InputRenderLatency;

class InputRenderLatencyTests {
    // 입력 1~3 중 2번 ACK의 추정값이 음수일 때 1번으로 대체하지 않는지 확인한다.
    // 1번은 정리되고, 3번은 ACK가 오면 한 번만 측정되는지 검증한다.
    @Test
    void measuresOnlyTheMatchingInputAndKeepsNewerInputs() {
        InputRenderLatency latency = new InputRenderLatency();
        latency.recordInput(1_000_000);
        latency.recordInput(11_000_000);
        latency.recordInput(21_000_000);
        assertEquals(3, latency.getLastInputId());

        latency.onRenderAck(2, 26_000_000, 40);
        assertTrue(latency.getLatestLatencyMs().isEmpty());

        latency.onRenderAck(1, 40_000_000, 40);
        assertTrue(latency.getLatestLatencyMs().isEmpty());

        latency.onRenderAck(3, 91_000_000, 40);
        assertEquals(OptionalDouble.of(50), latency.getLatestLatencyMs());

        latency.onRenderAck(3, 101_000_000, 40);
        assertEquals(OptionalDouble.of(50), latency.getLatestLatencyMs());
    }

    // 새 번호로 그린 경우만 ACK를 보내도록 0번, 중복 번호, 이전 번호를 확인한다.
    @Test
    void requestsAnAckOnlyForANewerRenderedInput() {
        InputRenderLatency latency = new InputRenderLatency();
        assertFalse(latency.shouldSendRenderAck(0));
        assertTrue(latency.shouldSendRenderAck(3));
        assertFalse(latency.shouldSendRenderAck(3));
        assertFalse(latency.shouldSendRenderAck(2));
        assertTrue(latency.shouldSendRenderAck(4));
    }

    // RTT 미측정, 5초가 지난 ACK, 일시정지로 정리한 입력은 샘플을 만들지 않는다.
    @Test
    void skipsInputsWithoutAUsableMeasurement() {
        InputRenderLatency latency = new InputRenderLatency();
        latency.recordInput(0);
        latency.onRenderAck(1, 40_000_000, -1);
        latency.recordInput(50_000_000);
        latency.onRenderAck(2, 5_050_000_000L, 40);
        latency.recordInput(5_060_000_000L);
        latency.clearPendingInputs();
        latency.onRenderAck(3, 5_100_000_000L, 40);

        assertEquals(3, latency.getLastInputId());
        assertTrue(latency.getLatestLatencyMs().isEmpty());
    }
}
