package tetris.block.blocks;

import java.awt.Color;
import tetris.block.BlockData;
import tetris.block.ColorPalette;

// S 블럭 (Z블럭과 대비)
public class BlockS extends Block {
    public BlockS() {
        ColorPalette palette = new ColorPalette(Color.GREEN, new Color(34, 139, 34), new Color(60, 179, 113));
        this.blockData = new BlockData(shapePalette.GetShapeS(), palette);
    }
}
