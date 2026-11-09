package tetris.gamescene.blockholding;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.blockqueue.BlockQueue;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class BlockHoldingTest {
    @Test
    void holdsCurrentBlockAndDoesNotHoldTwiceBeforeItLands() {
        GameProgress progress = new GameProgress();
        BlockQueue queue = new BlockQueue(0, progress, Difficulty.NORMAL, GameMode.NORMAL);
        GameBoard board = new GameBoard(0);
        CurrentBlock current = new CurrentBlock(queue, board, () -> {});
        BlockData heldBlock = current.GetCurrentBlock();

        assertSame(null, current.GetBlockHolding());

        current.BlockHold();

        assertSame(heldBlock, current.GetBlockHolding());
        assertNotSame(heldBlock, current.GetCurrentBlock());
        BlockData activeBlock = current.GetCurrentBlock();
        current.BlockHold();
        assertSame(activeBlock, current.GetCurrentBlock());
        assertSame(heldBlock, current.GetBlockHolding());
    }
}
