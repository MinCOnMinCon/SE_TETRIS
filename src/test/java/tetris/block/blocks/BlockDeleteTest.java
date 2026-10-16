package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.CurrentBlock;
import tetris.block.data.CurrentBlock.ItemType;
import tetris.block.move.BlockMove;
import tetris.block.move.CanMove;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.settings.GameSettings.Difficulty;

class BlockDeleteTest {
    @Test
    void findsAndClearsCompleteSingleRowAndAwardsLineScore() {
        BoardElement[][] board = new GameBoard().GetBoard();
        for (BoardElement element : board[19]) {
            element.setBlock(true);
        }
        GameScore score = new GameScore(new GameProgress());

        assertEquals(19, BlockDelete.FindDelLines(board)[0]);
        BlockDelete.DelCompleteLines(board, score);

        assertEquals(100, score.GetGameScore());
        for (BoardElement element : board[0]) {
            assertFalse(element.isBlock());
        }
    }

    @Test
    void findsAndClearsCompleteRowsAndAwardsLineScore() {
        int[] expectedScores = {0, 100, 300, 500, 800};

        for (int linesToClear = 1; linesToClear <= 4; linesToClear++) {
            BoardElement[][] board = new GameBoard().GetBoard();
            int firstClearedRow = GameBoard.boardRow - linesToClear;
            board[firstClearedRow - 1][0].setBlock(true);

            for (int row = firstClearedRow; row < GameBoard.boardRow; row++) {
                for (BoardElement element : board[row]) {
                    element.setBlock(true);
                }
            }

            GameScore score = new GameScore(new GameProgress());
            int[] clearedRows = BlockDelete.DelCompleteLines(board, score);

            for (int i = 0; i < linesToClear; i++) {
                assertEquals(firstClearedRow + i, clearedRows[i]);
            }
            for (int i = linesToClear; i < clearedRows.length; i++) {
                assertEquals(-1, clearedRows[i]);
            }
            assertEquals(expectedScores[linesToClear], score.GetGameScore());
            assertTrue(board[GameBoard.boardRow - 1][0].isBlock());
            for (int col = 1; col < GameBoard.boardCol; col++) {
                assertFalse(board[GameBoard.boardRow - 1][col].isBlock());
            }
        }
    }

    @Test
    void horizontalLineItemDeletesItsTargetRow() {
        GameBoard gameBoard = new GameBoard();
        BoardElement[][] board = gameBoard.GetBoard();
        board[9][0].setBlock(true);
        fillRow(board, 10);
        GameScore score = new GameScore(new GameProgress());
        int[] clearedRows = gameBoard.GetClearedRows();

        dropItem(board, ItemType.DEL_HLINE, 4, 10, score, clearedRows);

        assertEquals(100, score.GetGameScore());
        assertEquals(10, clearedRows[0]);
        assertEquals(-1, clearedRows[1]);
        assertTrue(board[10][0].isBlock());
        assertFalse(board[10][1].isBlock());
        for (BoardElement element : board[0]) {
            assertFalse(element.isBlock());
        }
    }

    @Test
    void verticalLineItemDeletesItsTargetColumnOnly() {
        BoardElement[][] board = new GameBoard().GetBoard();
        for (BoardElement[] row : board) {
            row[4].setBlock(true);
        }
        board[10][3].setBlock(true);

        dropItem(board, ItemType.DEL_VLINE, 4, 10, new GameScore(new GameProgress()), new int[4]);

        for (BoardElement[] row : board) {
            assertFalse(row[4].isBlock());
        }
        assertTrue(board[10][3].isBlock());
    }

    @Test
    void verticalCompressorMovesBlocksDownIntoEmptyCells() {
        BoardElement[][] board = new GameBoard().GetBoard();
        board[2][4].setBlock(true);
        board[5][4].setBlock(true);

        dropItem(board, ItemType.DEL_VEMPTY, 4, 10, new GameScore(new GameProgress()), new int[4]);

        for (int row = GameBoard.boardRow - 4; row < GameBoard.boardRow; row++) {
            assertTrue(board[row][4].isBlock());
        }
        assertFalse(board[GameBoard.boardRow - 5][4].isBlock());
    }

