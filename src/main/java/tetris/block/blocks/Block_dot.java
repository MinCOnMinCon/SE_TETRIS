package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// O 블럭
public class Block_dot extends Block {
    public Block_dot(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.GRAY, Color.GRAY, Color.GRAY);
        this.blockData = new BlockData(shapePalette.GetShape_dot(), palette, colorMode, 4, 0);
    }
}