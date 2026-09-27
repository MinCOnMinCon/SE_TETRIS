package tetris.block.blocks;

import java.awt.Color;
import tetris.block.BlockData;
import tetris.block.ColorPalette;

// O 블럭
public class BlockO extends Block {
    public BlockO() {
        ColorPalette palette = new ColorPalette(Color.YELLOW, new Color(255, 215, 0), new Color(255, 250, 205));
        this.blockData = new BlockData(shapePalette.GetShapeO(), palette);
    }
}
