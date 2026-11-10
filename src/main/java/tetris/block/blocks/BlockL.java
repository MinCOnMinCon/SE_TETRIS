package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// L 블럭 (J블럭과 대비)
public class BlockL extends Block {
    public BlockL(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeL(), ColorType.BlockL, colorMode);
        this.blockData.SetPatternType(PatternType.DIAGONAL_UP);
    }
}
