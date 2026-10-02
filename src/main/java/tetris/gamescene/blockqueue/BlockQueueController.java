package tetris.gamescene.blockqueue;

import java.util.Random;

import tetris.block.data.BlockData;
import tetris.block.blocks.BlockCreate;

/*
    BlockQueueController 클래스는 테트리스 블럭의 큐를 관리하는 클래스
    BlockQueueController(int) 큐 초기화, 색상 모드 설정
    Add() 큐에 블럭 추가
    GetNextBlock() 큐에서 블럭 제거 후 반환, 큐에 블럭 추가
    EnQ(BlockData) 큐에 블럭 추가
    DeQ() 큐에서 블럭 제거 후 반환
    GetRandomBlockIndex() 0~6 사이의 랜덤한 블럭 인덱스 반환
    GetColorMode(), SetColorMode(int) 색상 모드 반환 및 설정
    GetBlockQueue() 큐 반환
*/

public class BlockQueueController {
    private BlockQueue blockQueue;
    private int[] queueFRS = new int[3]; // [0]: front, [1]: rear, [2]: size
    private int colorMode = 0; // 0: 일반, 1: 적록, 2: 청황
    
    public BlockQueueController(int colorMode) {
        this.blockQueue = new BlockQueue();
        this.queueFRS = blockQueue.GetQueueFRS();
        this.colorMode = colorMode;
        // 초기 큐 채우기
        while (queueFRS[2] < blockQueue.GetMaxQueueSize()) {
            Add();
        }
    }
    public BlockQueueController() {
        this(0);
    }

    public void Add() {
        BlockData blockData = BlockCreate.Block(GetRandomBlockIndex(), colorMode).GetBlockData();
        EnQ(blockData);
    }
    public BlockData GetNextBlock() {
        BlockData nextBlock = DeQ();
        Add();
        return nextBlock;
    }
    
    public int GetColorMode() {
        return this.colorMode;
    }
    public void SetColorMode(int colorMode) {
        this.colorMode = colorMode;
    }
    public BlockQueue GetBlockQueue() {
        return this.blockQueue;
    }

    public void EnQ(BlockData blockData) {
        queueFRS = blockQueue.GetQueueFRS();

        if (queueFRS[2] < blockQueue.GetMaxQueueSize()) {
            blockQueue.SetBlock(blockData, queueFRS[1]);
            queueFRS[1] = (queueFRS[1] + 1) % blockQueue.GetMaxQueueSize();
            queueFRS[2]++;
            blockQueue.SetQueueFRS(queueFRS);
        }
    }

    public BlockData DeQ() {
        queueFRS = blockQueue.GetQueueFRS();
        BlockData dequeuedBlock = null;

        if (queueFRS[2] > 0) {
            dequeuedBlock = blockQueue.GetBlock(queueFRS[0]);
            queueFRS[0] = (queueFRS[0] + 1) % blockQueue.GetMaxQueueSize();
            queueFRS[2]--;
            blockQueue.SetQueueFRS(queueFRS);
        }
        return dequeuedBlock;
    }

    public int GetRandomBlockIndex() {
    Random random = new Random();
    // nextInt(7)은 0 이상 7 미만의 정수를 반환하므로 0~6이 나옵니다.
    return random.nextInt(7); 
    }
}
