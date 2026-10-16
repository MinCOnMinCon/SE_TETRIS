package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameProgressTest {
    @Test
    void advancesAfterConfiguredSpawnsAndCapsAtMaximumLevel() {
        GameProgress progress = new GameProgress(2, 1);

        progress.OnBlockSpawned();
        assertEquals(0, progress.GetLevel());
        progress.OnBlockSpawned();
        assertEquals(1, progress.GetLevel());
        progress.OnBlockSpawned();

        assertEquals(1, progress.GetLevel());
        assertEquals(3, progress.GetSpawnedBlockCount());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new GameProgress(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new GameProgress(1, -1));
    }
}
