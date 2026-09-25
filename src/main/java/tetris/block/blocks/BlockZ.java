package src.main.java.tetris.block.blocks;

import java.awt.Color;
import src.main.java.tetris.block.BlockData;
import src.main.java.tetris.block.ColorPalette;

// Z 블럭 (S블럭과 대비)
public class BlockZ extends Block {
    public BlockZ() {
        ColorPalette palette = new ColorPalette(Color.RED, new Color(139, 0, 0), new Color(255, 99, 71));
        this.blockData = new BlockData(shapePalette.GetShapeZ(), palette);
    }
}
