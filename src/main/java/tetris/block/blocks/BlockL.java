package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// L 블럭 (J블럭과 대비)
public class BlockL extends Block {
    public BlockL(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.ORANGE, Color.rgb(255, 140, 0), Color.rgb(255, 69, 0));
        this.blockData = new BlockData(shapePalette.GetShapeL(), palette, colorMode);
    }
}
