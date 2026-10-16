package tetris.gamescene.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class GameBoardTest {
    @Test
    void initializesAnEmptyBoardAndClearedRows() {
        GameBoard gameBoard = new GameBoard();

        assertEquals(GameBoard.boardRow, gameBoard.GetBoard().length);
        assertEquals(GameBoard.boardCol, gameBoard.GetBoard()[0].length);
        assertEquals(GameBoard.maxClearedLines, gameBoard.GetClearedRows().length);
        for (BoardElement[] row : gameBoard.GetBoard()) {
            for (BoardElement element : row) {
                assertFalse(element.isBlock());
            }
        }
        for (int clearedRow : gameBoard.GetClearedRows()) {
            assertEquals(-1, clearedRow);
        }
    }
}
