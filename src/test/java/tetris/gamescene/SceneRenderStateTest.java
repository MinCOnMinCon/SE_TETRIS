package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;
import org.junit.jupiter.api.Test;

class SceneRenderStateTest {
    @Test
    void carriesGameStateWithoutReplacingReferences() {
        BoardElement[][] board = {{new BoardElement()}};
        BlockData current = new BlockData(new boolean[][] {{true}}, 1, 2);
        BlockData queued = new BlockData(new boolean[][] {{true}}, 3, 4);
        BlockData held = new BlockData(new boolean[][] {{true}}, 5, 6);
        SceneRenderState state = new SceneRenderState(board, current, 77L,
                new BlockData[] {queued}, held);

        assertSame(board, state.board());
        assertSame(current, state.currentBlock());
        assertEquals(77L, state.score());
        assertSame(queued, state.blockQueue()[0]);
        assertSame(held, state.blockHolding());
    }
}
