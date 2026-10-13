package tetris.block.blocks;

import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import java.util.Arrays;

public class BlockDelete {
    public static int[] DelCompleteLines(BoardElement[][] board, GameScore gameScore) {
            int index[] = FindDelLines(board);
            int scoreIndex = -1;
            
            for (int delrow : index) {
                if (delrow == -1) break; // 제거할 행이 없으면 종료
                DeleteHLine(board, delrow);
                scoreIndex++;
            }
            if(scoreIndex != -1) gameScore.AddLineClearScore(scoreIndex + 1);
            return index;

    }

    public static int[] FindDelLines(BoardElement[][] board) {
        int index[] = new int[GameBoard.maxClearedLines];
        Arrays.fill(index, -1);
        int i = 0;
        int k = 0;
        for (BoardElement[] row : board) {
            boolean isComplete = true;
            for (BoardElement element : row) {
                if (!element.isBlock()) {
                    isComplete = false;
                    break; // 블럭이 없는 경우, 다음 행으로 이동
                }
            }
            if (isComplete) {
                index[k++] = i;
            }
            i++;
        }
        return index;
    }
    // 이동 끝 + ItemType.DEL_HLine
    public static void DeleteHLine(BoardElement[][] board, int row) {
        for (int j = row; j > 0; j--) {
            board[j] = board[j - 1]; // 위의 행을 한 칸 아래로 이동
        }
        board[0] = new BoardElement[board[0].length]; // 최상단 행 초기화
        for (int col = 0; col < board[0].length; col++) {
            board[0][col] = new BoardElement();
        }
    }
    // ItemType.DEL_4VLINE
    public static void Delete4VLines(BoardElement[][] board, BlockData blockData) {// 블럭의 x, y 좌표
        int line = blockData.GetX();
        int row = blockData.GetY();
        if (row + 2 < 0 || row + 2 >= 20)   return;
        for (int i = 0; i < 4; i++){
            board[row + 2][line + i] = new BoardElement();
        }
    }
    // ItemType.DEL_VLINE 세로줄 삭제
    public static void DeleteVLine(BoardElement[][] board, int line) {
        for (BoardElement[] row : board) {
            row[line] = new BoardElement();
        }
    }
    // ItemType.DEL_VEMPTY 압축기
    public static void DeleteVEmpty(BoardElement[][] board, int line) {
        int currentRowIndex = 0;
        int start = -1;
        for (BoardElement[] row : board) {
            if (row[line].isBlock() && start == -1){
                start = currentRowIndex;
            }
            else if (!row[line].isBlock() && start != -1) {
                for (int j = currentRowIndex; j > start; j--) {
                    board[j][line] = board[j - 1][line]; // 위의 칸을 한 칸 아래로 이동
                }
                board[start][line] = new BoardElement(); // 시작 칸 초기화
            }
            currentRowIndex++;
        }
    }
    // ItemType.DEL_AEMPTY 역 폭탄
    public static void DeleteAroundEmpty(BoardElement[][] board, int line, int row) {
        BlockData dot = new Block_dot(1).GetBlockData();
        for (int i = (line-1); i<(line+2); i++){
            for (int j = (row-1); j < (row+2); j++) {
                if(!board[j][i].isBlock()) {
                    board[j][i].setBlock(true);
                    board[j][i].setElementColor(dot.GetCurrentColor());
                }
            }
        }
    }
}
