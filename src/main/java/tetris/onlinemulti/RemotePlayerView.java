package tetris.onlinemulti;

import java.util.Objects;

import javafx.scene.Parent;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderer;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.settings.GameSettings.PlayMode;

// 경기마다 생성하고 JavaFX 스레드에서 호출하며, 전달한 수신 상태는 이후 변경하지 않는다.
public final class RemotePlayerView {
    private static final int LINE_CLEAR_EFFECT_DURATION_MS = 1000;

    private final SceneRenderer renderer;
    private final Parent root;
    private SceneRenderState state;
    
    private boolean lineClearEffectRunning;
    private double effectRemainingMs;
    private int[] clearedRows = new int[0];
    private long lastEffectId = -1;

    // 온라인용 렌더러를 만들고 상대의 초기 상태로 화면을 구성한다.
    public RemotePlayerView(RenderConfig config, SceneRenderState initialState) {
        state = Objects.requireNonNull(initialState, "initialState must not be null");
        renderer = new SceneRenderer(Objects.requireNonNull(config, "config must not be null"), PlayMode.ONLINE);
        root = renderer.CreateRoot(state);
    }

    // 이펙트 중에는 수신 화면을 버리고, 새 ID의 이펙트는 내부에서 1초로 시작한다.
    public void ApplyState(SceneRenderState state, long effectId, boolean effectRunning, int[] clearedRows) {
        if (lineClearEffectRunning) {
            return;
        }
        // 종료된 이펙트의 지연 메시지는 보드까지 적용하지 않아 이전 화면으로 돌아가지 않는다.
        if (effectRunning && effectId == lastEffectId) {
            return;
        }

        Objects.requireNonNull(state, "state must not be null");
        if (effectRunning) {
            Objects.requireNonNull(clearedRows, "clearedRows must not be null during an effect");
        }

        lastEffectId = effectId;
        this.state = state;
        
        lineClearEffectRunning = effectRunning;
        effectRemainingMs = effectRunning ? LINE_CLEAR_EFFECT_DURATION_MS : 0;
        this.clearedRows = effectRunning ? clearedRows.clone() : new int[0];
    }

    // 새 상태를 그리고 이펙트 시간을 진행하되, 종료 후에는 다음 수신 전까지 화면을 유지한다.
    public void Update(double deltaTimeMs) {
        if (deltaTimeMs < 0) {
            throw new IllegalArgumentException("deltaTimeMs must be finite and not negative");
        }
        if (!lineClearEffectRunning) {
            renderer.UpdateRoot(state);
            return;
        }

        effectRemainingMs -= deltaTimeMs;
        int elapsedMs = (int) (LINE_CLEAR_EFFECT_DURATION_MS - effectRemainingMs);
        renderer.DrawLineClearEffect(clearedRows, elapsedMs);
        lineClearEffectRunning = effectRemainingMs > 0;
    }

    // VersusRenderer에 배치할 상대 화면 루트를 반환한다.
    public Parent GetRoot() {
        return root;
    }
}
