package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BlockOTest {
    @Test
    void createsSquareFourCellOBlock() {
        boolean[][] shape = new BlockO(0).GetBlockData().GetShape();

        assertEquals(2, shape.length);
        assertEquals(2, shape[0].length);
        assertEquals(4, countOccupied(shape));
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
