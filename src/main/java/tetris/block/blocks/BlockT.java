package tetris.block.blocks;

import java.awt.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// T 블럭
public class BlockT extends Block {
    public BlockT() {
        ColorPalette palette = new ColorPalette(Color.MAGENTA, new Color(148, 0, 211), new Color(218, 112, 214));
        this.blockData = new BlockData(shapePalette.GetShapeT(), palette);
    }
}