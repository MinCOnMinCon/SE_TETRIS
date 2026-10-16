package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.PatternType;

// O 블럭
public class BlockO extends Block {
    public BlockO(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.YELLOW, Color.rgb(255, 215, 0), Color.rgb(255, 250, 205));
        this.blockData = new BlockData(shapePalette.GetShapeO(), palette, colorMode);
        this.blockData.SetPatternType(PatternType.CHECKERBOARD);
    }
}
