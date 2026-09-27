package tetris.block.blocks;

import java.awt.Color;
import tetris.block.BlockData;
import tetris.block.ColorPalette;

// J 블럭 (L블럭과 대비)
public class BlockJ extends Block {
    public BlockJ() {
        ColorPalette palette = new ColorPalette(Color.BLUE, new Color(0, 0, 128), new Color(128, 0, 128));
        this.blockData = new BlockData(shapePalette.GetShapeJ(), palette);
    }
}
