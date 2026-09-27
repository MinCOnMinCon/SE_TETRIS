package tetris.block.move;

import tetris.block.data.BlockData;

/*
    CanMove 클래스는 테트리스 블럭의 이동 가능 여부를 판단하는 클래스
    CanMove.IsNoBlock((0, -1, 1), BlockData, int[][]) 블럭이 특정 방향(0: 아래, -1: 왼쪽, 1: 오른쪽)으로 이동할 수 있는지 확인
    이동 가능하면 true, 이동 불가능하면 false 반환

    현재 x, y 좌표의 기준 : 블럭의 좌측 상단 모서리
*/

public class CanMove {
    public static boolean IsNoBlock(int direction, BlockData blockData, int[][] board) {
        int x = blockData.GetX();
        int y = blockData.GetY();
        int[][] shape = blockData.GetShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                // 블럭의 데이터가 존재하는 부분(1)만 검사
                if (shape[i][j] != 0) {
                    int newX = x + j + direction; // 좌(-1), 우(1), 아래(0)
                    int newY = y + i + (direction == 0 ? 1 : 0);

                    // 1. 보드 경계선(벽, 바닥) 충돌 체크
                    if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20) {
                        return false; // Out of bounds
                    }

                    // 2. 다른 블럭과의 충돌 체크
                    if (board[newY][newX] != 0) {
                        return false; // 이미 다른 블럭이 존재하므로 이동 불가
                    }
                }
            }
        }
        return true; // 아무 충돌이 없으므로 이동 가능
    }
}
