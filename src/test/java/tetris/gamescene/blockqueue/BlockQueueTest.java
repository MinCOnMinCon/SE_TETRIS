package tetris.gamescene.blockqueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import tetris.gamescene.GameProgress;
import tetris.settings.GameSettings.Difficulty;

class BlockQueueTest {
    @Test
    void startsFullAndReturnsBlocksInQueueOrder() {
        GameProgress progress = new GameProgress();
        BlockQueue queue = new BlockQueue(1, progress, Difficulty.NORMAL);

        assertEquals(queue.GetMaxQueueSize(), queue.GetQueueFRS()[2]);
        assertEquals(queue.GetMaxQueueSize(), queue.GetBlocksInQueueOrder().length);
        assertEquals(1, queue.GetColorMode());
        assertNotNull(queue.GetNextBlock());
        assertEquals(1, progress.GetSpawnedBlockCount());
        assertEquals(queue.GetMaxQueueSize(), queue.GetQueueFRS()[2]);
    }
}
