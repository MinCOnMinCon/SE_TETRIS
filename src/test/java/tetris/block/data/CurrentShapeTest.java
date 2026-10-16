package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class CurrentShapeTest {
    @Test
    void returnsOccupiedCellsInRowMajorOrder() {
        boolean[][] shape = {
            {true, false, true},
            {false, true, false}
        };

        assertArrayEquals(new int[][] {{0, 0}, {0, 2}, {1, 1}},
                CurrentShape.currentShapeIndex(shape));
    }

    @Test
    void returnsNoCoordinatesForAnEmptyShape() {
        assertArrayEquals(new int[0][2],
                CurrentShape.currentShapeIndex(new boolean[][] {{false}, {false}}));
    }
}
