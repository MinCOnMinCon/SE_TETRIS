package tetris.block.rotate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.block.move.BlockController;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

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
        GameProgress progress = new GameProgress();
        BlockController controller = new BlockController(
                current, new GameScore(progress), progress, Difficulty.NORMAL);

        controller.RotateClock();
        assertArrayEquals(new boolean[][] {{true, true}, {true, false}, {true, false}},
                data.GetShape());
        controller.RotateCounterClock();
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
        GameProgress progress = new GameProgress();
        BlockController controller = new BlockController(
                current, new GameScore(progress), progress, Difficulty.NORMAL);

        controller.RotateClock();

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
        GameProgress progress = new GameProgress();
        BlockController controller = new BlockController(
                current, new GameScore(progress), progress, Difficulty.NORMAL);

        controller.RotateClock();

        assertArrayEquals(original, data.GetShape());
    }
}
