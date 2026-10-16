package tetris.pausescene;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PauseSceneTest {
    @Test
    void requiresBothPauseActions() {
        assertThrows(NullPointerException.class, () -> new PauseScene(null, () -> {}));
        assertThrows(NullPointerException.class, () -> new PauseScene(() -> {}, null));
    }
}
