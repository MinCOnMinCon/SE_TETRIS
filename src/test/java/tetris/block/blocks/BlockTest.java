package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class BlockTest {
    @Test
    void blockFactoryCreatesDataWithShape() {
        Block block = new Block();
        var data = block.GetBlockT(0);

        assertNotNull(data);
        assertNotNull(data.GetShape());
    }
}
