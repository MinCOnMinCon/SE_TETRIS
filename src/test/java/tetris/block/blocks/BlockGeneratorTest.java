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
        int previous = -1;

        for (int i = 0; i < 100; i++) {
            int generated = generator.GenerateBlockIndex();
            assertTrue(generated >= 0 && generated < 7);
            if (previous != -1) {
                assertNotEquals(previous, generated);
            }
            previous = generated;
        }

        assertEquals(100, generator.GetGeneratedCount());
    }
}
