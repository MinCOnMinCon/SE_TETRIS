package tetris.block.move;

import tetris.block.blocks.BlockDelete;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.block.data.CurrentShape;
import tetris.block.data.CurrentBlock.ItemType;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;

/*
    BlockMove 클래스는 테트리스 블럭의 기본적인 이동 기능을 제공하는 클래스
    BlockMove.MoveRight(BlockData, BoardElement[][]) 블럭을 오른쪽으로 이동
    BlockMove.MoveLeft(BlockData, BoardElement[][]) 블럭을 왼쪽으로 이동
    BlockMove.MoveDown(BlockData, BoardElement[][]) 블럭을 아래로 이동

    블럭이 바닥에 닿았을 때의 처리 로직은 TODO로 남겨두었으며, 새 블럭 생성 로직을 추가해야 함
*/

public class BlockMove {
    private static boolean canMoveH = true;
    public static void MoveRight(CurrentBlock currentBlock, BoardElement[][] board) {
        if (canMoveH && CanMove.IsNoBlock(1, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() + 1);
        }
    }
    public static void MoveLeft(CurrentBlock currentBlock, BoardElement[][] board) {
        if (canMoveH && CanMove.IsNoBlock(-1, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() - 1);
        }
    }
    // 실제로 한 칸 내려갔을 때만 기본 낙하 점수를 추가한다.
    public static void MoveDown(CurrentBlock currentBlock, BoardElement[][] board, GameScore gameScore, int[] clearedRows) {
        if (CanMove.IsNoBlock(0, currentBlock, board)) {
            currentBlock.GetCurrentBlock().SetY(currentBlock.GetCurrentBlock().GetY() + 1);
            if (canMoveH)   gameScore.AddBlockDownScore();
            else BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
        }
        else if (currentBlock.IsItem()) {
            int itemX, itemY;
            switch (currentBlock.GetItemType()) {
                case DEL_HLINE: // 가로선 삭제
                    MoveFix(currentBlock, board);
                    itemY = currentBlock.GetCurrentBlock().GetY() + currentBlock.GetItemPos()[1];
                    BlockDelete.DeleteHLine(board, itemY);
                    break;
                case DEL_4VLINE: // 무게추 세로 4줄 삭제
                    if (canMoveH) {
                        canMoveH = false;
                        BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
                    }
                    else {
                        canMoveH = true;
                        MoveFix(currentBlock, board);
                    }
                    break;
                case DEL_VLINE: // 세로선 삭제
                    MoveFix(currentBlock, board);
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetItemPos()[0];
                    BlockDelete.DeleteVLine(board, itemX);
                    break;
                case DEL_VEMPTY: // 세로 공백 삭제
                    MoveFix(currentBlock, board);
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetItemPos()[0];
                    BlockDelete.DeleteVEmpty(board, itemX);
                    break;
                case DEL_AEMPTY: // 주변 공백 삭제
                    MoveFix(currentBlock, board);
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetItemPos()[0];
                    itemY = currentBlock.GetCurrentBlock().GetY() + currentBlock.GetItemPos()[1];
                    BlockDelete.DeleteAroundEmpty(board, itemX, itemY);
                    break;
                default:
                    break;
                }

            if(currentBlock.GetItemType() != ItemType.DEL_4VLINE || canMoveH) {
                BlockDelete.DelCompleteLines(board, gameScore);
                currentBlock.MoveEnd(board);
            }
            
        }
        else {
            // 테트리스가 바닥에 닿았을 때 블럭 고정 및 새 블럭 생성 로직
            MoveFix(currentBlock, board);
            int[] deletedRows = BlockDelete.DelCompleteLines(board, gameScore); // 완료된 줄 삭제 + 삭제되는 행 번호 저장
            System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length); // 삭제되는 행 번호 gameboard에 넘기기

            currentBlock.MoveEnd(board);
        
        }
    }
    public static void MoveFix(CurrentBlock currentBlock, BoardElement[][] board) {
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
    }
}
