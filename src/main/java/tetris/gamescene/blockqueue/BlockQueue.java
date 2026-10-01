package tetris.gamescene.blockqueue;

import tetris.block.data.BlockData;

/*
    BlockQueue 클래스는 테트리스 블럭의 큐를 관리하는 클래스
    BlockQueue() 큐 초기화
    BlockQueue(BlockData[]) 큐 초기화, 큐에 블럭 추가
    GetMaxQueueSize() 큐의 최대 크기 반환
    GetQueueFRS() 큐의 front, rear, size 반환
    SetQueueFRS(int[]) 큐의 front, rear, size 설정
    GetBlockQueue() 큐에 있는 블럭 배열 반환
    GetBlock(int) 큐에서 특정 인덱스의 블럭 반환
    SetBlock(BlockData, int) 큐에서 특정 인덱스의 블럭 설정
    **큐는 원형 큐로 구현되어 있으며, front, rear, size를 관리하여 블럭을 추가하고 제거**
*/

public class BlockQueue {
    private BlockData[] blockQueue;
    private final int maxQueueSize;
    private int[] queueFRS = new int[3]; // [0]: front, [1]: rear, [2]: size


    public BlockQueue() {
        this.maxQueueSize = 3;
        this.queueFRS[0] = 0; // front
        this.queueFRS[1] = 0; // rear
        this.queueFRS[2] = 0; // size
        this.blockQueue = new BlockData[this.maxQueueSize];
    }
    public BlockQueue(BlockData[] blocks) {
        this.maxQueueSize = 3;
        this.blockQueue = blocks;
        this.queueFRS[0] = 0;
        this.queueFRS[1] = blocks.length;
        this.queueFRS[2] = blocks.length;
    }

    public int GetMaxQueueSize() {
        return this.maxQueueSize;
    }

    public int[] GetQueueFRS() {
        return this.queueFRS;
    }
    public void SetQueueFRS(int[] FRS) {
        this.queueFRS = FRS;
    }

    public BlockData[] GetBlockQueue() {
        return blockQueue;
    }

    public BlockData GetBlock(int index) {
        return this.blockQueue[index];
    }
    public void SetBlock(BlockData blockData, int index) {
        this.blockQueue[index] = blockData;
    }
}
