package src.main.java.tetris.block.blocks;

import java.awt.Color;
import src.main.java.tetris.block.BlockData;
import src.main.java.tetris.block.ColorPalette;

// L 블럭 (J블럭과 대비)
public class BlockL extends Block {
    public BlockL() {
        ColorPalette palette = new ColorPalette(Color.ORANGE, new Color(255, 140, 0), new Color(255, 69, 0));
        this.blockData = new BlockData(shapePalette.GetShapeL(), palette);
    }
}
