package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// T 블럭
public class BlockT extends Block {
    public BlockT(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeT(), ColorType.BlockT, colorMode);
        this.blockData.SetPatternType(PatternType.GRID);
    }
}
