package tetris.block.move;

import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;

/*
    UserMove 클래스는 테트리스 블럭의 사용자에 의한 이동 기능을 제공하는 클래스
    UserMove.MoveRight(BlockData, BoardElement[][]) 블럭을 오른쪽으로 이동
    UserMove.MoveLeft(BlockData, BoardElement[][]) 블럭을 왼쪽으로 이동
    UserMove.MoveDown(BlockData, BoardElement[][]) 블럭을 아래로 이동
    
    **UserMove.MoveDownMax(BlockData, BoardElement[][]) 블럭을 맨 아래로 이동**
*/

public class UserMove {
    public static void BlockHolding(CurrentBlock CurrentBlock, BoardElement[][] board) {
        CurrentBlock.BlockHold(board);
    }
    public static void MoveRight(CurrentBlock CurrentBlock, BoardElement[][] board) {
        BlockMove.MoveRight(CurrentBlock, board);
    }
    public static void MoveLeft(CurrentBlock CurrentBlock, BoardElement[][] board) {
        BlockMove.MoveLeft(CurrentBlock, board);
    }
    public static void MoveDown(CurrentBlock CurrentBlock, BoardElement[][] board, GameScore gameScore, int[] clearedRows) {
        BlockMove.MoveDown(CurrentBlock, board, gameScore, clearedRows);
    }
    public static void MoveDownMax(CurrentBlock CurrentBlock, BoardElement[][] board, GameScore gameScore, int[] clearedRows) {
        // 블럭이 맨 아래로 이동하고 AutoMove가 돌때까지 기다림
        // 바로 다음 블럭 생성 필요 시 수정 필요
        while (CanMove.IsNoBlock(0, CurrentBlock, board)) {
            BlockMove.MoveDown(CurrentBlock, board, gameScore, clearedRows);
            gameScore.AddGameScore(1); // 하드 드롭의 한 칸당 추가 점수
        }
        //gameScore.AddGameScore(10); // 추가 점수 - 그냥 스페이스 계속 누르면 무조건 이 점수는 얻을 수 있어서 주석 처리
    }
}
