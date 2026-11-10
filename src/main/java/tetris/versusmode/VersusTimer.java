package tetris.versusmode;

import java.util.Objects;

public class VersusTimer {
    // 기본 제한시간: 180초.
    private static final double TIME_LIMIT_MS = 180_000;

    private final Runnable onTimeExpired;
    private double remainingTimeMs = TIME_LIMIT_MS;
    private boolean expired;

    public VersusTimer(Runnable onTimeExpired) {
        this.onTimeExpired = Objects.requireNonNull(onTimeExpired);
    }

    public double GetRemainingTimeMs() {
        return remainingTimeMs;
    }

    // 관리자가 진행 중인 프레임의 경과 시간만 밀리초 단위로 전달한다.
    public void Update(double deltaTimeMs) {
        if (deltaTimeMs < 0) {
            throw new IllegalArgumentException("경과 시간은 0 이상의 값이어야 합니다.");
        }
        if (expired) return;

        remainingTimeMs = Math.max(0, remainingTimeMs - deltaTimeMs);
        if (remainingTimeMs == 0) {
            expired = true;
            onTimeExpired.run();
        }
    }
}
