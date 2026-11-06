package tetris.block.move;

import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.score.GameScore;
import tetris.block.rotate.BlockRotate;

public class BlockController {
    private final CurrentBlock currentBlock;
    private final AutoMove autoMove;
    private final BlockMove blockMove;
    private final BlockRotate blockRotate;
    private final GameScore gameScore;
    private final GameProgress progress;

    public BlockController(CurrentBlock currentBlock, AutoMove autoMove, GameScore gameScore, GameProgress progress) {
        this.currentBlock = currentBlock;
        this.autoMove = autoMove;
        this.gameScore = gameScore;
        this.progress = progress;
        this.blockMove = new BlockMove(currentBlock, gameScore, progress);
        this.blockRotate = new BlockRotate(currentBlock);
    }


    public void BlockHolding() {
        blockMove.BlockHolding();
    }
    public void MoveLeft() {
        blockMove.MoveLeft();
    }
    public void MoveRight() {
        blockMove.MoveRight();
    }
    public void MoveDown() {
        blockMove.MoveDown();
    }
    public void MoveDownMax() {
        blockMove.MoveDownMax();
    }
    public void TimeUpdate(int msTime) {
        autoMove.TimeUpdate(msTime, gameScore, progress, blockMove);
    }
    public void RotateClock() {
        blockRotate.Rotate(true);
    }
    public void RotateCounterClock() {
        blockRotate.Rotate(false);
    }

    public CurrentBlock GetCurrentBlock() {
        return currentBlock;
    }
    public AutoMove GetAutoMove() {
        return autoMove;
    }
    public GameScore GetGameScore() {
        return gameScore;
    }
    public GameProgress GetGameProgress() {
        return progress;
    }
}
