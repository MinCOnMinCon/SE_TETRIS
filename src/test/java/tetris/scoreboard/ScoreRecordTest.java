package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings.Difficulty;

class ScoreRecordTest {
    @Test
    void exposesScoreDetailsAndCanClearHighlight() {
        ScoreRecord record = new ScoreRecord("TetrisFan", 2400, Difficulty.NORMAL);

        assertEquals("TetrisFan", record.getName());
        assertEquals(2400, record.getScore());
        assertEquals(Difficulty.NORMAL, record.getDifficulty());
        assertTrue(record.isHighlight());
        record.setFalseHighlight();
        assertFalse(record.isHighlight());
    }
}
