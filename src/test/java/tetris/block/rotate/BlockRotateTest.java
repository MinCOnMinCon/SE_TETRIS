package tetris.block.rotate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
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
        GameBoard gameBoard = new GameBoard(0);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, gameBoard, () -> {});
        BlockRotate rotation = new BlockRotate(current);

        rotation.Rotate(true);
        assertArrayEquals(new boolean[][] {{true, true}, {true, false}, {true, false}},
                data.GetShape());
        rotation.Rotate(false);
        assertArrayEquals(original, data.GetShape());
    }

    @Test
    void keepsOriginalShapeWhenRotatedShapeWouldOverlapAnotherBlock() {
        boolean[][] original = {
            {true, false, false},
            {true, true, true}
        };
        BlockData data = new BlockData(original, 3, 2);
        GameBoard gameBoard = new GameBoard(0);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, gameBoard, () -> {});
        gameBoard.GetBoard()[4][3].setBlock(true);
        BlockRotate rotation = new BlockRotate(current);

        rotation.Rotate(true);

        assertArrayEquals(original, data.GetShape());
    }

    @Test
    void keepsOriginalShapeWhenRotationWouldCrossBoardBoundary() {
        boolean[][] original = {
            {true, false, false},
            {true, true, true}
        };
        BlockData data = new BlockData(original, 3, 18);
        GameBoard gameBoard = new GameBoard(0);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, gameBoard, () -> {});
        BlockRotate rotation = new BlockRotate(current);

        rotation.Rotate(true);

        assertArrayEquals(original, data.GetShape());
    }
}
