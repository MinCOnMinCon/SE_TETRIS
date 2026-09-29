package tetris.block.move;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentShape;

/*
    BlockMove 클래스는 테트리스 블럭의 기본적인 이동 기능을 제공하는 클래스
    BlockMove.MoveRight(BlockData, int[][]) 블럭을 오른쪽으로 이동
    BlockMove.MoveLeft(BlockData, int[][]) 블럭을 왼쪽으로 이동
    BlockMove.MoveDown(BlockData, int[][]) 블럭을 아래로 이동

    블럭이 바닥에 닿았을 때의 처리 로직은 TODO로 남겨두었으며, 새 블럭 생성 로직을 추가해야 함
*/

public class BlockMove {
    public static void MoveRight(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(1, blockData, board)) {
            blockData.SetX(blockData.GetX() + 1);
        }
    }
    public static void MoveLeft(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(-1, blockData, board)) {
            blockData.SetX(blockData.GetX() - 1);
        }
    }
    public static void MoveDown(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(0, blockData, board)) {
            blockData.SetY(blockData.GetY() + 1);
        }
        else {
            int[][] shape = blockData.GetShape();
            int x = blockData.GetX();
            int y = blockData.GetY();

            int[][] currentShape = CurrentShape.currentShapeIndex(shape); // 블럭의 현재 모양에서 1인 좌표만 추출

            for (int[] coord : currentShape) {
                int i = coord[0];
                int j = coord[1];
                board[y + i][x + j] = 1; // 블럭을 보드에 고정
            }
            // TODO: 바닥에 닿았을 때 새 블럭 생성 로직
        }
    }
}