    @Test
    void areaItemFillsNearbyEmptyCellsAndClearsCompletedRow() {
        BoardElement[][] board = new GameBoard().GetBoard();
        for (int col : new int[] {0, 1, 2, 6, 7, 8, 9}) {
            board[10][col].setBlock(true);
        }
        board[9][0].setBlock(true);
        GameScore score = new GameScore(new GameProgress());
        int[] clearedRows = new int[GameBoard.maxClearedLines];

        dropItem(board, ItemType.DEL_AEMPTY, 4, 10, score, clearedRows);

        assertEquals(10, clearedRows[0]);
        assertEquals(-1, clearedRows[1]);
        assertEquals(100, score.GetGameScore());
        assertTrue(board[10][0].isBlock());
        for (int col : new int[] {1, 2, 6, 7, 8, 9}) {
            assertFalse(board[10][col].isBlock());
        }
        for (int col : new int[] {3, 4, 5}) {
            assertTrue(board[10][col].isBlock());
        }
    }

    @Test
    void fourVerticalLineBlockClearsFourCellsWhileFallingAndFixesAtFloor() {
        BoardElement[][] board = new GameBoard().GetBoard();
        int x = 3;
        int y = 0;
        for (int col = x - 1; col <= x + 4; col++) {
            board[y + 2][col].setBlock(true);
        }
        board[y + 1][x + 1].setBlock(true);

        BlockData itemData = new Block_item4V(0).GetBlockData();
        itemData.SetItem(true);
        itemData.SetItemType(ItemType.DEL_4VLINE);
        itemData.SetItemPos(new int[] {0, 0});
        itemData.SetX(x);
        itemData.SetY(y);
        GameProgress progress = new GameProgress();
        BlockQueue nextBlocks = new BlockQueue(0, progress, Difficulty.NORMAL);
        CurrentBlock currentBlock = new CurrentBlock(
                itemData, nextBlocks, new BlockHolding(), () -> {});
        GameScore score = new GameScore(progress);
        int[] clearedRows = new int[GameBoard.maxClearedLines];

        assertEquals(2, itemData.GetShape().length);
        assertEquals(4, itemData.GetShape()[0].length);
        assertTrue(itemData.GetShape()[0][1]);
        assertTrue(itemData.GetShape()[1][0]);

        BlockMove.MoveDown(currentBlock, board, score, clearedRows);

        assertFalse(currentBlock.GetCanMoveH());
        for (int col = x; col < x + 4; col++) {
            assertFalse(board[y + 2][col].isBlock());
        }
        assertTrue(board[y + 2][x - 1].isBlock());
        assertTrue(board[y + 2][x + 4].isBlock());
        assertEquals(0, score.GetGameScore());

        board[y + 1][x + 1].setBlock(false);
        int floorY = GameBoard.boardRow - itemData.GetShape().length;
        while (itemData.GetY() < floorY) {
            BlockMove.MoveDown(currentBlock, board, score, clearedRows);
        }
        assertEquals(floorY, itemData.GetY());
        assertFalse(currentBlock.GetCanMoveH());
        assertFalse(CanMove.IsNoBlock(0, currentBlock, board));
        BlockMove.MoveDown(currentBlock, board, score, clearedRows);

        assertTrue(currentBlock.GetCanMoveH());
        assertTrue(board[floorY][x + 1].isBlock());
        assertTrue(board[floorY][x + 2].isBlock());
        assertFalse(board[floorY][x].isBlock());
        assertFalse(board[floorY][x + 3].isBlock());
        for (int col = x; col < x + 4; col++) {
            assertTrue(board[floorY + 1][col].isBlock());
        }
    }

    private static void fillRow(BoardElement[][] board, int row) {
        for (BoardElement element : board[row]) {
            element.setBlock(true);
        }
    }

    private static void dropItem(BoardElement[][] board, ItemType type, int x, int y,
            GameScore score, int[] clearedRows) {
        BlockData itemData = new BlockData(
                new boolean[][] {{true}},
                new ColorPalette(Color.GRAY, Color.GRAY, Color.GRAY),
                0,
                x,
                y,
                true,
                type,
                new int[] {0, 0});
        GameProgress progress = new GameProgress();
        BlockQueue nextBlocks = new BlockQueue(0, progress, Difficulty.NORMAL);
        CurrentBlock currentBlock = new CurrentBlock(
                itemData, nextBlocks, new BlockHolding(), () -> {});

        board[y + 1][x].setBlock(true);
        BlockMove.MoveDown(currentBlock, board, score, clearedRows);
    }
}
