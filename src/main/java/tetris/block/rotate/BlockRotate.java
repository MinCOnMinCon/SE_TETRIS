package tetris.block.rotate;

import tetris.block.data.BlockData;

public class BlockRotate {
    public static void Rotate(BlockData blockData, int[][] board, int direction) {
        int[][] originalShape = blockData.GetShape();
        BlockData tmpBlockData = new BlockData(originalShape, blockData.GetX(), blockData.GetY());
        int rows = originalShape.length;       // 기존 배열의 세로 길이(y)
        int cols = originalShape[0].length;    // 기존 배열의 가로 길이(x)
        // 가로 세로 길이가 반전된 새로운 배열 생성
        int[][] rotatedShape = new int[cols][rows];

        if (direction == 0) {
            rotatedShape = RotateRight(originalShape);
        } else if (direction == 1) {
            rotatedShape = RotateLeft(originalShape);
        }
        
        if (CanRotate.IsNoBlock(tmpBlockData, board)) {
            blockData.SetShape(rotatedShape);
        }
    }
        
    public static int[][] RotateRight(int[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 시계방향 90도 회전 공식 적용
        int[][] rotatedShape = new int[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [x][rows - 1 - y]로 이동
                rotatedShape[x][rows - 1 - y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }

    public static int[][] RotateLeft(int[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 반시계방향 90도 회전 공식 적용
        int[][] rotatedShape = new int[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [cols - 1 - x][y]로 이동
                rotatedShape[cols - 1 - x][y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }
}
