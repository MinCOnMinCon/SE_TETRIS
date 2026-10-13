package tetris.block.data;

import tetris.block.move.CanMove;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.blockholding.BlockHolding;
import tetris.gamescene.board.BoardElement;


/*
    CurrentBlock 클래스는 현재 테트리스 블럭의 정보를 저장하는 클래스
    CurrentBlock(BlockData, BlockQueueController, BlockHolding) 현재 블럭, 다음 블럭 큐, 홀딩 블럭을 설정
    GetCurrentBlock() 현재 블럭 반환
    SetCurrentBlock(BlockData) 현재 블럭 설정
    MoveEnd() 블럭이 바닥에 닿았을 때 다음 블럭 반환
    BlockHold() 현재 블럭을 홀딩에 저장하거나 홀딩된 블럭과 교체
*/

public class CurrentBlock {
    public enum ItemType {
        NORMAL,
        DEL_HLINE,
        DEL_VLINE,
        DEL_4VLINE,
        DEL_VEMPTY,
        DEL_AEMPTY
    }
    private BlockData blockData;
    private BlockQueue nextBlocks;
    private BlockHolding blockHolding;
    
    

    public CurrentBlock(BlockQueue nextBlocks, BlockHolding blockHolding) {
        this.blockData = nextBlocks.GetNextBlock();
        this.nextBlocks = nextBlocks;
        this.blockHolding = blockHolding;
    }

    public BlockData GetCurrentBlock() {
        return this.blockData;
    }
    public void SetCurrentBlock(BlockData blockData) {
        this.blockData = blockData;
    }
    

    public void MoveEnd(BoardElement[][] board) {
        SetCurrentBlock(nextBlocks.GetNextBlock());
        if(!CanMove.CanGetNextBlock(this.GetCurrentBlock(), board)){
            // 게임 오버 처리 로직 추가 필요
        }

    }

    public void BlockHold(BoardElement[][] board) {
        if (blockHolding.GetBlockHolding() == null) {
            SaveHold();
            MoveEnd(board);
        } else {
            BlockData temp = blockHolding.GetBlockHolding();
            SaveHold();
            SetCurrentBlock(temp);
        }
    }
    private void SaveHold() {
        boolean[][] dot = {{true}};
            this.blockData.SetX(3);
            this.blockData.SetY(0);
            if (blockData.GetShape() == dot)
                this.blockData.SetX(4);
            blockHolding.SetBlockHolding(this.blockData);
    }
}
