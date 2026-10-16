package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class BlockTest {
    @Test
    void concreteBlockExposesItsBlockData() {
        Block block = new BlockT(0);

        assertNotNull(block.GetBlockData());
        assertNotNull(block.GetBlockData().GetShape());
    }
}
