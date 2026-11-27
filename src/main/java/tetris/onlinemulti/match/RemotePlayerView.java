package tetris.onlinemulti.match;

import java.util.Objects;

import javafx.scene.Parent;
import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderer;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.settings.GameSettings.PlayMode;

// 경기마다 생성하고 JavaFX 스레드에서 수신 상태를 적용한 뒤 화면에 그린다.
public final class RemotePlayerView {
    private static final int LINE_CLEAR_EFFECT_DURATION_MS = 1000;

    private final SceneRenderer renderer;
    private final Parent root;
    private final int colorMode;
    private SceneRenderState state;
    private long stateInputId;
    
    private boolean lineClearEffectRunning;
    private double effectRemainingMs;
    private int[] clearedRows = new int[0];
    private long lastEffectId = -1;

    // 온라인용 렌더러를 만들고 상대의 초기 상태로 화면을 구성한다.
    public RemotePlayerView(RenderConfig config, SceneRenderState initialState) {
        state = Objects.requireNonNull(initialState, "initialState must not be null");
        renderer = new SceneRenderer(Objects.requireNonNull(config, "config must not be null"), PlayMode.ONLINE);
        colorMode = config.colorMode;
        root = renderer.CreateRoot(state);
    }

    // 이펙트 중에는 수신 화면을 버리고, 새 ID의 이펙트는 내부에서 1초로 시작한다.
    public void ApplyState(SceneRenderState state, long effectId, boolean effectRunning, int[] clearedRows,
            long inputId) {
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


        // 상대의 블럭을 내 컬러모드에 맞게 색칠하기 위해 블럭에 컬러모드를 내껄로 다 설정한다.
        for (BoardElement[] row : state.board()) {
            for (BoardElement element : row) {
                element.setElementColorMode(colorMode);
            }
        }
        BlockData currentBlock = state.currentBlock();
        if (currentBlock != null) {
            currentBlock.SetColorMode(colorMode);
        }
        BlockData[] nextBlocks = state.blockQueue();
        if (nextBlocks != null) {
            for (BlockData nextBlock : nextBlocks) {
                if (nextBlock != null) {
                    nextBlock.SetColorMode(colorMode);
                }
            }
        }
        BlockData heldBlock = state.blockHolding();
        if (heldBlock != null) {
            heldBlock.SetColorMode(colorMode);
        }

        lastEffectId = effectId;
        this.state = state;
        stateInputId = inputId;
        
        lineClearEffectRunning = effectRunning;
        effectRemainingMs = effectRunning ? LINE_CLEAR_EFFECT_DURATION_MS : 0;
        this.clearedRows = effectRunning ? clearedRows.clone() : new int[0];
    }

    // 새 상태를 그리고 이펙트 시간을 진행하되, 종료 후에는 다음 수신 전까지 화면을 유지한다.
    public long Update(double deltaTimeMs) {
        if (deltaTimeMs < 0) {
            throw new IllegalArgumentException("deltaTimeMs must be finite and not negative");
        }
        if (!lineClearEffectRunning) {
            renderer.UpdateRoot(state);
            return stateInputId;
        }

        effectRemainingMs -= deltaTimeMs;
        int elapsedMs = (int) (LINE_CLEAR_EFFECT_DURATION_MS - effectRemainingMs);
        renderer.DrawLineClearEffect(clearedRows, elapsedMs);
        lineClearEffectRunning = effectRemainingMs > 0;
        return 0;
    }

    // VersusRenderer에 배치할 상대 화면 루트를 반환한다.
    public Parent GetRoot() {
        return root;
    }
}
