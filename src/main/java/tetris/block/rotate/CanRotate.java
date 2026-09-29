package tetris.block.rotate;

import tetris.block.data.BlockData;

/*
    회전 난이도
        하  블럭당 고정 좌표
            블럭이 회전 후 위치 가능한 좌표가 고정되어 있음
        중  블럭당 회전 좌표 (회전 축 고정)
            블럭이 회전 축 기준으로 회전 가능할 때 회전
        상  블럭당 회전 좌표 (회전 축 이동)
            블럭이 회전 한 후에 위치 가능한 주변 좌표로 유동적 이동

    int[][] originalShape = blockData.GetShape();
        int rows = originalShape.length;       // 기존 배열의 세로 길이(y)
        int cols = originalShape[0].length;    // 기존 배열의 가로 길이(x)
        
        // 가로 세로 길이가 반전된 새로운 배열 생성
        int[][] rotatedShape = new int[cols][rows];
        
        // 시계방향 90도 회전 공식 적용
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [x][rows - 1 - y]로 이동
                rotatedShape[x][rows - 1 - y] = originalShape[y][x];
            }
        }

        // 반시계방향 90도 회전 공식 적용
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [cols - 1 - x][y]로 이동
                rotatedShape[cols - 1 - x][y] = originalShape[y][x];
            }
        }
        
        // 1. 회전된 배열을 블럭에 임시 적용
        blockData.SetShape(rotatedShape);
        
        // 2. 바뀐 모양으로 제자리에 있을 수 있는지(충돌 없는지) 검사
        if (!CanRotateCheck(blockData, board)) {
            // 충돌이 발생하면 회전을 취소하고 원래 모양으로 되돌림 (Rollback)
            blockData.SetShape(originalShape);
        }
    }
*/

public class CanRotate {
    public static void IsNoBlock(char direction, BlockData blockData, int[][] board) {
        int x = blockData.GetX();
        int y = blockData.GetY();
        int[][] shape = blockData.GetShape();
        int[][] tmpShape = new int[shape.length][shape[0].length];

        // I
        if (shape.length == 1 || shape[0].length == 1) {
            if (shape.length == 1) {
                // ㅡ 회전 후 I
                if (direction == 'l' || direction == 'r') {
                    for (int i = 0; i < 4; i++) {
                        if (board[y - i][x+1] != 0) {
                            return;
                        }
                    }

                    blockData.SetX(x + 1);
                    blockData.SetY(y - 3);
                    for (int i = 0; i < shape.length; i++) {
                        for (int j = 0; j < shape[i].length; j++) {
                            if (shape[i][j] == 1) {
                                tmpShape[j][i] = 1;
                            }
                        }
                    }
                    blockData.SetShape(tmpShape);
                    return;
                }
            } else {
                // I
                if (direction == 'l' || direction == 'r') {
                    // 회전 후 가로로 놓이게 됨
                    for (int i = 0; i < 4; i++) {
                        if (board[y + 3][x + i - 1] != 0) {
                            return;
                        }
                    }

                    blockData.SetX(x - 1);
                    blockData.SetY(y + 3);
                    for (int i = 0; i < shape.length; i++) {
                        for (int j = 0; j < shape[i].length; j++) {
                            if (shape[i][j] == 1) {
                                tmpShape[j][i] = 1;
                            }
                        }
                    }
                    blockData.SetShape(tmpShape);
                    return;
                }
            }
            return;
        }
        // O
        else if (shape.length == 2 && shape[0].length == 2) {
            return;
        }
        // T, J, L, S, Z
        else {
            return;
        }

        // for (int i = 0; i < shape.length; i++) {
        //     for (int j = 0; j < shape[i].length; j++) {
        //         // 블럭의 데이터가 존재하는 부분(1)만 검사
        //         if (shape[i][j] != 0) {
        //             int newX = x + j + direction; // 좌(-1), 우(1), 아래(0)
        //             int newY = y + i + (direction == 'l' || direction == 'r' ? 0 : 1);

        //             // 1. 보드 경계선(벽, 바닥) 충돌 체크
        //             if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20) {
        //                 return false; // Out of bounds
        //             }

        //             // 2. 다른 블럭과의 충돌 체크
        //             if (board[newY][newX] != 0) {
        //                 return false; // 이미 다른 블럭이 존재하므로 이동 불가
        //             }
        //         }
        //     }
        // }
        return; // 아무 충돌이 없으므로 이동 가능
    }
}
