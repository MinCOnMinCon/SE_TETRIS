package tetris.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class BlockGenerationRunnerTest {
    @Test
    void rejectsNonPositiveTrialCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> BlockGenerationTest.Test(0, Difficulty.EASY, 252L));
        assertThrows(IllegalArgumentException.class,
                () -> BlockGenerationTest.Test(-1, Difficulty.EASY, 252L));
    }

    @Test
    void runsDistributionCheckThroughPublicBlockGeneration() {
        assertDoesNotThrow(() -> BlockGenerationTest.Test(100, Difficulty.EASY, 252L));
    }
}
