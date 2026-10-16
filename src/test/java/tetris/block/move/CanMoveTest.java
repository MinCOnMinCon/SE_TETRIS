package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.GameBoard;
import org.junit.jupiter.api.Test;

class CanMoveTest {
    @Test
    void checksBoardBoundariesAndOccupiedCells() {
        var board = new GameBoard().GetBoard();
        CurrentBlock current = new CurrentBlock(new BlockData(new boolean[][] {{true}}, 0, 0),
                null, null, () -> {});

        assertTrue(CanMove.IsNoBlock(1, current, board));
        assertFalse(CanMove.IsNoBlock(-1, current, board));
        current.GetCurrentBlock().SetX(5);
        current.GetCurrentBlock().SetY(19);
        assertFalse(CanMove.IsNoBlock(0, current, board));
        current.GetCurrentBlock().SetY(0);
        board[0][6].setBlock(true);
        assertFalse(CanMove.IsNoBlock(1, current, board));
    }

    @Test
    void allowsNextBlockWhenItsSpawnCellsAreEmpty() {
        var board = new GameBoard().GetBoard();
        BlockData nextBlock = new BlockData(
                new boolean[][] {{true, false}, {true, true}}, 3, 0);

        assertTrue(CanMove.CanGetNextBlock(nextBlock, board));
    }

    @Test
    void rejectsNextBlockWhenItsSpawnCellIsOccupied() {
        var board = new GameBoard().GetBoard();
        BlockData nextBlock = new BlockData(
                new boolean[][] {{true, false}, {true, true}}, 3, 0);
        board[1][4].setBlock(true);

        assertFalse(CanMove.CanGetNextBlock(nextBlock, board));
    }
}
