package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;

class CurrentBlockTest {
    @Test
    void storesCurrentBlockAndMovementState() {
        GameBoard board = new GameBoard();
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
        GameBoard board = new GameBoard();
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
        GameBoard board = new GameBoard();
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
                createBlock(0, 0), null, null, true, false, new GameBoard(), () -> {});
        BoardElement[][] deletedBoard = {{new BoardElement()}};
        currentBlock.SetDelBoard(deletedBoard);

        assertSame(deletedBoard, currentBlock.GetDelBoard());
        assertNull(currentBlock.GetDelBoard());
    }

    @Test
    void versusBoardsCanBeAddedAndPoppedInOrder() {
        CurrentBlock currentBlock = new CurrentBlock(
                createBlock(0, 0), null, null, true, false, new GameBoard(), () -> {});
        BoardElement[][] firstBoard = {{new BoardElement()}};
        BoardElement[][] secondBoard = {{new BoardElement()}};

        currentBlock.AddVersusBoards(firstBoard);
        currentBlock.AddVersusBoards(secondBoard);

        assertSame(firstBoard, currentBlock.PopVersusBoards());
        assertSame(secondBoard, currentBlock.PopVersusBoards());
        assertNull(currentBlock.PopVersusBoards());
    }

    @Test
    void moveEndAppliesAndConsumesQueuedVersusBoards() {
        GameBoard board = new GameBoard();
        BoardElement[] attackRow = new BoardElement[GameBoard.boardCol];
        for (int col = 0; col < attackRow.length; col++) {
            attackRow[col] = new BoardElement();
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
        return new BlockData(new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE), 0, x, y);
    }
}
