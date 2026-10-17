package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import tetris.gamescene.SceneRender.RenderConfig;
import tetris.gamescene.SceneRender.SceneRenderer;
import tetris.settings.GameSettings;

class SceneRendererTest {
    @Test
    void acceptsRenderConfigAndRejectsNullConfig() {
        GameSettings settings = new GameSettings(
                "1280x720", 1280, 720, 0, java.util.Map.of(), GameSettings.Difficulty.NORMAL);
        assertDoesNotThrow(() -> new SceneRenderer(new RenderConfig(settings, 2)));
        assertThrows(NullPointerException.class, () -> new SceneRenderer(null));
    }
}
