package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;

// O 블럭
public class Block_dot extends Block {
    public Block_dot(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShape_dot(), ColorType.GRAY, colorMode);
        this.blockData.SetX(4);
    }
}
