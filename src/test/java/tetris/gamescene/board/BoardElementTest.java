package tetris.gamescene.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import tetris.block.data.ColorType;

class BoardElementTest {
    @Test
    void lazilyConvertsAndInvalidatesCachedColorWhenTypeChanges() {
        BoardElement element = new BoardElement(0);

        assertFalse(element.isBlock());
        assertEquals(Color.BLACK, element.getElementColor());
        element.setBlock(true);
        element.setElementColorType(ColorType.BlockI);
        assertTrue(element.isBlock());
        assertEquals(Color.CYAN, element.getElementColor());

        element.setElementColorType(ColorType.BlockO);
        assertEquals(Color.YELLOW, element.getElementColor());
    }
}
