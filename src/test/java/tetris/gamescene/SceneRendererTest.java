package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SceneRendererTest {
    @Test
    void acceptsRenderConfigAndRejectsNullConfig() {
        assertDoesNotThrow(() -> new SceneRenderer(new RenderConfig(2)));
        assertThrows(NullPointerException.class, () -> new SceneRenderer(null));
    }
}
