package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;
import tetris.block.data.PatternType;

// J 블럭 (L블럭과 대비)
public class BlockJ extends Block {
    public BlockJ(int colorMode) {
        ColorPalette palette = new ColorPalette(Color.BLUE, Color.rgb(0, 0, 128), Color.rgb(128, 0, 128));
        this.blockData = new BlockData(shapePalette.GetShapeJ(), palette, colorMode);
        this.blockData.SetPatternType(PatternType.HORIZONTAL_STRIPES);
    }
}
