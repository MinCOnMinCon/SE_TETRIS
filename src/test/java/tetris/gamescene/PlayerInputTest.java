package tetris.gamescene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.settings.KeyBindingSettings.ActionType;

class PlayerInputTest {
    @Test
    void dispatchesConfiguredMovementAndPauseKeys() {
        GameBoard board = new GameBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(data, null, null, () -> {});
        GameScore score = new GameScore(new GameProgress());
        int[] pauseCalls = {0};
        PlayerInput input = new PlayerInput(Map.of(
                ActionType.MOVE_LEFT, List.of(KeyCode.A),
                ActionType.PAUSE, List.of(KeyCode.ESCAPE)),
                current, board, score, () -> pauseCalls[0]++);

        input.HandleKeyCode(KeyCode.A);
        input.HandleKeyCode(KeyCode.ESCAPE);
        input.HandleKeyCode(KeyCode.B);

        assertEquals(3, data.GetX());
        assertEquals(1, pauseCalls[0]);
    }

    @Test
    void ignoresConfiguredKeysWhenInputIsDisabled() {
        GameBoard board = new GameBoard();
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(data, null, null, () -> {});
        PlayerInput input = new PlayerInput(
                Map.of(ActionType.MOVE_LEFT, List.of(KeyCode.A)),
                current, board, new GameScore(new GameProgress()), () -> {});
        input.SetInputEnabled(false);

        input.HandleKeyCode(KeyCode.A);

        assertEquals(4, data.GetX());
        assertFalse(board.GetBoard()[4][4].isBlock());
    }
}
