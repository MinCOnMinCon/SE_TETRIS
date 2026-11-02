package tetris.block.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
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

    private static BlockData createBlock(int x, int y) {
        return new BlockData(new boolean[][] {{true}},
                new ColorPalette(Color.WHITE, Color.WHITE, Color.WHITE), 0, x, y);
    }
}
