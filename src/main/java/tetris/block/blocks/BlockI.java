package tetris.block.blocks;

import java.awt.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// I 블럭
public class BlockI extends Block {
    public BlockI() {
        // 색상: 일반(Cyan), 적록(하늘색), 청황(진한 파랑)
        ColorPalette palette = new ColorPalette(Color.CYAN, new Color(135, 206, 235), Color.BLUE);
        this.blockData = new BlockData(shapePalette.GetShapeI(), palette);
    }
}
