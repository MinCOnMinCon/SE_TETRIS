package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// T 블럭
public class BlockT extends Block {
    public BlockT(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.MAGENTA, Color.rgb(148, 0, 211), Color.rgb(218, 112, 214));
        this.blockData = new BlockData(shapePalette.GetShapeT(), palette, colorMode);
    }
}