package tetris.block.rotate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tetris.block.data.BlockData;
import tetris.gamescene.board.GameBoard;
import org.junit.jupiter.api.Test;

class CanRotateTest {
    @Test
    void rejectsOutOfBoundsAndOccupiedCells() {
        var board = new GameBoard().GetBoard();

        assertTrue(CanRotate.IsNoBlock(new BlockData(new boolean[][] {{true}}, 0, 0), board));
        assertFalse(CanRotate.IsNoBlock(new BlockData(new boolean[][] {{true}}, 10, 0), board));
        assertFalse(CanRotate.IsNoBlock(new BlockData(new boolean[][] {{true}}, -1, 0), board));
        assertFalse(CanRotate.IsNoBlock(new BlockData(new boolean[][] {{true}}, 0, 20), board));
        board[0][0].setBlock(true);
        assertFalse(CanRotate.IsNoBlock(new BlockData(new boolean[][] {{true}}, 0, 0), board));
    }

    @Test
    void checksEveryOccupiedCellInTheRotatedShape() {
        var board = new GameBoard().GetBoard();
        boolean[][] rotatedShape = {
            {true, false},
            {true, true}
        };
        BlockData data = new BlockData(rotatedShape, 3, 2);

        assertTrue(CanRotate.IsNoBlock(data, board));

        board[3][4].setBlock(true);
        assertFalse(CanRotate.IsNoBlock(data, board));
    }
}
