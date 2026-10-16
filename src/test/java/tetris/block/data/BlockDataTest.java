package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import tetris.block.data.CurrentBlock.ItemType;
import org.junit.jupiter.api.Test;

class BlockDataTest {
    @Test
    void tracksShapePositionColorAndItemProperties() {
        boolean[][] shape = {{true, false}};
        ColorPalette palette = new ColorPalette(Color.RED, Color.GREEN, Color.BLUE);
        BlockData data = new BlockData(shape, palette, 2, 4, 5, false, ItemType.NORMAL, null);

        assertSame(shape, data.GetShape());
        assertEquals(Color.BLUE, data.GetCurrentColor());
        assertEquals(4, data.GetX());
        assertEquals(5, data.GetY());
        assertFalse(data.IsItem());
        data.SetX(8);
        data.SetY(9);
        data.SetItem(true);
        data.SetItemType(ItemType.DEL_HLINE);
        data.SetItemPos(new int[] {2, 3});

        assertEquals(8, data.GetX());
        assertEquals(9, data.GetY());
        assertTrue(data.IsItem());
        assertEquals(ItemType.DEL_HLINE, data.GetItemType());
        assertArrayEquals(new int[] {2, 3}, data.GetItemPos());
    }
}
