package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// Z 블럭 (S블럭과 대비)
public class BlockZ extends Block {
    public BlockZ(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeZ(), ColorType.BlockZ, colorMode);
        this.blockData.SetPatternType(PatternType.DOTS);
    }
}
