package tetris.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScoreStorageTest {
    @Test
    void storesScoresInTheApplicationDataFile() {
        assertEquals("scores.csv", ScoreStorage.SCORE_FILE.getFileName().toString());
        assertEquals(".se_tetris", ScoreStorage.SCORE_FILE.getParent().getFileName().toString());
    }
}
