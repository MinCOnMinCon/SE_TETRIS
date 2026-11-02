package tetris.block.move;

import tetris.block.data.CurrentBlock;
import tetris.block.move.AutoMove;
import tetris.gamescene.GameProgress;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.GameProgress;
import tetris.block.rotate.BlockRotate;

public class BlockController {
    private final CurrentBlock currentBlock;
    private final AutoMove autoMove;
    private final GameScore gameScore;
    private final GameProgress progress;

    public BlockController(CurrentBlock currentBlock, AutoMove autoMove, GameScore gameScore, GameProgress progress) {
        this.currentBlock = currentBlock;
        this.autoMove = autoMove;
        this.gameScore = gameScore;
        this.progress = progress;
    }


    public void BlockHolding() {
        BlockMove.BlockHolding(currentBlock);
    }
    public void MoveLeft() {
        BlockMove.MoveLeft(currentBlock);
    }
    public void MoveRight() {
        BlockMove.MoveRight(currentBlock);
    }
    public void MoveDown() {
        BlockMove.MoveDown(currentBlock, gameScore, progress);
    }
    public void MoveDownMax() {
        BlockMove.MoveDownMax(currentBlock, gameScore, progress);
    }
    public void TimeUpdate(int msTime) {
        autoMove.TimeUpdate(msTime, gameScore, progress);
    }
    public void RotateClock() {
        BlockRotate.Rotate(currentBlock, true);
    }
    public void RotateCounterClock() {
        BlockRotate.Rotate(currentBlock, false);
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
