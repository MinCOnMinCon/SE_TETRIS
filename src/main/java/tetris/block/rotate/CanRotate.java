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
