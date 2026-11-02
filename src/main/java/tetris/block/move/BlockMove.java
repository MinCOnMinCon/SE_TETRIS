package tetris.block.move;

import tetris.block.blocks.BlockDelete;
import tetris.gamescene.GameProgress;
import tetris.block.data.CurrentBlock;
import tetris.block.data.BlockData;

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
    public static void BlockHolding(CurrentBlock CurrentBlock) {
        CurrentBlock.BlockHold();
    }
    public static void MoveRight(CurrentBlock currentBlock) {
        if (currentBlock.GetCanMoveH() && IsNoBlock(1, currentBlock)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() + 1);
        }
    }
    public static void MoveLeft(CurrentBlock currentBlock) {
        if (currentBlock.GetCanMoveH() && IsNoBlock(-1, currentBlock)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() - 1);
        }
    }
    public static void MoveDownMax(CurrentBlock currentBlock, GameScore gameScore, GameProgress progress) {
        // 블럭이 맨 아래로 이동하고 AutoMove가 돌때까지 기다림
        // 바로 다음 블럭 생성 필요 시 수정 필요
        while (IsNoBlock(0, currentBlock)) {
            MoveDown(currentBlock, gameScore, progress);
            gameScore.AddGameScore(1); // 하드 드롭의 한 칸당 추가 점수
        }
        MoveDown(currentBlock, gameScore, progress);
        //gameScore.AddGameScore(10); // 추가 점수 - 그냥 스페이스 계속 누르면 무조건 이 점수는 얻을 수 있어서 주석 처리
    }


    // 실제로 한 칸 내려갔을 때만 기본 낙하 점수를 추가한다.
    public static void MoveDown(CurrentBlock currentBlock, GameScore gameScore, GameProgress progress) {
        int[] clearedRows = currentBlock.GetGameBoard().GetClearedRows();
        BoardElement[][] board = currentBlock.GetBoard();
        if (IsNoBlock(0, currentBlock)) {
            currentBlock.GetCurrentBlock().SetY(currentBlock.GetCurrentBlock().GetY() + 1);
            if (currentBlock.GetCanMoveH())
                gameScore.AddBlockDownScore();
            else
                BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
        }
        else if (currentBlock.GetCurrentBlock().IsItem()) {
            int itemX, itemY;
            switch (currentBlock.GetCurrentBlock().GetItemType()) {
                case DEL_HLINE: // 가로선 삭제
                    MoveFix(currentBlock);
                    itemY = currentBlock.GetCurrentBlock().GetY() + currentBlock.GetCurrentBlock().GetItemPos()[1];
                    BlockDelete.DeleteHLine(board, itemY);
                    int deletedRow[] = {-1};
                    deletedRow[0] = itemY;
                    gameScore.AddLineClearScore(1);
                    System.arraycopy(deletedRow, 0, clearedRows, 0, deletedRow.length); // 삭제되는 행 번호 gameboard에 넘기기
                    break;
                case DEL_4VLINE: // 무게추 세로 4줄 삭제
                    if (currentBlock.GetCanMoveH()) {
                        currentBlock.SetCanMoveH(false);
                        BlockDelete.Delete4VLines(board, currentBlock.GetCurrentBlock());
                    }
                    else {
                        currentBlock.SetCanMoveH(true);
                        MoveFix(currentBlock);
                    }
                    break;
                case DEL_VLINE: // 세로선 삭제
                    MoveFix(currentBlock);
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetCurrentBlock().GetItemPos()[0];
                    BlockDelete.DeleteVLine(board, itemX);
                    break;
                case DEL_VEMPTY: // 세로 공백 삭제
                    MoveFix(currentBlock);
                    itemX = currentBlock.GetCurrentBlock().GetX() + currentBlock.GetCurrentBlock().GetItemPos()[0];
                    BlockDelete.DeleteVEmpty(board, itemX);
                    break;
                case DEL_AEMPTY: // 주변 공백 삭제
                    MoveFix(currentBlock);
                    itemX = currentBlock.GetCurrentBlock().GetX();
                    itemY = currentBlock.GetCurrentBlock().GetY();
                    BlockDelete.DeleteAroundEmpty(board, itemX, itemY);
                    break;
                default:
                    break;
                }

            if(currentBlock.GetCanMoveH()) {
                int[] deletedRows = BlockDelete.DelCompleteLines(board, gameScore, progress);
                
                System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length);
                
                currentBlock.MoveEnd();
            }
            
        }
        else {
            // 테트리스가 바닥에 닿았을 때 블럭 고정 및 새 블럭 생성 로직
            MoveFix(currentBlock);
            int[] deletedRows = BlockDelete.DelCompleteLines(board, gameScore, progress); // 완료된 줄 삭제 + 삭제되는 행 번호 저장
            System.arraycopy(deletedRows, 0, clearedRows, 0, deletedRows.length); // 삭제되는 행 번호 gameboard에 넘기기

            currentBlock.MoveEnd();
        
        }
    }
    private static void MoveFix(CurrentBlock currentBlock) {
        BoardElement[][] board = currentBlock.GetBoard();
        BlockData currentBlockData = currentBlock.GetCurrentBlock();
        int x = currentBlockData.GetX();
        int y = currentBlockData.GetY();

        int[][] currentShapeIndex = currentBlockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출
        for (int[] coord : currentShapeIndex) {
            int i = coord[0];
            int j = coord[1];
            board[y + i][x + j].setBlock(true); // 블럭을 보드에 고정
            board[y + i][x + j].setElementColor(currentBlockData.GetCurrentColor()); // 블럭 색상 설정
            board[y + i][x + j].setPatternType(currentBlockData.GetPatternType()); // 고정 후에도 패턴 유지
        }
        currentBlock.SetFalseIsHolded();
    }
    public static boolean IsNoBlock(int direction, CurrentBlock currentBlock) {
        BlockData blockData = currentBlock.GetCurrentBlock();
        int x = blockData.GetX();
        int y = blockData.GetY();
        BoardElement[][] board = currentBlock.GetBoard();

        int[][] currentShapeIndex = blockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출

        for (int[] coord : currentShapeIndex) {
            int i = coord[0];
            int j = coord[1];

            int newX = x + j + direction; // 좌(-1), 우(1), 아래(0)
            int newY = y + i + (direction == 0 ? 1 : 0);

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
