package tetris.block.blocks;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

public class Block4VLines extends Block {
    public Block4VLines(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.GRAY, Color.GRAY, Color.GRAY);
        this.blockData = new BlockData(shapePalette.GetShape_4VLines(), palette, colorMode);
    }
}
