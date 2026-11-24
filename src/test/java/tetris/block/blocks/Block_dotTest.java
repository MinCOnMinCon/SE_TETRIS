package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Block_dotTest {
    @Test
    void createsSingleCellItemBlockAtItsSpawnColumn() {
        var data = new Block().GetDotBlock();

        assertEquals(4, data.GetX());
        assertEquals(0, data.GetY());
        assertTrue(data.GetShape()[0][0]);
    }
}
