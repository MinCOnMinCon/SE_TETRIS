package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// S 블럭 (Z블럭과 대비)
public class BlockS extends Block {
    public BlockS(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeS(), ColorType.BlockS, colorMode);
        this.blockData.SetPatternType(PatternType.DIAGONAL_DOWN);
    }
}
