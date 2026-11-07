package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.CurrentBlock;
import tetris.block.data.PatternType;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.BoardElement;
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
            board.GetBoard()[18][col].setElementColor(Color.RED);
            board.GetBoard()[18][col].setPatternType(PatternType.GRID);
            if (col > 0) {
                board.GetBoard()[19][col].setBlock(true);
                board.GetBoard()[19][col].setElementColor(Color.RED);
                board.GetBoard()[19][col].setPatternType(PatternType.GRID);
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
        BoardElement[][] deletedBoard = current.GetDelBoard();
        assertNotNull(deletedBoard);
        assertEquals(2, deletedBoard.length);
        for (int row = 0; row < deletedBoard.length; row++) {
            for (int col = 0; col < GameBoard.boardCol; col++) {
                if (deletedBoard[row][col].isBlock()) {
                    assertEquals(Color.GRAY, deletedBoard[row][col].getElementColor());
                    assertEquals(PatternType.NONE, deletedBoard[row][col].getPatternType());
                }
            }
        }
        for (int row = 18; row < GameBoard.boardRow; row++) {
            for (int col = 0; col < GameBoard.boardCol; col++) {
                assertFalse(board.GetBoard()[row][col].isBlock());
            }
        }
    }
}
