package tetris.block.data;

import java.util.Objects;

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
    private BlockData blockData;
    private BlockQueue nextBlocks;
    private BlockHolding blockHolding;
    private final Runnable onGameOver;

    public CurrentBlock(BlockData blockData, BlockQueue nextBlocks, BlockHolding blockHolding,
            Runnable onGameOver) {
        this.blockData = blockData;
        this.nextBlocks = nextBlocks;
        this.blockHolding = blockHolding;
        this.onGameOver = Objects.requireNonNull(onGameOver);
    }
    public CurrentBlock(BlockQueue nextBlocks, BlockHolding blockHolding, Runnable onGameOver) {
        this(nextBlocks.GetNextBlock(), nextBlocks, blockHolding, onGameOver);
    }

    public BlockData GetCurrentBlock() {
        return this.blockData;
    }
    public BlockHolding GetBlockHolding(){
        return blockHolding;
    }
    public BlockQueue GetBlockQueue(){
        return nextBlocks;
    }
    public void SetCurrentBlock(BlockData blockData) {
        this.blockData = blockData;
    }

    public void MoveEnd(BoardElement[][] board) {
        SetCurrentBlock(nextBlocks.GetNextBlock());
        if(!CanMove.CanGetNextBlock(this.GetCurrentBlock(), board)){
            onGameOver.run();
            return;
        }

    }

    public void BlockHold(BoardElement[][] board) {
        if (blockHolding.GetBlockHolding() == null) {
            blockHolding.SetBlockHolding(this.blockData);
            SetCurrentBlock(nextBlocks.GetNextBlock());
        } else {
            BlockData temp = blockHolding.GetBlockHolding();
            int x = this.blockData.GetX();
            int y = this.blockData.GetY();
            this.blockData.SetX(3);
            this.blockData.SetY(0);
            temp.SetX(x);
            temp.SetY(y);
            blockHolding.SetBlockHolding(this.blockData);
            SetCurrentBlock(temp);
        }
    }
}
