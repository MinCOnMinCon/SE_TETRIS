package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScoreRecordTest {
    @Test
    void exposesScoreDetailsAndCanClearHighlight() {
        ScoreRecord record = new ScoreRecord("TetrisFan", 2400);

        assertEquals("TetrisFan", record.getName());
        assertEquals(2400, record.getScore());
        assertTrue(record.isHighlight());
        record.setFalseHighlight();
        assertFalse(record.isHighlight());
    }
}
