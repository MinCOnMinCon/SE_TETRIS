package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class Block4VLinesTest {
    @Test
    void createsTheExpectedFourVShapeAtTheDefaultSpawnPosition() {
        var data = new Block().GetWeightBlock();

        assertArrayEquals(new boolean[] {false, true, true, false}, data.GetShape()[0]);
        assertArrayEquals(new boolean[] {true, true, true, true}, data.GetShape()[1]);
        assertEquals(3, data.GetX());
        assertEquals(0, data.GetY());
    }

    @Test
    void usesGrayForWeightBlock() {
        assertSame(Color.GRAY, new Block().GetWeightBlock().GetColor());
    }
}
