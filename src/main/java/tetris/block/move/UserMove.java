package tetris.block.move;

import tetris.block.data.BlockData;

/*
    UserMove 클래스는 테트리스 블럭의 사용자에 의한 이동 기능을 제공하는 클래스
    UserMove.MoveRight(BlockData, int[][]) 블럭을 오른쪽으로 이동
    UserMove.MoveLeft(BlockData, int[][]) 블럭을 왼쪽으로 이동
    UserMove.MoveDown(BlockData, int[][]) 블럭을 아래로 이동
    
    **UserMove.MoveDownMax(BlockData, int[][]) 블럭을 맨 아래로 이동**
*/

public class UserMove {
    public static void MoveRight(BlockData blockData, int[][] board) {
        BlockMove.MoveRight(blockData, board);
    }
    public static void MoveLeft(BlockData blockData, int[][] board) {
        BlockMove.MoveLeft(blockData, board);
    }
    public static void MoveDown(BlockData blockData, int[][] board) {
        BlockMove.MoveDown(blockData, board);
    }
    public static void MoveDownMax(BlockData blockData, int[][] board) {
        // 블럭이 맨 아래로 이동하고 AutoMove가 돌때까지 기다림
        // 바로 다음 블럭 생성 필요 시 수정 필요
        while (CanMove.IsNoBlock(0, blockData, board)) {
            BlockMove.MoveDown(blockData, board);
        }
    }
}
