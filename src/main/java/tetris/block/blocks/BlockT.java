package src.main.java.tetris.block.blocks;

import java.awt.Color;
import src.main.java.tetris.block.BlockData;
import src.main.java.tetris.block.ColorPalette;

// T 블럭
public class BlockT extends Block {
    public BlockT() {
        ColorPalette palette = new ColorPalette(Color.MAGENTA, new Color(148, 0, 211), new Color(218, 112, 214));
        this.blockData = new BlockData(shapePalette.GetShapeT(), palette);
    }
}