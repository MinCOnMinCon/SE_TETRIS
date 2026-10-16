package tetris.gamescene.blockholding;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import tetris.block.data.BlockData;
import org.junit.jupiter.api.Test;

class BlockHoldingTest {
    @Test
    void storesHeldBlockAndStartsEmptyByDefault() {
        BlockHolding holding = new BlockHolding();
        assertNull(holding.GetBlockHolding());

        BlockData block = new BlockData(new boolean[][] {{true}}, 2, 3);
        holding.SetBlockHolding(block);
        assertSame(block, holding.GetBlockHolding());
        assertSame(block, new BlockHolding(block).GetBlockHolding());
    }
}
