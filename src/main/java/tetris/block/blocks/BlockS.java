package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.PatternType;

// S 블럭 (Z블럭과 대비)
public class BlockS extends Block {
    public BlockS(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.GREEN, Color.rgb(0, 255, 255), Color.rgb(60, 179, 113));
        this.blockData = new BlockData(shapePalette.GetShapeS(), palette, colorMode);
        this.blockData.SetPatternType(PatternType.DIAGONAL_DOWN);
    }
}
