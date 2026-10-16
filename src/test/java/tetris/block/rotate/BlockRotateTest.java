package tetris.block.rotate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import tetris.block.data.BlockData;
import tetris.gamescene.board.GameBoard;
import org.junit.jupiter.api.Test;

class BlockRotateTest {
    @Test
    void rotatesShapeClockwiseAndCounterclockwise() {
        boolean[][] original = {
            {true, false, false},
            {true, true, true}
        };
        BlockData data = new BlockData(original, 3, 2);
        var board = new GameBoard().GetBoard();

        BlockRotate.Rotate(data, board, 0);
        assertArrayEquals(new boolean[][] {{true, true}, {true, false}, {true, false}},
                data.GetShape());
        BlockRotate.Rotate(data, board, 1);
        assertArrayEquals(original, data.GetShape());
    }

    @Test
    void keepsOriginalShapeWhenRotatedShapeWouldOverlapAnotherBlock() {
        boolean[][] original = {
            {true, false, false},
            {true, true, true}
        };
        BlockData data = new BlockData(original, 3, 2);
        var board = new GameBoard().GetBoard();
        board[4][3].setBlock(true);

        BlockRotate.Rotate(data, board, 0);

        assertArrayEquals(original, data.GetShape());
    }

    @Test
    void keepsOriginalShapeWhenRotationWouldCrossBoardBoundary() {
        boolean[][] original = {
            {true, false, false},
            {true, true, true}
        };
        BlockData data = new BlockData(original, 3, 18);
        var board = new GameBoard().GetBoard();

        BlockRotate.Rotate(data, board, 0);

        assertArrayEquals(original, data.GetShape());
    }
}
