package tetris.block.move;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;

/*
    AutoMove 클래스는 테트리스 블럭의 자동 이동 기능을 제공하는 클래스
    AutoMove(BlockData, BoardElement[][], int) 생성자: 현재 블럭과 보드 상태를 설정
        currentBlock, currentBoard, intervalMillis의 상태를 변경할 수 있는 getter/setter 제공
    TimeUpdate(int) 메서드: 일정 시간 간격으로 블럭을 아래로 이동시키는 기능 제공
*/

public class AutoMove {
    private CurrentBlock currentBlock;
    private BoardElement[][] currentBoard;
    private int level = 0; // 현재 레벨 (기본값: 0)
    private int fallSpeedLevel[] = {1000, 800, 600, 400, 200}; // 각 레벨별 블럭 낙하 속도 (밀리초 단위)
    private int intervalMillis;
    private int currentTimeMillis = 0;
    private BlockData prevBlockData; // 이전 블럭 데이터 저장

    public AutoMove(CurrentBlock currentBlock, BoardElement[][] board, int level) {
        this.currentBlock = currentBlock;
        this.prevBlockData = currentBlock.GetCurrentBlock(); // 이전 블럭 데이터 초기화
        this.currentBoard = board;
        this.intervalMillis = fallSpeedLevel[level]; // level 0부터 시작
        this.level = level;
        this.currentTimeMillis = 0; // 초기화
    }
    public AutoMove(CurrentBlock blockData, BoardElement[][] board) {
        this(blockData, board, 0); // Default level of 0
    }

    public void TimeUpdate(int msTime, GameScore gameScore) {
        currentTimeMillis += msTime;
        if (currentBlock != null && !currentBlock.GetCurrentBlock().equals(prevBlockData)) {
            prevBlockData = currentBlock.GetCurrentBlock(); // 이전 블럭 데이터 업데이트
            currentTimeMillis = 0; // 블럭이 바뀌면 타이머 초기화
        }
        if (currentTimeMillis >= intervalMillis) {
            currentTimeMillis -= intervalMillis; // Reset the timer
            if (currentBlock != null) {
                BlockMove.MoveDown(currentBlock, currentBoard, gameScore);
                gameScore.GetBlockDownScore(this.level); // 블럭이 아래로 이동할 때마다 점수 증가
            }
        } 
    }

    public void ResetTimer() {
        this.currentTimeMillis = 0;
    }

    
    public void SetFallSpeedLevel(int level) {
        this.intervalMillis = fallSpeedLevel[level - 1];
    }
    public int GetIntervalMillis() {
        return this.intervalMillis;
    }
    public void SetCurrentBlock(CurrentBlock blockData) {
        this.currentBlock = blockData;
    }
    public CurrentBlock GetCurrentBlock() {
        return this.currentBlock;
    }
    public void SetCurrentBoard(BoardElement[][] board) {
        this.currentBoard = board;
        this.prevBlockData = currentBlock.GetCurrentBlock();
        this.currentTimeMillis = 0;
    }
}
