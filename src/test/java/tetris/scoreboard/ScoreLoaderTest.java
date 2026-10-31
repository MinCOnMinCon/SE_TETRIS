package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreLoaderTest {
    @Test
    void loadsScoresAsRecords() {
        List<ScoreRecord> scores = new ScoreLoader().loadScores();

        assertNotNull(scores);
        assertTrue(scores.stream().allMatch(record ->
                record.getName() != null && record.isHighlight()));
    }
}
