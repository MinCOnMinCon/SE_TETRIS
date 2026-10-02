package tetris.block.rotate;

import tetris.block.data.BlockData;

/*
    BlockRotate 클래스는 테트리스 블럭의 회전 기능을 제공
    Rotate(BlockData, boolean[][], int) 블럭을 회전시키고 회전 가능 여부에 따라 회전된 모양을 적용
    RotateRight(boolean[][]) 블럭을 시계방향으로 90도 회전
    RotateLeft(boolean[][]) 블럭을 반시계방향으로 90도 회전
*/

public class BlockRotate {
    public static void Rotate(BlockData blockData, boolean[][] board, int direction) {
        boolean[][] originalShape = blockData.GetShape();
        BlockData tmpBlockData = new BlockData(originalShape, blockData.GetX(), blockData.GetY());
        int rows = originalShape.length;       // 기존 배열의 세로 길이(y)
        int cols = originalShape[0].length;    // 기존 배열의 가로 길이(x)
        // 가로 세로 길이가 반전된 새로운 배열 생성
        boolean[][] rotatedShape = new boolean[cols][rows];

        if (direction == 0) {
            rotatedShape = RotateRight(originalShape);
        } else if (direction == 1) {
            rotatedShape = RotateLeft(originalShape);
        }
        tmpBlockData.SetShape(rotatedShape);
        
        if (CanRotate.IsNoBlock(tmpBlockData, board)) {
            blockData.SetShape(rotatedShape);
        }
    }
        
    public static boolean[][] RotateRight(boolean[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 시계방향 90도 회전 공식 적용
        boolean[][] rotatedShape = new boolean[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [x][rows - 1 - y]로 이동
                rotatedShape[x][rows - 1 - y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }

    public static boolean[][] RotateLeft(boolean[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 반시계방향 90도 회전 공식 적용
        boolean[][] rotatedShape = new boolean[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [cols - 1 - x][y]로 이동
                rotatedShape[cols - 1 - x][y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }
}
