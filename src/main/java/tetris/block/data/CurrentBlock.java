package tetris.block.data;

import java.util.List;
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
    private boolean isVersusMode = false;
    private BoardElement[][] deletedBoard; // 2줄 이상 삭제 시, 삭제된 보드의 줄들을 저장
    private List<BoardElement[][]> versusBoards; // 상대의 삭제된 보드의 줄들을 저장
    private boolean canDefend = true; // 상대의 공격을 방어할 수 있는지 여부

    public CurrentBlock(BlockData blockData, BlockQueue nextBlocks, BlockData blockHolding, 
        boolean canMoveH, boolean isHolded, GameBoard board, Runnable onGameOver) {
        this.blockData = blockData;
        this.nextBlocks = nextBlocks;
        this.blockHolding = blockHolding;
        this.canMoveH = canMoveH;
        this.isHolded = isHolded;
        this.board = board;
        this.onGameOver = Objects.requireNonNull(onGameOver);
        //versusBoards = new java.util.ArrayList<>();
    }
    public CurrentBlock(BlockQueue nextBlocks, GameBoard board, Runnable onGameOver) {
        this(nextBlocks.GetNextBlock(), nextBlocks, null, true, false, board, onGameOver);
    }
    public CurrentBlock(BlockQueue nextBlocks, GameBoard board, Runnable onGameOver, boolean isVersusMode) {
        this(nextBlocks.GetNextBlock(), nextBlocks, null, true, false, board, onGameOver);
        this.isVersusMode = isVersusMode;
        versusBoards = new java.util.ArrayList<>();
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
    public boolean GetIsVersusMode() {
        return isVersusMode;
    }
    public BoardElement[][] GetDelBoard() {
        BoardElement[][] tmp = this.deletedBoard;
        this.deletedBoard = null; // 반환 후 삭제된 보드 초기화
        return tmp;
    }
    public void SetDelBoard(BoardElement[][] deletedBoard) {
        this.deletedBoard = deletedBoard;
    }
    public List<BoardElement[][]> GetVersusBoards() {
        return this.versusBoards;
    }
    public BoardElement[][] PopVersusBoards() {
        if (this.versusBoards.isEmpty()) {
            return null;
        }
        return this.versusBoards.remove(0);
    }
    public void AddVersusBoards(BoardElement[][] versusBoard) {
        if (this.versusBoards == null)
            this.versusBoards = new java.util.ArrayList<>();
        this.versusBoards.add(versusBoard);
    }
    public void PrintDelBoard() {
        if (deletedBoard == null) {
            System.out.println("삭제된 보드가 없습니다.");
            return;
        }

        System.out.println("삭제된 보드 (# = 블록, . = 빈칸):");
        for (BoardElement[] row : deletedBoard) {
            if (row == null) {
                continue;
            }
            for (BoardElement element : row) {
                System.out.print(element.isBlock() ? '#' : '.');
            }
            System.out.println();
        }
    }
    /*
    가로줄 아이템 삭제 이펙트도 정상 작동
    deletedBoard
        
        가로줄 삭제
            삭제된 보드 (# = 블록, . = 빈칸):
            ########..
            ###.####..
        압축기
            삭제된 보드 (# = 블록, . = 빈칸):
            ###.######
            ###.######
            ###.######
        역폭탄
            삭제된 보드 (# = 블록, . = 빈칸):
            #####..###
            #####..###
            #######.##
    */
    
    

    public void MoveEnd(BlockData blockData) {
        if(!CanGetNextBlock(blockData)){
            onGameOver.run();
            return;
        }
        this.canMoveH = true;
        SetCurrentBlock(blockData);
        PrintDelBoard(); // 삭제된 보드 확인
        // if (deletedBoard != null) {
        //     this.versusBoards.add(GetDelBoard());
        // }
        if (versusBoards != null && !versusBoards.isEmpty())
            Attacked();
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
    private void Attacked() {
        while (!versusBoards.isEmpty()) {
            BoardElement[][] versusBoard = PopVersusBoards();
            int size = versusBoard.length;
            System.out.println("상대의 공격! 삭제된 줄 수: " + size);
            BoardElement[][] myBoard = board.GetBoard();
            for (int i = 0; i < size; i++) {
                for (BoardElement myCol : myBoard[i]) {
                    if (myCol.isBlock()) {
                        canDefend = false;
                        break;
                    }
                }
            }
            int mySize = myBoard.length;
            for (int j = 0; j < mySize; j++) {
                if (j + size < mySize) {
                    myBoard[j] = myBoard[j + size];
                } else {
                    myBoard[j] = versusBoard[j - (mySize - size)];
                }
            }
            if (!canDefend)
                onGameOver.run();
        }
    }
}
