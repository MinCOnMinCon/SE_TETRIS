package tetris.block.blocks;

import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;

public class BlockDelete {
    public static void DelCompleteLines(BoardElement[][] board, GameScore gameScore) {
            int index[] = FindDelLines(board);
            int scoreIndex = -1;
            int[] delScoreArr = {100, 300, 500, 800}; // 제거된 줄 수에 따른 점수 배열
            
            for (int delLine : index) {
                if (delLine == -1) break; // 제거할 행이 없으면 종료
                DeleteLine(board, delLine);
                scoreIndex++;
            }
            gameScore.SetGameScore(delScoreArr[scoreIndex]); // 제거된 줄 수에 따라 점수 증가

    }

    public static int[] FindDelLines(BoardElement[][] board) {
        int index[] = {-1, -1, -1, -1}; // 최대 4개의 행이 제거될 수 있으므로 4로 설정
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
