package tetris.block.blocks;

import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import java.util.Arrays;

public class BlockDelete {
    public static int[] DelCompleteLines(BoardElement[][] board, GameScore gameScore) {
            int index[] = FindDelLines(board);
            int scoreIndex = -1;
            
            for (int delLine : index) {
                if (delLine == -1) break; // 제거할 행이 없으면 종료
                DeleteLine(board, delLine);
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
    public static void DeleteLine(BoardElement[][] board, int line) {
        for (int j = line; j > 0; j--) {
            board[j] = board[j - 1]; // 위의 행을 한 칸 아래로 이동
        }
        board[0] = new BoardElement[board[0].length]; // 최상단 행 초기화
        for (int col = 0; col < board[0].length; col++) {
            board[0][col] = new BoardElement();
        }
    }
}
