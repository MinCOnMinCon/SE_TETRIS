package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BlockJTest {
    @Test
    void createsFourCellJBlock() {
        assertEquals(4, countOccupied(new BlockJ(0).GetBlockData().GetShape()));
    }

    private static int countOccupied(boolean[][] shape) {
        int count = 0;
        for (boolean[] row : shape) {
            for (boolean cell : row) {
                if (cell) {
                    count++;
                }
            }
        }
        return count;
    }
}
