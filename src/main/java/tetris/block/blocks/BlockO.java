package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// O 블럭
public class BlockO extends Block {
    public BlockO(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeO(), ColorType.BlockO, colorMode);
        this.blockData.SetPatternType(PatternType.CHECKERBOARD);
    }
}
