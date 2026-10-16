package tetris.gamescene;

public class GameProgress {
    private final int blocksPerLevel; // 현재 레벨은 생성된 블럭 수 / 이 변수로 해서 구한다. 
    private final int maxLevel;
    private long spawnedBlockCount;
    private long clearedLineCount;
    private int madeItemCount;

    public GameProgress() { 
        this(3, 20);
    }

    public GameProgress(int blocksPerLevel, int maxLevel) {
        if (blocksPerLevel <= 0 || maxLevel < 0) {
            throw new IllegalArgumentException("blocksPerLevel must be positive and maxLevel non-negative");
        }
        this.blocksPerLevel = blocksPerLevel;
        this.maxLevel = maxLevel;
    }

    // 미리 채워둔 큐가 아니라, 큐에서 꺼내 플레이에 투입한 블럭을 센다.
    public void OnBlockSpawned() {
        if (spawnedBlockCount < Long.MAX_VALUE) {
            spawnedBlockCount++;
        }
    }

    public long GetSpawnedBlockCount() {
        return spawnedBlockCount;
    }

    // 한 번에 삭제한 줄 수를 누적한다.
    public void OnLinesCleared(int lineCount) {
        if (lineCount < 0) {
            throw new IllegalArgumentException("lineCount must be non-negative");
        }
        clearedLineCount += Math.min((long) lineCount, Long.MAX_VALUE - clearedLineCount);
    }

    public long GetClearedLineCount() {
        return clearedLineCount;
    }

    public void AddMadeItemCount(int count) {
        this.madeItemCount += count;
    }

    public int GetMadeItemCount() {
        return madeItemCount;
    }

    public int GetLevel() {
        return (int) Math.min(spawnedBlockCount / blocksPerLevel, maxLevel);
    }
}
