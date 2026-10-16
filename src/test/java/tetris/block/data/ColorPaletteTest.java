package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class ColorPaletteTest {
    @Test
    void returnsColorForEachColorBlindMode() {
        ColorPalette palette = new ColorPalette(Color.CYAN, Color.ORANGE, Color.BLUE);

        assertEquals(Color.CYAN, palette.GetColor(0));
        assertEquals(Color.ORANGE, palette.GetColor(1));
        assertEquals(Color.BLUE, palette.GetColor(2));
        assertEquals(Color.CYAN, palette.GetColor(99));
    }
}
