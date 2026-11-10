package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.block.move.AutoMove;
import tetris.block.move.BlockController;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.KeyBindingSettings.ActionType;

class PlayerInputTest {
    @Test
    void dispatchesConfiguredMovementAndPauseKeys() {
        GameBoard board = new GameBoard(0);
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameProgress progress = new GameProgress();
        GameScore score = new GameScore(progress);
        BlockController controller = new BlockController(
                current, new AutoMove(current, Difficulty.NORMAL), score, progress);
        int[] pauseCalls = {0};
        PlayerInput input = new PlayerInput(Map.of(
                ActionType.MOVE_LEFT, List.of(KeyCode.A),
                ActionType.PAUSE, List.of(KeyCode.ESCAPE)),
                controller, () -> pauseCalls[0]++);

        input.HandleKeyCode(KeyCode.A);
        input.HandleKeyCode(KeyCode.ESCAPE);
        input.HandleKeyCode(KeyCode.B);

        assertEquals(3, data.GetX());
        assertEquals(1, pauseCalls[0]);
    }

    @Test
    void ignoresConfiguredKeysWhenInputIsDisabled() {
        GameBoard board = new GameBoard(0);
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameProgress progress = new GameProgress();
        GameScore score = new GameScore(progress);
        BlockController controller = new BlockController(
                current, new AutoMove(current, Difficulty.NORMAL), score, progress);
        PlayerInput input = new PlayerInput(
                Map.of(ActionType.MOVE_LEFT, List.of(KeyCode.A)),
                controller, () -> {});
        input.SetInputEnabled(false);

        input.HandleKeyCode(KeyCode.A);

        assertEquals(4, data.GetX());
        assertFalse(board.GetBoard()[4][4].isBlock());
    }
}
