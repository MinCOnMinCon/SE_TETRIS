package tetris.block.rotate;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentShape;

/*
    회전 난이도
        하  블럭당 고정 좌표
            블럭이 회전 후 위치 가능한 좌표가 고정되어 있음
        *중  블럭당 회전 좌표 (회전 축 고정)
            블럭이 회전 축 기준으로 회전 가능할 때 회전
        이후 요구사항에 따라 상으로 확장 가능하게
        상  블럭당 회전 좌표 (회전 축 이동)
            블럭이 회전 한 후에 위치 가능한 주변 좌표로 유동적 이동
*/

public class CanRotate {
    public static boolean IsNoBlock(BlockData blockData, boolean[][] board) {
        int x = blockData.GetX();
        int y = blockData.GetY();
        boolean[][] shape = blockData.GetShape();

        int[][] currentShape = CurrentShape.currentShapeIndex(shape); // 블럭의 현재 모양에서 1인 좌표만 추출

        for (int[] coord : currentShape) {
            int i = coord[0];
            int j = coord[1];

            int newX = x + j; // 좌(-1), 우(1), 아래(0)
            int newY = y + i;

            // 1. 보드 경계선(벽, 바닥) 충돌 체크
            if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20) {
                return false; // Out of bounds
            }

            // 2. 다른 블럭과의 충돌 체크
            if (board[newY][newX]) {
                return false; // 이미 다른 블럭이 존재하므로 이동 불가
            }
        }

        return true; // 아무 충돌이 없으므로 이동 가능
    }
}
