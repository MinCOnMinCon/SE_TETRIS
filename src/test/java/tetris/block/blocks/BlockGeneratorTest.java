package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class BlockGeneratorTest {
    @Test
    void seededGeneratorReturnsValidNonRepeatingConsecutiveBlocks() {
        BlockGenerator generator = new BlockGenerator(Difficulty.NORMAL, new Random(42), 0);
        Class<? extends Block> previous = null;

        for (int i = 0; i < 100; i++) {
            Block block = generator.GetBlock();
            assertTrue(block instanceof BlockI || block instanceof BlockJ
                    || block instanceof BlockL || block instanceof BlockO
                    || block instanceof BlockS || block instanceof BlockT || block instanceof BlockZ);
            Class<? extends Block> generated = block.getClass();
            if (previous != null) {
                assertNotEquals(previous, generated);
            }
            previous = generated;
        }

        assertEquals(100, generator.GetGeneratedCount());
    }
}
