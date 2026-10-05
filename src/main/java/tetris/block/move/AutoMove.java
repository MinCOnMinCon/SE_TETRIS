package tetris.block.move;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;

/*
    AutoMove 클래스는 전달받은 프레임 시간을 누적하여 1초마다 블럭을 아래로 이동한다.
    새 블럭 생성 또는 홀딩으로 현재 블럭이 교체되면 누적 시간을 초기화한다.
*/

public class AutoMove {
    private static final double DROP_INTERVAL = 1.0;

    private final CurrentBlock currentBlock;
    private final BoardElement[][] currentBoard;

    private BlockData previousBlock;
    private double elapsedTime;

    public AutoMove(CurrentBlock currentBlock, BoardElement[][] board) {
        this.currentBlock = currentBlock;
        this.currentBoard = board;
        this.previousBlock = currentBlock.GetCurrentBlock();
        this.elapsedTime = 0.0;
    }

    public void Update(double deltaTime) {
        // 입력 등으로 블럭이 교체됐다면 새 블럭은 0초부터 시작
        if (ResetIfBlockChanged()) {
            return;
        }

        elapsedTime += deltaTime;

        while (elapsedTime >= DROP_INTERVAL) {
            elapsedTime -= DROP_INTERVAL;
            BlockMove.MoveDown(currentBlock, currentBoard);

            // 내려오다가 새 블럭이 생성되면 남은 시간도 초기화
            if (ResetIfBlockChanged()) {
                break;
            }
        }
    }

    private boolean ResetIfBlockChanged() {
        BlockData block = currentBlock.GetCurrentBlock();

        if (block == previousBlock) {
            return false;
        }

        previousBlock = block;
        elapsedTime = 0.0;
        return true;
    }
}
