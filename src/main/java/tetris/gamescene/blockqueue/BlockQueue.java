package tetris.gamescene.blockqueue;

import java.util.Random;

import tetris.block.blocks.BlockCreate;
import tetris.block.data.BlockData;

/*
    **큐는 원형 큐로 구현되어 있으며, front, rear, size를 관리하여 블럭을 추가하고 제거**
    BlockQueue 클래스는 테트리스 블럭의 큐를 관리하는 클래스
    BlockQueue() 큐 초기화
    BlockQueue(BlockData[]) 큐 초기화, 큐에 블럭 추가
    GetMaxQueueSize() 큐의 최대 크기 반환
    GetQueueFRS() 큐의 front, rear, size 반환
    SetQueueFRS(int[]) 큐의 front, rear, size 설정
    GetBlockQueue() 큐에 있는 블럭 배열 반환
    GetBlock(int) 큐에서 특정 인덱스의 블럭 반환
    SetBlock(BlockData, int) 큐에서 특정 인덱스의 블럭 설정
*/

public class BlockQueue {
    private BlockData[] blockQueue;
    private final int maxQueueSize;
    private int[] queueFRS = new int[3]; // [0]: front, [1]: rear, [2]: size
    private int colorMode = 0; // 0: 일반, 1: 적록, 2: 청황

    public BlockQueue(int colorMode) {
        this.maxQueueSize = 3;
        this.colorMode = colorMode;
        this.queueFRS[0] = 0; // front
        this.queueFRS[1] = 0; // rear
        this.queueFRS[2] = 0; // size
        this.blockQueue = new BlockData[this.maxQueueSize];
        while (this.queueFRS[2] < this.maxQueueSize) {
            BlockData blockData = BlockCreate.Block(GetRandomBlockIndex(), this.colorMode).GetBlockData();
            EnQ(blockData);
        }
    }

    public BlockQueue() {
        this(0);
    }

    public BlockData GetNextBlock() {
        BlockData nextBlock = DeQ();
        BlockData blockData = BlockCreate.Block(GetRandomBlockIndex(), this.colorMode).GetBlockData();
        EnQ(blockData);
        return nextBlock;
    }


    public void EnQ(BlockData blockData) {
        if (this.queueFRS[2] < maxQueueSize) {
            SetBlock(blockData, queueFRS[1]);
            this.queueFRS[1] = (queueFRS[1] + 1) % maxQueueSize;
            this.queueFRS[2]++;
        }
    }

    public BlockData DeQ() {
        BlockData dequeuedBlock = null;

        if (this.queueFRS[2] > 0) {
            dequeuedBlock = this.blockQueue[this.queueFRS[0]];
            this.queueFRS[0] = (this.queueFRS[0] + 1) % this.maxQueueSize;
            this.queueFRS[2]--;
        }
        return dequeuedBlock;
    }


    
    public int GetColorMode() {
        return this.colorMode;
    }
    public void SetColorMode(int colorMode) {
        this.colorMode = colorMode;
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
        return this.blockQueue;
    }

    // front부터 실제로 꺼내는 순서대로 화면에 전달할 배열을 만든다. 렌더러가 화면에 표시하기 위한 함수
    public BlockData[] GetBlocksInQueueOrder() {
        BlockData[] orderedBlocks = new BlockData[this.queueFRS[2]];
        for (int i = 0; i < orderedBlocks.length; i++) {
            orderedBlocks[i] = this.blockQueue[(this.queueFRS[0] + i) % this.maxQueueSize];
        }
        return orderedBlocks;
    }

    public BlockData GetBlock(int index) {
        return this.blockQueue[index];
    }
    public void SetBlock(BlockData blockData, int index) {
        this.blockQueue[index] = blockData;
    }

    public int GetRandomBlockIndex() {
    Random random = new Random();
    // nextInt(7)은 0 이상 7 미만의 정수를 반환하므로 0~6이 나옵니다.
    return random.nextInt(7); 
    }
}
