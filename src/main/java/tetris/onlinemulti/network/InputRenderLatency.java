package tetris.onlinemulti.network;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

// 각 경기의 JavaFX 스레드에서 입력과 렌더링 확인을 추적한다.
public class InputRenderLatency {
    private static final long ACK_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);

    private final Map<Long, Long> pendingInputs = new LinkedHashMap<>();
    private long lastInputId;
    private long lastAckId;
    private OptionalDouble latestLatencyMs = OptionalDouble.empty();

    // 입력했을 때 시간 기록
    public void recordInput(long inputNanos) {
        pendingInputs.values().removeIf(sentNanos -> inputNanos - sentNanos >= ACK_TIMEOUT_NANOS);
        pendingInputs.put(++lastInputId, inputNanos);
    }

    public long getLastInputId() {
        return lastInputId;
    }

    // remotePlayerView에서 이번 화면 업데이트를 위해 받은 데이터의 id가 최신인지 확인하고 최신이면 그걸로 갱신하고 ack 보내라고 리턴
    public boolean shouldSendRenderAck(long renderedInputId) {
        if (renderedInputId <= lastAckId) {
            return false;
        }
        lastAckId = renderedInputId;
        return true;
    }
    // ACK 번호와 일치하는 입력의 지연만 계산한다.
    public void onRenderAck(long ackId, long receivedNanos, long pingRttMs) {
        if (ackId <= 0 || ackId > lastInputId) {
            return;
        }
        var pending = pendingInputs.entrySet().iterator();
        while (pending.hasNext()) {
            var input = pending.next();
            long key = input.getKey();
            if (key > ackId) {
                break;
            } else if (key < ackId) {
                pending.remove();
            } else {
                long elapsedNanos = receivedNanos - input.getValue();
                double latencyMs = elapsedNanos / 1_000_000.0 - pingRttMs / 2.0;
                if (pingRttMs >= 0  && latencyMs >= 0) {
                    latestLatencyMs = OptionalDouble.of(latencyMs); // latencyMs를 최신 레이턴시 ms로 지정
                }
                latestLatencyMs.ifPresent(value -> System.out.printf("입력 반영 지연: %.2f ms%n", value));
                pending.remove();
                break;
            }
        }
    }

    public OptionalDouble getLatestLatencyMs() {
        return latestLatencyMs;
    }

    public void clearPendingInputs() {
        pendingInputs.clear();
    }
}
