package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class ScoreStorageTest {
    @Test
    void storesScoresInTheApplicationDataFile() {
        assertEquals("scores.csv", new ScoreStorage().scoreFile().getFileName().toString());
        assertEquals(".se_tetris", new ScoreStorage().scoreFile().getParent().getFileName().toString());
    }

    @Test
    void initializeCreatesMissingScoreFile(@TempDir Path tempDir) {
        Path file = tempDir.resolve("nested").resolve("scores.csv");

        new ScoreStorage(file).initialize();

        assertTrue(Files.exists(file));
    }

    @Test
    void savesSanitizedNamesInDescendingScoreOrder(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("scores.csv");
        ScoreStorage storage = new ScoreStorage(file);

        assertEquals(1, storage.saveScore("low", 10, Difficulty.EASY, GameMode.NORMAL));
        assertEquals(1, storage.saveScore(null, 30, Difficulty.HARD, GameMode.ITEM));
        assertEquals(2, storage.saveScore("mid,name", 20, Difficulty.NORMAL, GameMode.NORMAL));
        assertEquals(4, storage.saveScore("   ", 5, Difficulty.NORMAL, GameMode.NORMAL));

        assertEquals(List.of(
                "익명,30,HARD,ITEM",
                "mid name,20,NORMAL,NORMAL",
                "low,10,EASY,NORMAL",
                "익명,5,NORMAL,NORMAL"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void initializeKeepsExistingScoresAndClearRemovesThem(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("scores.csv");
        Files.writeString(file, "keep,1,NORMAL,NORMAL\n", StandardCharsets.UTF_8);
        ScoreStorage storage = new ScoreStorage(file);

        storage.initialize();

        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("keep,1,NORMAL,NORMAL"));
        assertTrue(storage.clearScores());
        assertEquals("", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void returnsFailureWhenScoreFileCannotBeWritten(@TempDir Path tempDir) throws Exception {
        Path blockedParent = tempDir.resolve("not-a-directory");
        Files.writeString(blockedParent, "x", StandardCharsets.UTF_8);
        ScoreStorage storage = new ScoreStorage(blockedParent.resolve("scores.csv"));

        assertEquals(-1, storage.saveScore("a", 1, Difficulty.NORMAL, GameMode.NORMAL));
        assertFalse(storage.clearScores());
    }
}
