package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShapePaletteTest {
    @Test
    void tetrominoShapesContainFourCellsAndDotShapeContainsOne() {
        ShapePalette palette = new ShapePalette();
        boolean[][][] shapes = {
            palette.GetShapeI(), palette.GetShapeJ(), palette.GetShapeL(),
            palette.GetShapeO(), palette.GetShapeS(), palette.GetShapeT(),
            palette.GetShapeZ()
        };

        for (boolean[][] shape : shapes) {
            int occupiedCells = 0;
            for (boolean[] row : shape) {
                for (boolean occupied : row) {
                    if (occupied) {
                        occupiedCells++;
                    }
                }
            }
            assertEquals(4, occupiedCells);
        }
        assertTrue(palette.GetShape_dot()[0][0]);
    }
}
