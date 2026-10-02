package tetris.gamescene.blockholding;

import tetris.block.data.BlockData;

/*
    BlockHolding 클래스는 테트리스 블럭의 홀딩 기능을 제공하는 클래스
    BlockHolding() 홀딩 초기화
    BlockHolding(BlockData) 홀딩 초기화, 홀딩에 블럭 추가
    GetBlockHolding() 홀딩에 있는 블럭 반환
    SetBlockHolding(BlockData) 홀딩에 블럭 설정
*/

public class BlockHolding {
    private BlockData blockHolidng;

    public BlockHolding() {
        this(null);
    }

    public BlockHolding(BlockData heldBlock) {
        this.blockHolidng = heldBlock;
    }

    public BlockData GetBlockHolding() {
        return blockHolidng;
    }
    public void SetBlockHolding(BlockData blockHolidng) {
        this.blockHolidng = blockHolidng;
    }
}
