package tetris.gamescene;

public class GameProgress {
    private final int blocksPerLevel;
    private final int maxLevel;
    private long spawnedBlockCount;

    public GameProgress() {
        this(10, 37);
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

    public int GetLevel() {
        return (int) Math.min(spawnedBlockCount / blocksPerLevel, maxLevel);
    }
}
