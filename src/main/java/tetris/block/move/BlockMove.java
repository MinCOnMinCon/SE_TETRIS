package tetris.block.move;

import tetris.block.data.CurrentBlock;
import tetris.block.data.CurrentShape;
import tetris.gamescene.board.BoardElement;

/*
    BlockMove 클래스는 테트리스 블럭의 기본적인 이동 기능을 제공하는 클래스
    BlockMove.MoveRight(BlockData, BoardElement[][]) 블럭을 오른쪽으로 이동
    BlockMove.MoveLeft(BlockData, BoardElement[][]) 블럭을 왼쪽으로 이동
    BlockMove.MoveDown(BlockData, BoardElement[][]) 블럭을 아래로 이동

    블럭이 바닥에 닿았을 때의 처리 로직은 TODO로 남겨두었으며, 새 블럭 생성 로직을 추가해야 함
*/

public class BlockMove {
    public static void MoveRight(CurrentBlock currentBlock, BoardElement[][] board) {
        if (CanMove.IsNoBlock(1, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() + 1);
        }
    }
    public static void MoveLeft(CurrentBlock currentBlock, BoardElement[][] board) {
        if (CanMove.IsNoBlock(-1, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() - 1);
        }
    }
    public static void MoveDown(CurrentBlock currentBlock, BoardElement[][] board) {
        if (CanMove.IsNoBlock(0, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetY(currentBlock.GetCurrentBlock().GetY() + 1);
        }
        else {
            boolean[][] shape = currentBlock.GetCurrentBlock().GetShape();
            int x = currentBlock.GetCurrentBlock().GetX();
            int y = currentBlock.GetCurrentBlock().GetY();

            int[][] currentShapeIndex = CurrentShape.currentShapeIndex(shape); // 블럭의 현재 모양에서 1인 좌표만 추출

            for (int[] coord : currentShapeIndex) {
                int i = coord[0];
                int j = coord[1];
                board[y + i][x + j].setBlock(true); // 블럭을 보드에 고정
                board[y + i][x + j].setElementColor(currentBlock.GetCurrentBlock().GetCurrentColor()); // 블럭 색상 설정
            }
            // TODO: 바닥에 닿았을 때 새 블럭 생성 로직
            currentBlock.MoveEnd(); // 블럭이 바닥에 닿았음을 알림
        }
    }
}