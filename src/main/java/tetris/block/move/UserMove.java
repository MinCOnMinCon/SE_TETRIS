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
    public static void MoveRight(CurrentBlock blockData, BoardElement[][] board) {
        BlockMove.MoveRight(blockData, board);
    }
    public static void MoveLeft(CurrentBlock blockData, BoardElement[][] board) {
        BlockMove.MoveLeft(blockData, board);
    }
    public static void MoveDown(CurrentBlock blockData, BoardElement[][] board) {
        BlockMove.MoveDown(blockData, board);
        blockData.GetGameScore().setGameScore(10); // 블럭이 아래로 이동할 때마다 점수 증가
    }
    public static void MoveDownMax(CurrentBlock blockData, BoardElement[][] board) {
        // 블럭이 맨 아래로 이동하고 AutoMove가 돌때까지 기다림
        // 바로 다음 블럭 생성 필요 시 수정 필요
        while (CanMove.IsNoBlock(0, blockData, board)) {
            BlockMove.MoveDown(blockData, board);
            blockData.GetGameScore().setGameScore(10); // 블럭이 아래로 이동할 때마다 점수 증가
        }
        blockData.GetGameScore().setGameScore(10); // 추가 점수
    }
}
