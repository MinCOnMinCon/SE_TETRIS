package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class CurrentShapeTest {
    @Test
    void returnsOccupiedCellsInRowMajorOrder() {
        BlockData block = new BlockData(new boolean[][] {
            {true, false, true},
            {false, true, false}
        }, 0, 0);

        assertArrayEquals(new int[][] {{0, 0}, {0, 2}, {1, 1}},
                block.GetCurrentShapeIndex());
    }

    @Test
    void updatesOccupiedCoordinatesWhenShapeChanges() {
        BlockData block = new BlockData(new boolean[][] {{true}}, 0, 0);
        block.SetShape(new boolean[][] {{false, true}, {true, false}});

        assertArrayEquals(new int[][] {{0, 1}, {1, 0}},
                block.GetCurrentShapeIndex());
    }
}
