package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;

class CurrentBlockTest {
    @Test
    void storesCurrentBlockAndMovementState() {
        GameBoard board = new GameBoard(0);
        BlockData initialBlock = createBlock(0, 0);
        CurrentBlock currentBlock = new CurrentBlock(
                initialBlock, null, null, true, false, board, () -> {});

        assertSame(initialBlock, currentBlock.GetCurrentBlock());
        assertTrue(currentBlock.GetCanMoveH());
        assertEquals(null, currentBlock.GetBlockQueue());
        assertEquals(null, currentBlock.GetBlockHolding());
        assertSame(board.GetBoard(), currentBlock.GetBoard());

        BlockData replacementBlock = createBlock(1, 1);
        currentBlock.SetCurrentBlock(replacementBlock);
        currentBlock.SetCanMoveH(false);

        assertSame(replacementBlock, currentBlock.GetCurrentBlock());
        assertFalse(currentBlock.GetCanMoveH());
    }

    @Test
    void moveEndAcceptsNextBlockWhenSpawnCellsAreEmpty() {
        GameBoard board = new GameBoard(0);
        BlockData initialBlock = createBlock(0, 0);
        BlockData nextBlock = createBlock(2, 3);
        CurrentBlock currentBlock = new CurrentBlock(
                initialBlock, null, null, false, false, board, () -> {});

        currentBlock.MoveEnd(nextBlock);

        assertSame(nextBlock, currentBlock.GetCurrentBlock());
        assertTrue(currentBlock.GetCanMoveH());
    }

    @Test
    void moveEndKeepsCurrentBlockAndSignalsGameOverWhenSpawnIsBlocked() {
        GameBoard board = new GameBoard(0);
        BlockData initialBlock = createBlock(0, 0);
        BlockData blockedBlock = createBlock(2, 3);
        board.GetBoard()[3][2].setBlock(true);
        int[] gameOverCalls = {0};
        CurrentBlock currentBlock = new CurrentBlock(
                initialBlock, null, null, true, false, board, () -> gameOverCalls[0]++);

        currentBlock.MoveEnd(blockedBlock);

        assertTrue(board.GetBoard()[3][2].isBlock());
        assertSame(initialBlock, currentBlock.GetCurrentBlock());
        assertEquals(1, gameOverCalls[0]);
    }

    @Test
    void getDelBoardReturnsAndClearsDeletedBoard() {
        CurrentBlock currentBlock = new CurrentBlock(
                createBlock(0, 0), null, null, true, false, new GameBoard(0), () -> {});
        BoardElement[][] deletedBoard = {{new BoardElement(0)}};
        currentBlock.SetDelBoard(deletedBoard);

        assertSame(deletedBoard, currentBlock.GetDelBoard());
        assertNull(currentBlock.GetDelBoard());
    }

    @Test
    void versusBoardCanBeAddedAndPopped() {
        CurrentBlock currentBlock = new CurrentBlock(
                createBlock(0, 0), null, null, true, false, new GameBoard(0), () -> {});
        BoardElement[][] firstBoard = {{new BoardElement(0)}};
        BoardElement[][] secondBoard = {{new BoardElement(0)}};

        currentBlock.AddVersusBoards(firstBoard);
        currentBlock.AddVersusBoards(secondBoard);

        BoardElement[][] popped = currentBlock.PopVersusBoards();
        assertNotNull(popped);
        assertEquals(2, popped.length);
        assertSame(firstBoard[0], popped[0]);
        assertSame(secondBoard[0], popped[1]);
        assertNull(currentBlock.PopVersusBoards());
    }

    @Test
    void versusBoardKeepsOnlyAvailableRowsAndIgnoresFullAttack() {
        CurrentBlock currentBlock = new CurrentBlock(
                createBlock(0, 0), null, null, true, false, new GameBoard(0), () -> {});
        BoardElement[][] firstBoard = new BoardElement[8][];
        BoardElement[][] secondBoard = new BoardElement[5][];
        for (int i = 0; i < firstBoard.length; i++)
            firstBoard[i] = new BoardElement[] {new BoardElement(i)};
        for (int i = 0; i < secondBoard.length; i++)
            secondBoard[i] = new BoardElement[] {new BoardElement(i + 8)};

        currentBlock.AddVersusBoards(firstBoard);
        currentBlock.AddVersusBoards(secondBoard);

        BoardElement[][] popped = currentBlock.PopVersusBoards();
        assertNotNull(popped);
        assertEquals(10, popped.length);
        assertSame(firstBoard[7], popped[7]);
        assertSame(secondBoard[1], popped[9]);

        BoardElement[][] fullBoard = new BoardElement[10][];
        for (int i = 0; i < fullBoard.length; i++)
            fullBoard[i] = new BoardElement[] {new BoardElement(i)};
        BoardElement[][] ignoredBoard = new BoardElement[1][];
        ignoredBoard[0] = new BoardElement[] {new BoardElement(99)};

        currentBlock.AddVersusBoards(fullBoard);
        BoardElement[][] fullAfterAdd = currentBlock.PopVersusBoards();
        assertNotNull(fullAfterAdd);
        assertSame(fullBoard[0], fullAfterAdd[0]);

        currentBlock.AddVersusBoards(fullBoard);
        currentBlock.AddVersusBoards(ignoredBoard);
        BoardElement[][] remainsFull = currentBlock.PopVersusBoards();
        assertNotNull(remainsFull);
        assertEquals(10, remainsFull.length);
        assertSame(fullBoard[9], remainsFull[9]);
    }

    @Test
    void moveEndAppliesAndConsumesQueuedVersusBoards() {
        GameBoard board = new GameBoard(0);
        BoardElement[] attackRow = new BoardElement[GameBoard.boardCol];
        for (int col = 0; col < attackRow.length; col++) {
            attackRow[col] = new BoardElement(0);
        }
        attackRow[0].setBlock(true);
        CurrentBlock currentBlock = new CurrentBlock(
                createBlock(0, 0), null, null, true, false, board, () -> {});
        currentBlock.AddVersusBoards(new BoardElement[][] {attackRow});

        currentBlock.MoveEnd(createBlock(2, 3));

        assertSame(attackRow, board.GetBoard()[GameBoard.boardRow - 1]);
        assertTrue(board.GetBoard()[GameBoard.boardRow - 1][0].isBlock());
        assertNull(currentBlock.PopVersusBoards());
    }

    private static BlockData createBlock(int x, int y) {
        return new BlockData(new boolean[][] {{true}}, x, y);
    }
}
