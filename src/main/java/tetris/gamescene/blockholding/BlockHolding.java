package tetris.gamescene.blockholding;

import tetris.block.BlockData;

public class BlockHolding {
    private BlockData blockHolidng;

    public BlockHolding() {
        this(null);
    }

    public BlockHolding(BlockData heldBlock) {
        this.blockHolidng = heldBlock;
    }

    public BlockData GetBlockHolding() {
        return blockHolidng;
    }
}
