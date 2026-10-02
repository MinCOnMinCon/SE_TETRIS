package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// J 블럭 (L블럭과 대비)
public class BlockJ extends Block {
    public BlockJ(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.BLUE, Color.rgb(0, 0, 128), Color.rgb(128, 0, 128));
        this.blockData = new BlockData(shapePalette.GetShapeJ(), palette, colorMode);
    }
}
