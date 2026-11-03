package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import org.junit.jupiter.api.Test;

class BlockMoveTest {
    @Test
    void movesCurrentBlockAndAwardsSoftDropScore() {
        GameBoard board = new GameBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameProgress progress = new GameProgress();
        GameScore score = new GameScore(progress);

        BlockMove.MoveRight(current);
        BlockMove.MoveDown(current, score, progress);

        assertEquals(5, data.GetX());
        assertEquals(5, data.GetY());
        assertEquals(10, score.GetGameScore());
    }
}
