package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class GameResultTest {
    @Test
    void storesFinalScoreAndDifficulty() {
        GameResult result = new GameResult(1200, Difficulty.HARD);

        assertEquals(1200, result.score());
        assertEquals(Difficulty.HARD, result.difficulty());
    }
}
