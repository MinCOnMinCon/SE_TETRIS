package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class UserMoveTest {
    @Test
    void delegatesHorizontalMovementToBlockMovement() {
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(data, null, null, () -> {});
        var board = new GameBoard().GetBoard();

        UserMove.MoveRight(current, board);
        assertEquals(5, data.GetX());

        UserMove.MoveLeft(current, board);
        assertEquals(4, data.GetX());
    }

    @Test
    void hardDropLocksBlockAndAwardsDropAndHardDropScores() {
        GameProgress progress = new GameProgress();
        GameBoard gameBoard = new GameBoard();
        BlockQueue queue = new BlockQueue(0, progress, Difficulty.NORMAL);
        BlockData data = new BlockData(
                new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE), 0, 4, 0);
        CurrentBlock current = new CurrentBlock(data, queue, null, () -> {});
        GameScore score = new GameScore(progress);

        UserMove.MoveDownMax(current, gameBoard.GetBoard(), score, gameBoard.GetClearedRows(), progress);

        assertEquals(19, data.GetY());
        assertTrue(gameBoard.GetBoard()[19][4].isBlock());
        assertEquals(209, score.GetGameScore());
    }
}
