package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ShapePalette;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;


// 블럭 클래스 통합

public class Block {
    private BlockData blockData;
    private final ShapePalette shapePalette = new ShapePalette();

    public BlockData GetBlockData() {
        return this.blockData;
    }
    
    public BlockData GetBlockI(int colorMode) {
        return new BlockData(shapePalette.GetShapeI(), ColorType.BlockI, colorMode, PatternType.VERTICAL_STRIPES);
    }

    public BlockData GetBlockJ(int colorMode) {
        return new BlockData(shapePalette.GetShapeJ(), ColorType.BlockJ, colorMode, PatternType.HORIZONTAL_STRIPES);
    }

    public BlockData GetBlockL(int colorMode) {
        return new BlockData(shapePalette.GetShapeL(), ColorType.BlockL, colorMode, PatternType.DIAGONAL_UP);
    }

    public BlockData GetBlockO(int colorMode) {
        return new BlockData(shapePalette.GetShapeO(), ColorType.BlockO, colorMode, PatternType.CHECKERBOARD);
    }

    public BlockData GetBlockS(int colorMode) {
        return new BlockData(shapePalette.GetShapeS(), ColorType.BlockS, colorMode, PatternType.DIAGONAL_DOWN);
    }

    public BlockData GetBlockT(int colorMode) {
        return new BlockData(shapePalette.GetShapeT(), ColorType.BlockT, colorMode, PatternType.GRID);
    }

    public BlockData GetBlockZ(int colorMode) {
        return new BlockData(shapePalette.GetShapeZ(), ColorType.BlockZ, colorMode, PatternType.DOTS);
    }
    public BlockData GetWeightBlock() {
        return new BlockData(shapePalette.GetShapeWeightBlock(), ColorType.GRAY, 0);
    }
    public BlockData GetDotBlock() {
        this.blockData = new BlockData(shapePalette.GetShapeDotBlock(), ColorType.GRAY, 0);
        this.blockData.SetX(4);
        return blockData;
    }
}