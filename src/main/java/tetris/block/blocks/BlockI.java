package tetris.block.blocks;

import javafx.scene.paint.Color;

import tetris.block.data.BlockData;
import tetris.block.data.ColorPalette;

// I 블럭
public class BlockI extends Block {
    public BlockI(int colorMode) {
        // 색상: 일반(Cyan), 적록(하늘색), 청황(진한 파랑)
        ColorPalette palette = new ColorPalette(Color.CYAN, Color.rgb(135, 206, 235), Color.BLUE);
        this.blockData = new BlockData(shapePalette.GetShapeI(), palette, colorMode);
    }
}


/* 
    적록 모드: I의 하늘색과 S의 청록색은 둘 다 푸른 계열이라 비슷하게 느껴질 수 있고
                L의 주황색과 O의 금색도 가까워 보일 수 있어요
    청황 모드: I의 파랑과 J의 보라가 일부 사용자에게 혼동될 수 있어요
*/