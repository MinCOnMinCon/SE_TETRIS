package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BlockTTest {
    @Test
    void createsFourCellTBlock() {
        assertEquals(4, countOccupied(new BlockT(0).GetBlockData().GetShape()));
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
