package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tetris.gamescene.SceneRender.RenderConfig;
import tetris.settings.GameSettings;

class RenderConfigTest {
    @Test
    void calculatesQueueCanvasHeightFromSlotCount() {
        GameSettings settings = new GameSettings(
                "1280x720", 1280, 720, 0, java.util.Map.of(), GameSettings.Difficulty.NORMAL);
        RenderConfig config = new RenderConfig(settings, 2);

        assertEquals(2, config.blockQueueDisplayCount);
        assertEquals(config.panelTitleHeight + config.panelBottomPadding
                + 2 * config.previewSlotHeight + config.previewSlotGap,
                config.blockQueueCanvasHeight);
    }
}
