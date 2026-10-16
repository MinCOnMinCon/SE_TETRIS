package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RenderConfigTest {
    @Test
    void calculatesQueueCanvasHeightFromSlotCount() {
        RenderConfig config = new RenderConfig(2);

        assertEquals(2, config.blockQueueDisplayCount);
        assertEquals(config.panelTitleHeight + config.panelBottomPadding
                + 2 * config.previewSlotHeight + config.previewSlotGap,
                config.blockQueueCanvasHeight);
    }
}
