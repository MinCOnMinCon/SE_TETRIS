package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class ColorTypeConverterTest {
    @Test
    void convertsEnumUsingTheSelectedColorMode() {
        assertEquals(Color.CYAN, ColorTypeConverter.ConvertEnumToColor(ColorType.BlockI, 0));
        assertEquals(Color.rgb(135, 206, 235),
                ColorTypeConverter.ConvertEnumToColor(ColorType.BlockI, 1));
        assertEquals(Color.BLUE, ColorTypeConverter.ConvertEnumToColor(ColorType.BlockI, 2));
        assertEquals(Color.BLACK,
                ColorTypeConverter.ConvertEnumToColor(ColorType.NONE, 0));
    }
}
