package tetris.block.move;

import tetris.block.blocks.BlockDelete;
import tetris.gamescene.GameProgress;
import tetris.block.data.CurrentBlock;
import tetris.block.data.BlockData;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;
import java.util.Arrays;

/*
    BlockMove 클래스는 테트리스 블럭의 기본적인 이동 기능을 제공하는 클래스
    BlockMove.MoveRight(BlockData, BoardElement[][]) 블럭을 오른쪽으로 이동
    BlockMove.MoveLeft(BlockData, BoardElement[][]) 블럭을 왼쪽으로 이동
    BlockMove.MoveDown(BlockData, BoardElement[][]) 블럭을 아래로 이동

    블럭이 바닥에 닿았을 때의 처리 로직은 TODO로 남겨두었으며, 새 블럭 생성 로직을 추가해야 함
*/

public class BlockMove {
    private CurrentBlock currentBlock;
    private GameScore gameScore;
    private GameProgress progress;
    private BoardElement[][] oldBoard;

    public BlockMove(CurrentBlock currentBlock, GameScore gameScore, GameProgress progress) {
        this.currentBlock = currentBlock;
        this.gameScore = gameScore;
        this.progress = progress;
    }
    public void BlockHolding() {
        currentBlock.BlockHold();
    }
    public void MoveRight() {
        if (currentBlock.GetCanMoveH() && IsNoBlock(1)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() + 1);
        }
    }
    public void MoveLeft() {
        if (currentBlock.GetCanMoveH() && IsNoBlock(-1)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() - 1);
        }
    }
    public void MoveDownMax() {
        // 블럭이 맨 아래로 이동하고 AutoMove가 돌때까지 기다림
        // 바로 다음 블럭 생성 필요 시 수정 필요
        while (IsNoBlock(0)) {
            MoveDown();
            gameScore.AddGameScore(1); // 하드 드롭의 한 칸당 추가 점수
        }
        MoveDown();
        //gameScore.AddGameScore(10); // 추가 점수 - 그냥 스페이스 계속 누르면 무조건 이 점수는 얻을 수 있어서 주석 처리
    }


    // 실제로 한 칸 내려갔을 때만 기본 낙하 점수를 추가한다.
    public void MoveDown() {
        int[] clearedRows = currentBlock.GetGameBoard().GetClearedRows();
        BoardElement[][] board = currentBlock.GetBoard();
        if (IsNoBlock(0)) {
            currentBlock.GetCurrentBlock().SetY(currentBlock.GetCurrentBlock().GetY() + 1);
            if (currentBlock.GetCanMoveH())
                gameScore.AddBlockDownScore();
            else
                BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
        }
        else if (currentBlock.GetCurrentBlock().IsItem()) {
            int itemX, itemY;
            boardCopy(board);
            int deletedRows[] = new int[GameBoard.maxClearedLines];
            for (int i = 0; i < deletedRows.length; i++) {
                deletedRows[i] = -1;
            }
            switch (currentBlock.GetCurrentBlock().GetItemType()) {
                case DEL_HLINE: // 가로선 삭제
                    MoveFix();
                    itemY = currentBlock.GetCurrentBlock().GetY() + currentBlock.GetCurrentBlock().GetItemPos()[1];
                    BlockDelete.DeleteEmptyHLine(board, itemY);
                    //deletedRows[0] = itemY;
                    //gameScore.AddLineClearScore(1);
                    //System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length); // 삭제되는 행 번호 gameboard에 넘기기
                    break;
                case DEL_4VLINE: // 무게추 세로 4줄 삭제
                    if (currentBlock.GetCanMoveH()) {
                        currentBlock.SetCanMoveH(false);
                        BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
                    }
                    else {
                        currentBlock.SetCanMoveH(true);
                        MoveFix();
                    }
                    break;
                case DEL_VLINE: // 세로선 삭제
                    MoveFix();
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetCurrentBlock().GetItemPos()[0];
                    BlockDelete.DeleteVLine(board, itemX);
                    break;
                case DEL_VEMPTY: // 세로 공백 삭제
                    MoveFix();
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetCurrentBlock().GetItemPos()[0];
                    BlockDelete.DeleteVEmpty(board, itemX);
                    break;
                case DEL_AEMPTY: // 주변 공백 삭제
                    MoveFix();
                    itemX = currentBlock.GetCurrentBlock().GetX();
                    itemY = currentBlock.GetCurrentBlock().GetY();
                    BlockDelete.DeleteAroundEmpty(board, itemX, itemY);
                    break;
                default:
                    break;
            }
            if(currentBlock.GetCanMoveH()) {
                int[] tmpDeletedRows = BlockDelete.DelCompleteLines(board, gameScore, progress);
                // if (deletedRows[0] != -1) {
                //     int i = 1;
                //     for (int index : tmpDeletedRows) {
                //         if (index == -1) break;
                //         deletedRows[i++] = index;
                //     }
                // }
                // else
                    deletedRows = tmpDeletedRows;

                System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length);                
                currentBlock.SetDelBoard(GetDelBoard(deletedRows));
                currentBlock.MoveEnd();
            }
            
        }
        else {
            boardCopy(board);
            // 테트리스가 바닥에 닿았을 때 블럭 고정 및 새 블럭 생성 로직
            MoveFix();
            int[] deletedRows = BlockDelete.DelCompleteLines(board, gameScore, progress); // 완료된 줄 삭제 + 삭제되는 행 번호 저장
            System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length); // 삭제되는 행 번호 gameboard에 넘기기
            currentBlock.SetDelBoard(GetDelBoard(deletedRows));
            currentBlock.MoveEnd();
        
        }
    }
    private void MoveFix() {
        BoardElement[][] board = currentBlock.GetBoard();
        BlockData currentBlockData = currentBlock.GetCurrentBlock();
        int x = currentBlockData.GetX();
        int y = currentBlockData.GetY();

        int[][] currentShapeIndex = currentBlockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출
        for (int[] coord : currentShapeIndex) {
            int i = coord[0];
            int j = coord[1];
            board[y + i][x + j].setBlock(true); // 블럭을 보드에 고정
            board[y + i][x + j].setElementColor(currentBlockData.GetCurrentColor()); // 블럭 색상 설정
            board[y + i][x + j].setPatternType(currentBlockData.GetPatternType()); // 고정 후에도 패턴 유지
        }
        currentBlock.SetFalseIsHolded();
    }
    private void boardCopy(BoardElement[][] board) {
        // 블럭을 고정하기 전 보드 상태를 깊은 복사
        oldBoard = new BoardElement[board.length][];
        for (int row = 0; row < board.length; row++) {
            oldBoard[row] = new BoardElement[board[row].length];
            for (int col = 0; col < board[row].length; col++) {
                BoardElement source = board[row][col];
                BoardElement copy = new BoardElement(
                    source.getElementColor(),
                    source.isBlock()
                );
                copy.setPatternType(source.getPatternType());
                oldBoard[row][col] = copy;
            }
        }
    }
    private BoardElement[][] GetDelBoard(int[] deletedRows) {
        BoardElement[][] deletedBoard;
        int deletedRowCount = 0;
        while (deletedRowCount < deletedRows.length && deletedRows[deletedRowCount] != -1) {
            deletedRowCount++;
        }
        Arrays.sort(deletedRows, 0, deletedRowCount);
        if (deletedRows[1] != -1) {
            deletedBoard = new BoardElement[oldBoard.length][];
            for (int i = 0; i < deletedRowCount; i++) {
                int row = deletedRows[i];
                deletedBoard[i] = new BoardElement[oldBoard[row].length];
                for (int j = 0; j < oldBoard[row].length; j++) {
                    deletedBoard[i][j] = oldBoard[row][j];
                }
            }
            return deletedBoard;
        }
        return null;
    }
    public boolean IsNoBlock(int direction) {
        BlockData blockData = currentBlock.GetCurrentBlock();
        int x = blockData.GetX();
        int y = blockData.GetY();
        BoardElement[][] board = currentBlock.GetBoard();

        int[][] currentShapeIndex = blockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출

        for (int[] coord : currentShapeIndex) {
            int i = coord[0];
            int j = coord[1];

            int newX = x + j + direction; // 좌(-1), 우(1), 아래(0)
            int newY = y + i + (direction == 0 ? 1 : 0);

            // 1. 보드 경계선(벽, 바닥) 충돌 체크
            if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20) {
                return false; // Out of bounds
            }

            // 2. 다른 블럭과의 충돌 체크
            if (board[newY][newX].isBlock()) {
                return false; // 이미 다른 블럭이 존재하므로 이동 불가
            }
        }
        
        return true; // 아무 충돌이 없으므로 이동 가능
    }
}
