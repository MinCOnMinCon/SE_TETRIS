package tetris.block.data;

import java.util.Objects;

import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;


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
    private BlockData blockHolding;
    private boolean canMoveH;
    private boolean isHolded;
    private GameBoard board;
    private final Runnable onGameOver;

    public CurrentBlock(BlockData blockData, BlockQueue nextBlocks, BlockData blockHolding, 
        boolean canMoveH, boolean isHolded, GameBoard board, Runnable onGameOver) {
        this.blockData = blockData;
        this.nextBlocks = nextBlocks;
        this.blockHolding = blockHolding;
        this.canMoveH = canMoveH;
        this.isHolded = isHolded;
        this.board = board;
        this.onGameOver = Objects.requireNonNull(onGameOver);
    }
    public CurrentBlock(BlockQueue nextBlocks, GameBoard board, Runnable onGameOver) {
        this(nextBlocks.GetNextBlock(), nextBlocks, null, true, false, board, onGameOver);
    }

    public BlockData GetCurrentBlock() {
        return this.blockData;
    }
    public void SetCurrentBlock(BlockData blockData) {
        this.blockData = blockData;
    }
    public BlockQueue GetBlockQueue(){
        return this.nextBlocks;
    }
    public BlockData GetBlockHolding(){
        return this.blockHolding;
    }
    public boolean GetCanMoveH() {
        return this.canMoveH;
    }
    public void SetCanMoveH(boolean isCan) {
        this.canMoveH = isCan;
    }
    public void SetFalseIsHolded() {
        this.isHolded = false;
    }
    public BoardElement[][] GetBoard() {
        return this.board.GetBoard();
    }
    public void SetBoard(BoardElement[][] board) {
        System.arraycopy(board, 0, this.board.GetBoard(), 0, board.length);
    }
    public GameBoard GetGameBoard() {
        return this.board;
    }
    
    

    public void MoveEnd(BlockData blockData) {
        if(!CanGetNextBlock(blockData)){
            onGameOver.run();
            return;
        }
        this.canMoveH = true;
        SetCurrentBlock(blockData);
    }
    public void MoveEnd() {
        MoveEnd(nextBlocks.GetNextBlock());
    }

    public void BlockHold() {
        if(!isHolded) {
            isHolded = true;
            if (this.blockHolding == null) {
                SaveHold();
                MoveEnd();
            } else {
                BlockData temp = this.blockHolding;
                SaveHold();
                MoveEnd(temp);
            }
        }
    }
    private void SaveHold() {
        this.blockData.SetX(3);
        this.blockData.SetY(0);
        if (blockData.GetShape() == new ShapePalette().GetShape_dot())
            this.blockData.SetX(4);
        this.blockHolding = this.blockData;
    }

    private boolean CanGetNextBlock(BlockData BlockData) {
        int x = BlockData.GetX();
        int y = BlockData.GetY();

        int[][] currentShapeIndex = BlockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출

        for (int[] coord : currentShapeIndex) {
            int i = coord[0];
            int j = coord[1];

            int newX = x + j;
            int newY = y + i;

            // 다른 블럭과의 충돌 체크
            if (board.GetBoard()[newY][newX].isBlock()) {
                return false; // 이미 다른 블럭이 존재하므로 이동 불가
            }
        }
        
        return true; // 아무 충돌이 없으므로 이동 가능
    }
}
