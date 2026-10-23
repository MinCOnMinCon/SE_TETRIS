package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class GameResultTest {
    @Test
    void storesFinalScoreAndDifficulty() {
        GameResult result = new GameResult(1200, Difficulty.HARD, GameMode.NORMAL);

        assertEquals(1200, result.score());
        assertEquals(Difficulty.HARD, result.difficulty());
    }
}
