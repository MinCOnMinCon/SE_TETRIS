package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.CurrentBlock;
import tetris.block.move.BlockController;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class UserMoveTest {
    @Test
    void delegatesHorizontalMovementToBlockMovement() {
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        GameProgress progress = new GameProgress();
        GameBoard board = new GameBoard();
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        BlockController controller = new BlockController(
                current, new AutoMove(current, Difficulty.NORMAL),
                new GameScore(progress), progress);

        controller.MoveRight();
        assertEquals(5, data.GetX());

        controller.MoveLeft();
        assertEquals(4, data.GetX());
    }

    @Test
    void hardDropLocksBlockAndAwardsDropAndHardDropScores() {
        GameProgress progress = new GameProgress();
        GameBoard gameBoard = new GameBoard();
        BlockQueue queue = new BlockQueue(0, progress, Difficulty.NORMAL, GameMode.NORMAL);
        BlockData data = new BlockData(
                new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE), 0, 4, 0);
        CurrentBlock current = new CurrentBlock(
                data, queue, null, true, false, gameBoard, () -> {});
        GameScore score = new GameScore(progress);
        BlockController controller = new BlockController(
                current, new AutoMove(current, Difficulty.NORMAL), score, progress);

        controller.MoveDownMax();

        assertEquals(19, data.GetY());
        assertTrue(gameBoard.GetBoard()[19][4].isBlock());
        assertEquals(209, score.GetGameScore());
    }
}
