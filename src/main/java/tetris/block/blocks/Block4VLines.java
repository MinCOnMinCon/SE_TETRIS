package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;

public class Block4VLines extends Block {
    public Block4VLines(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShape_4VLines(), ColorType.GRAY, colorMode);
    }
}
