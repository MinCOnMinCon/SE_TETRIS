package tetris.block.move;

import tetris.block.data.BlockData;

/*
    BlockMove 클래스는 테트리스 블럭의 기본적인 이동 기능을 제공하는 클래스
    BlockMove.MoveRight(BlockData, int[][]) 블럭을 오른쪽으로 이동
    BlockMove.MoveLeft(BlockData, int[][]) 블럭을 왼쪽으로 이동
    BlockMove.MoveDown(BlockData, int[][]) 블럭을 아래로 이동

    블럭이 바닥에 닿았을 때의 처리 로직은 TODO로 남겨두었으며, 필요에 따라 블럭 고정 및 새 블럭 생성 로직을 추가해야 함
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
            // TODO: 바닥에 닿았을 때 블럭 고정 및 새 블럭 생성 로직
        }
    }
}