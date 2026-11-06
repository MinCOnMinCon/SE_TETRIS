package tetris.block.rotate;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.block.data.ItemType;
import tetris.gamescene.board.BoardElement;

/*
    BlockRotate 클래스는 테트리스 블럭의 회전 기능을 제공
    Rotate(BlockData, boolean[][], int) 블럭을 회전시키고 회전 가능 여부에 따라 회전된 모양을 적용
    RotateRight(boolean[][]) 블럭을 시계방향으로 90도 회전
    RotateLeft(boolean[][]) 블럭을 반시계방향으로 90도 회전
*/

public class BlockRotate {
    private CurrentBlock currentBlock;
    public BlockRotate(CurrentBlock currentBlock) {
        this.currentBlock = currentBlock;
    }
    public void Rotate(boolean isRotateRight) {
        BlockData blockData = currentBlock.GetCurrentBlock();
        BoardElement[][] board = currentBlock.GetBoard();
        // 무게추 회전 고정
        if (blockData.GetItemType()==ItemType.DEL_4VLINE)
            return;
        
        boolean[][] originalShape = blockData.GetShape();
        BlockData tmpBlockData = new BlockData(originalShape, blockData.GetX(), blockData.GetY());
        int rows = originalShape.length;       // 기존 배열의 세로 길이(y)
        int cols = originalShape[0].length;    // 기존 배열의 가로 길이(x)
        // 가로 세로 길이가 반전된 새로운 배열 생성
        boolean[][] rotatedShape = new boolean[cols][rows];

        if (isRotateRight) { // 시계방향 회전
            rotatedShape = RotateRight(originalShape);
        } else { // 반시계방향 회전
            rotatedShape = RotateLeft(originalShape);
        }
        tmpBlockData.SetShape(rotatedShape);
        
        if (IsNoBlock(tmpBlockData, board)) {
            blockData.SetShape(rotatedShape);
            int[] itemPos = blockData.GetItemPos();
            if (blockData.IsItem() && itemPos != null) {//만약 아이템이 있을 경우 아이템 좌표도 같이 회전
                
                int x = itemPos[0];
                int y = itemPos[1];
                if (isRotateRight) {
                    blockData.SetItemPos(new int[]{rows - 1 - y, x});
                } else {
                    blockData.SetItemPos(new int[]{y, cols - 1 - x});
                }
            }
        }
    }
        
    public boolean[][] RotateRight(boolean[][] originalShape) {
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

    public boolean[][] RotateLeft(boolean[][] originalShape) {
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

    private boolean IsNoBlock(BlockData blockData, BoardElement[][] board) {
        int x = blockData.GetX();
        int y = blockData.GetY();

        int[][] currentShape = blockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출

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
            if (board[newY][newX].isBlock()) {
                return false; // 이미 다른 블럭이 존재하므로 이동 불가
            }
        }

        return true; // 아무 충돌이 없으므로 이동 가능
    }
}
