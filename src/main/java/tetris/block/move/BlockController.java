package tetris.block.move;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.block.data.ItemType;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.score.GameScore;
import tetris.settings.GameSettings.Difficulty;

public class BlockController {
    private final CurrentBlock currentBlock;
    private final BlockMove blockMove;
    private final GameScore gameScore;
    private final GameProgress progress;
    private final Difficulty difficulty;

    private final int minFallInterval = 100;
    private int basicFallInterval = 1000;// 자동으로 떨어지는 기본 간격
    private int fallIntervalDecrese = 50; // 레벨당 줄어드는 낙하 간격
    private double easyBonusRatio = 0.2; // 이지 모드일때, (줄어드는 낙하 간격 * 이 비율)만큼 낙하 간격이 덜 감소된다.
    private double hardBonusRatio = 0.2; // 하드 모드일때, (줄어드는 낙하 간격 * 이 비율)만큼 낙하 간격이 더 감소된다.
    private int intervalMillis;
    private int currentTimeMillis = 0;
    private BlockData prevBlockData; // 이전 블럭 데이터 저장


    public BlockController(CurrentBlock currentBlock, GameScore gameScore, GameProgress progress, Difficulty difficulty) {
        this(currentBlock, gameScore, progress, null, difficulty);
    }

    public BlockController(CurrentBlock currentBlock, GameScore gameScore,
            GameProgress progress, Runnable onLineClear, Difficulty difficulty) {
        this.currentBlock = currentBlock;
        this.gameScore = gameScore;
        this.progress = progress;
        this.blockMove = new BlockMove(currentBlock, gameScore, progress, onLineClear);
        this.difficulty = difficulty;
    }


    public void BlockHolding() {
        currentBlock.BlockHold();
    }
    public void MoveLeft() {
        if (currentBlock.GetCanMoveH() && blockMove.IsNoBlock(-1)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() - 1);
        }
    }
    public void MoveRight() {
        if (currentBlock.GetCanMoveH() && blockMove.IsNoBlock(1)) {
            currentBlock.GetCurrentBlock().SetX(currentBlock.GetCurrentBlock().GetX() + 1);
        }
    }
    public void MoveDown() {
        blockMove.MoveDown();
    }
    public void MoveDownMax() {
        while (blockMove.IsNoBlock(0)) {
            MoveDown();
            gameScore.AddGameScore(1); // 하드 드롭의 한 칸당 추가 점수
        }
        MoveDown();
    }
    public void TimeUpdate(int msTime) {
        SetFallInterval(progress.GetLevel());
        currentTimeMillis += msTime;
        if (currentBlock != null && !currentBlock.GetCurrentBlock().equals(prevBlockData)) {
            prevBlockData = currentBlock.GetCurrentBlock(); // 이전 블럭 데이터 업데이트
            currentTimeMillis = 0; // 블럭이 바뀌면 타이머 초기화
        }
        if (currentTimeMillis >= intervalMillis) {
            currentTimeMillis -= intervalMillis; // Reset the timer
            if (currentBlock != null) {
                MoveDown();
            }
        }
    }
    public void FinishDeleteAfterEffect() {
        blockMove.FinishDeleteAfterEffect(currentBlock.GetBoard());
    }

    // 회전
    public void RotateClock() {
        Rotate(true);
    }
    public void RotateCounterClock() {
        Rotate(false);
    }

    public void Rotate(boolean isRotateRight) {
        BlockData blockData = currentBlock.GetCurrentBlock();
        // 무게추 회전 고정
        if (blockData.GetItemType()==ItemType.DEL_4VLINE)
            return;
        
        boolean[][] originalShape = blockData.GetShape();
        BlockData tmpBlockData = new BlockData(originalShape, blockData.GetX(), blockData.GetY());
        int rows = originalShape.length;       // 기존 배열의 세로 길이(y)
        int cols = originalShape[0].length;    // 기존 배열의 가로 길이(x)
        // 가로 세로 길이가 반전된 새로운 배열 생성
        boolean[][] rotatedShape = new boolean[cols][rows];

        if (isRotateRight) { // 시계방향 회전
            rotatedShape = RotateRight(originalShape);
        } else { // 반시계방향 회전
            rotatedShape = RotateLeft(originalShape);
        }
        tmpBlockData.SetShape(rotatedShape);
        
        if (IsNoBlock(tmpBlockData)) {
            blockData.SetShape(rotatedShape);
            int[] itemPos = blockData.GetItemPos();
            if (blockData.IsItem() && itemPos != null) {//만약 아이템이 있을 경우 아이템 좌표도 같이 회전
                
                int x = itemPos[0];
                int y = itemPos[1];
                if (isRotateRight) {
                    blockData.SetItemPos(new int[]{rows - 1 - y, x});
                } else {
                    blockData.SetItemPos(new int[]{y, cols - 1 - x});
                }
            }
        }
    }
        
    public boolean[][] RotateRight(boolean[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 시계방향 90도 회전 공식 적용
        boolean[][] rotatedShape = new boolean[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [x][rows - 1 - y]로 이동
                rotatedShape[x][rows - 1 - y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }

    public boolean[][] RotateLeft(boolean[][] originalShape) {
        int rows = originalShape.length;
        int cols = originalShape[0].length;
        // 반시계방향 90도 회전 공식 적용
        boolean[][] rotatedShape = new boolean[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 기존의 [y][x] 값을 새로운 배열의 [cols - 1 - x][y]로 이동
                rotatedShape[cols - 1 - x][y] = originalShape[y][x];
            }
        }
        return rotatedShape;
    }

    private boolean IsNoBlock(BlockData blockData) {
        int x = blockData.GetX();
        int y = blockData.GetY();

        int[][] currentShape = blockData.GetCurrentShapeIndex(); // 블럭의 현재 모양에서 1인 좌표만 추출

        for (int[] coord : currentShape) {
            int i = coord[0];
            int j = coord[1];

            int newX = x + j; // 좌(-1), 우(1), 아래(0)
            int newY = y + i;

            // 1. 보드 경계선(벽, 바닥) 충돌 체크
            if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20) {
                return false; // Out of bounds
            }

            // 2. 다른 블럭과의 충돌 체크
            if (currentBlock.GetBoard()[newY][newX].isBlock()) {
                return false; // 이미 다른 블럭이 존재하므로 이동 불가
            }
        }

        return true; // 아무 충돌이 없으므로 이동 가능
    }

    public CurrentBlock GetCurrentBlock() {
        return currentBlock;
    }
    public GameScore GetGameScore() {
        return gameScore;
    }
    public GameProgress GetGameProgress() {
        return progress;
    }




    public void SetFallInterval(int level) {
        if(difficulty == Difficulty.EASY){
            intervalMillis = basicFallInterval - (int)(level* (fallIntervalDecrese * (1-easyBonusRatio))); 
        }
        else if(difficulty == Difficulty.HARD){
            intervalMillis = basicFallInterval - (int)(level* (fallIntervalDecrese * (1+hardBonusRatio))); 
        }
        else{
            intervalMillis = basicFallInterval - level*fallIntervalDecrese; 
        }
        intervalMillis = Math.max(minFallInterval, intervalMillis); // 최소값보다 더 떨어지지는 않는다.
    }
    public void SetCurrentBoard(BoardElement[][] board) {
        this.currentBlock.SetBoard(board);
        this.prevBlockData = currentBlock.GetCurrentBlock();
        this.currentTimeMillis = 0;
    }
}
