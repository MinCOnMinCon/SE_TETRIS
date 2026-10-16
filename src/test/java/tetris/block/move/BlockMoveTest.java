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
        var board = new GameBoard().GetBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(data, null, null, () -> {});
        GameScore score = new GameScore(new GameProgress());

        BlockMove.MoveRight(current, board);
        BlockMove.MoveDown(current, board, score, new int[4]);

        assertEquals(5, data.GetX());
        assertEquals(5, data.GetY());
        assertEquals(10, score.GetGameScore());
    }
}
