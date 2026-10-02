package tetris.gamescene.blockqueue;

import tetris.block.data.BlockData;

public class BlockQueue {
    private BlockData[] blockQueue;

    public BlockQueue() {
        this(new BlockData[0]);
    }

    public BlockQueue(BlockData[] blocks) {
        this.blockQueue = blocks;
    }

    public BlockData[] GetBlockQueue() {
        return blockQueue;
    }
}
