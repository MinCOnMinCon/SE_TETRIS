package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class AutoMoveTest {
    @Test
    void movesBlockAfterFallIntervalAndAwardsDropScore() {
        GameProgress progress = new GameProgress();
        var board = new GameBoard().GetBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 0);
        CurrentBlock current = new CurrentBlock(data, null, null, () -> {});
        GameScore score = new GameScore(progress);
        AutoMove autoMove = new AutoMove(current, board, Difficulty.NORMAL, progress, new int[4]);

        assertEquals(1000, autoMove.GetIntervalMillis());
        autoMove.TimeUpdate(999, score);
        assertEquals(0, data.GetY());
        autoMove.TimeUpdate(1, score);

        assertEquals(1, data.GetY());
        assertEquals(10, score.GetGameScore());
    }
}
