package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// Z 블럭 (S블럭과 대비)
public class BlockZ extends Block {
    public BlockZ(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.RED, Color.rgb(139, 0, 0), Color.rgb(255, 99, 71));
        this.blockData = new BlockData(shapePalette.GetShapeZ(), palette, colorMode);
    }
}
