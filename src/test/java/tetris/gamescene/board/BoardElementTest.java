package tetris.gamescene.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class BoardElementTest {
    @Test
    void storesAndUpdatesBlockStateAndColor() {
        BoardElement element = new BoardElement();

        assertFalse(element.isBlock());
        assertEquals(Color.BLACK, element.getElementColor());
        element.setBlock(true);
        element.setElementColor(Color.CYAN);
        assertTrue(element.isBlock());
        assertEquals(Color.CYAN, element.getElementColor());
    }
}
