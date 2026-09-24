package src.main.java.tetris.block.blocks;

import src.main.java.tetris.block.BlockData;
import src.main.java.tetris.block.ShapePalette;

/*
    Block 클래스는 블럭의 최종적인 데이터들을 통합하는 클래스의 기본형
    GetBlockData() 현 블럭의 데이터 로드
    Block*
        각 블럭에 맞는 모양과 색상 설정
*/

// 기본 빈 블럭 클래스
public abstract class Block {
    protected BlockData blockData;
    protected ShapePalette shapePalette = new ShapePalette();

    public BlockData GetBlockData() {
        return this.blockData;
    }
}
