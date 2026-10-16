package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import tetris.block.move.BlockMove;
import tetris.gamescene.board.BoardElement;
import org.junit.jupiter.api.Test;

class CurrentBlockTest {
    @Test
    void testConstructorAndAccessors() {
        BlockData initialBlock = createBlock(0, 0);
        CurrentBlock currentBlock = new CurrentBlock(initialBlock, null, null, () -> {});

        assertSame(initialBlock, currentBlock.GetCurrentBlock());
        assertTrue(currentBlock.GetCanMoveH());
        assertEquals(null, currentBlock.GetBlockQueue());
        assertEquals(null, currentBlock.GetBlockHolding());

        BlockData replacementBlock = createBlock(1, 1);
        currentBlock.SetCurrentBlock(replacementBlock);
        currentBlock.SetCanMoveH(false);

        assertSame(replacementBlock, currentBlock.GetCurrentBlock());
        assertFalse(currentBlock.GetCanMoveH());
    }

    @Test
    void testMoveEndWithAvailableBlock() {
        BlockData initialBlock = createBlock(0, 0);
        BlockData nextBlock = createBlock(2, 3);
        CurrentBlock currentBlock = new CurrentBlock(initialBlock, null, null, () -> {});

        currentBlock.SetCanMoveH(false);
        currentBlock.MoveEnd(createEmptyBoard(), nextBlock);

        assertSame(nextBlock, currentBlock.GetCurrentBlock());
        assertTrue(currentBlock.GetCanMoveH());
    }

    @Test
    void testMoveEndWithBlockedBlockKeepsLockedBlocksOnBoard() {
        BlockData initialBlock = createBlock(0, 0);
        BlockData blockedBlock = createBlock(2, 3);
        BoardElement[][] board = createEmptyBoard();
        board[3][2].setBlock(true);
        int[] gameOverCalls = {0};
        CurrentBlock currentBlock = new CurrentBlock(
                initialBlock, null, null, () -> gameOverCalls[0]++);

        BlockMove.MoveFix(currentBlock, board);
        currentBlock.MoveEnd(board, blockedBlock);

        assertTrue(board[0][0].isBlock());
        assertTrue(board[3][2].isBlock());
        assertSame(initialBlock, currentBlock.GetCurrentBlock());
        assertEquals(1, gameOverCalls[0]);
    }

    private static BlockData createBlock(int x, int y) {
        return new BlockData(new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE), 0, x, y);
    }

    private static BoardElement[][] createEmptyBoard() {
        BoardElement[][] board = new BoardElement[20][10];
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                board[y][x] = new BoardElement();
            }
        }
        return board;
    }

}
