package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class ScoreRecordTest {
    @Test
    void exposesScoreDetails() {
        ScoreRecord record = new ScoreRecord("TetrisFan", 2400, Difficulty.NORMAL, GameMode.ITEM);

        assertEquals("TetrisFan", record.getName());
        assertEquals(2400, record.getScore());
        assertEquals(Difficulty.NORMAL, record.getDifficulty());
        assertEquals(GameMode.ITEM, record.getGameMode());
    }

    @Test
    void missingGameModeBecomesNormal() {
        ScoreRecord record = new ScoreRecord("anon", 1, Difficulty.HARD, null);

        assertEquals(Difficulty.HARD, record.getDifficulty());
        assertEquals(GameMode.NORMAL, record.getGameMode());
    }
}
