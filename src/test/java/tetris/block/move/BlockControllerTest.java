package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class BlockControllerTest {
    @Test
    void movesBlockAfterFallIntervalAndAwardsDropScore() {
        GameProgress progress = new GameProgress();
        GameBoard board = new GameBoard(0);
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 0);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameScore score = new GameScore(progress);
        BlockController controller = new BlockController(current, score, progress, Difficulty.NORMAL);

        controller.TimeUpdate(0);
        controller.TimeUpdate(999);
        assertEquals(0, data.GetY());
        controller.TimeUpdate(1);

        assertEquals(1, data.GetY());
        assertEquals(10, score.GetGameScore());
    }
}
