package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class Block_item4VTest {
    @Test
    void createsTheExpectedFourVShapeAtTheDefaultSpawnPosition() {
        var data = new Block_item4V(0).GetBlockData();

        assertArrayEquals(new boolean[] {false, true, true, false}, data.GetShape()[0]);
        assertArrayEquals(new boolean[] {true, true, true, true}, data.GetShape()[1]);
        assertEquals(3, data.GetX());
        assertEquals(0, data.GetY());
    }

    @Test
    void usesWhiteForEveryColorMode() {
        for (int colorMode = 0; colorMode <= 2; colorMode++) {
            assertSame(Color.WHITE, new Block_item4V(colorMode).GetBlockData().GetCurrentColor());
        }
    }
}
