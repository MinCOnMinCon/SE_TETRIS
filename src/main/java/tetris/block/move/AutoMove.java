package tetris.block.move;


import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.GameProgress;
import java.util.Objects;
import tetris.settings.GameSettings.Difficulty;

/*
    AutoMove 클래스는 테트리스 블럭의 자동 이동 기능을 제공하는 클래스
    AutoMove(BlockData, BoardElement[][], int) 생성자: 현재 블럭과 보드 상태를 설정
        currentBlock, currentBoard, intervalMillis의 상태를 변경할 수 있는 getter/setter 제공
    TimeUpdate(int) 메서드: 일정 시간 간격으로 블럭을 아래로 이동시키는 기능 제공
*/

public class AutoMove {
    private CurrentBlock currentBlock;
    private BoardElement[][] currentBoard;
    private final int[] clearedRows;
    private final GameProgress progress;
    private final int minFallInterval = 100;
    private int basicFallInterval = 1000;// 자동으로 떨어지는 기본 간격
    private int fallIntervalDecrese = 50; // 레벨당 줄어드는 낙하 간격
    private double easyBonusRatio = 0.2; // 이지 모드일때 줄어드는 낙하 간격 * 이 비율만큼 낙하 간격이 덜 감소된다.
    private double hardBonusRatio = 0.2; // 하드 모드일때 줄어드는 낙하 간격 * 이 비율만큼 낙하 간격이 더 감소된다.
    private Difficulty difficulty; 
    private int intervalMillis;
    private int currentTimeMillis = 0;
    private BlockData prevBlockData; // 이전 블럭 데이터 저장

    public AutoMove(CurrentBlock currentBlock, BoardElement[][] board, Difficulty difficulty, GameProgress progress, int[] clearedRows) {
        this.clearedRows = clearedRows;
        this.progress = Objects.requireNonNull(progress);
        this.currentBlock = currentBlock;
        this.prevBlockData = currentBlock.GetCurrentBlock(); // 이전 블럭 데이터 초기화
        this.currentBoard = board;
        this.difficulty = difficulty;
        SetFallInterval();
        
        this.currentTimeMillis = 0; // 초기화
    }
    

    public void TimeUpdate(int msTime, GameScore gameScore) {
        SetFallInterval();
        currentTimeMillis += msTime;
        if (currentBlock != null && !currentBlock.GetCurrentBlock().equals(prevBlockData)) {
            prevBlockData = currentBlock.GetCurrentBlock(); // 이전 블럭 데이터 업데이트
            currentTimeMillis = 0; // 블럭이 바뀌면 타이머 초기화
        }
        if (currentTimeMillis >= intervalMillis) {
            currentTimeMillis -= intervalMillis; // Reset the timer
            if (currentBlock != null) {
                BlockMove.MoveDown(currentBlock, currentBoard, gameScore, clearedRows);
            }
        } 
    }


    
    public void SetFallInterval() {
        int level = progress.GetLevel();
        if(difficulty == Difficulty.EASY){
            intervalMillis = basicFallInterval - (int)((level*fallIntervalDecrese) * (1-easyBonusRatio)); 
        }
        else if(difficulty == Difficulty.HARD){
            intervalMillis = basicFallInterval - (int)((level*fallIntervalDecrese) * (1+hardBonusRatio)); 
        }
        else{
            intervalMillis = basicFallInterval - level*fallIntervalDecrese; 
        }
        intervalMillis = Math.max(minFallInterval, intervalMillis);
    }
    public int GetIntervalMillis() {
        SetFallInterval();
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
