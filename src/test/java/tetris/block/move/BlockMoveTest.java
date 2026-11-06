package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import org.junit.jupiter.api.Test;

class BlockMoveTest {
    @Test
    void movesCurrentBlockAndAwardsSoftDropScore() {
        GameBoard board = new GameBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameProgress progress = new GameProgress();
        GameScore score = new GameScore(progress);
        BlockMove movement = new BlockMove(current, score, progress);

        movement.MoveRight();
        movement.MoveDown();

        assertEquals(5, data.GetX());
        assertEquals(5, data.GetY());
        assertEquals(10, score.GetGameScore());
    }

    @Test
    void clearsTwoRowsAndAdvancesCurrentBlock() {
        GameProgress progress = new GameProgress();
        GameBoard board = new GameBoard();
        for (int col = 0; col < GameBoard.boardCol; col++) {
            board.GetBoard()[18][col].setBlock(true);
            if (col > 0) {
                board.GetBoard()[19][col].setBlock(true);
            }
        }

        BlockData data = new BlockData(
                new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE),
                0,
                0,
                19);
        CurrentBlock current = new CurrentBlock(
                data,
                new BlockQueue(0, progress, Difficulty.NORMAL, GameMode.NORMAL),
                null,
                true,
                false,
                board,
                () -> {});
        GameScore score = new GameScore(progress);
        BlockMove movement = new BlockMove(current, score, progress);

        movement.MoveDown();

        assertNotSame(data, current.GetCurrentBlock());
        assertNotNull(current.GetDelBoard());
        for (int row = 18; row < GameBoard.boardRow; row++) {
            for (int col = 0; col < GameBoard.boardCol; col++) {
                assertFalse(board.GetBoard()[row][col].isBlock());
            }
        }
    }
}
