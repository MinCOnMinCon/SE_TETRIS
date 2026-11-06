package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class ScoreLoaderTest {
    @Test
    void loadsScoresAsRecords() {
        List<ScoreRecord> scores = new ScoreLoader().loadScores();

        assertNotNull(scores);
        assertTrue(scores.stream().allMatch(record -> record.getName() != null));
    }

    @Test
    void missingFileReturnsEmptyList(@TempDir Path tempDir) {
        assertTrue(new ScoreLoader(tempDir.resolve("missing.csv")).loadScores().isEmpty());
    }

    @Test
    void readsValidLinesAndSkipsInvalidLines(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("scores.csv");
        Files.writeString(file, """
                alice,10,EASY,ITEM

                broken
                only,two
                bad,score,EASY,NORMAL
                unknown,15,NOPE,NORMAL
                bob,20,NORMAL
                """, StandardCharsets.UTF_8);

        List<ScoreRecord> scores = new ScoreLoader(file).loadScores();

        assertEquals(2, scores.size());
        assertEquals("alice", scores.get(0).getName());
        assertEquals(10, scores.get(0).getScore());
        assertEquals(Difficulty.EASY, scores.get(0).getDifficulty());
        assertEquals(GameMode.ITEM, scores.get(0).getGameMode());
        assertEquals("bob", scores.get(1).getName());
        assertEquals(20, scores.get(1).getScore());
        assertEquals(Difficulty.NORMAL, scores.get(1).getDifficulty());
        assertEquals(GameMode.NORMAL, scores.get(1).getGameMode());
    }
}
