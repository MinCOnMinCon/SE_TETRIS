package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;

// I 블럭
public class BlockI extends Block {
    public BlockI(int colorMode) {
        this.blockData = new BlockData(shapePalette.GetShapeI(), ColorType.BlockI, colorMode);
        this.blockData.SetPatternType(PatternType.VERTICAL_STRIPES);
    }
}


/* 
    적록 모드: I의 하늘색과 S의 청록색은 둘 다 푸른 계열이라 비슷하게 느껴질 수 있고
                L의 주황색과 O의 금색도 가까워 보일 수 있어요
    청황 모드: I의 파랑과 J의 보라가 일부 사용자에게 혼동될 수 있어요
*/
