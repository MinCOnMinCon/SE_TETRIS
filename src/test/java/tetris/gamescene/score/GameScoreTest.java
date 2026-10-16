package tetris.gamescene.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import tetris.gamescene.GameProgress;

class GameScoreTest {
    @Test
    void addsDownAndLineClearScoresUsingCurrentLevel() {
        GameProgress progress = new GameProgress(1, 5);
        GameScore score = new GameScore(progress);

        score.AddBlockDownScore();
        score.AddLineClearScore(1);
        assertEquals(110, score.GetGameScore());

        progress.OnBlockSpawned();
        score.AddBlockDownScore();
        score.AddLineClearScore(2);
        assertEquals(725, score.GetGameScore());
    }

    @Test
    void rejectsInvalidLineCountsAndSaturatesAtIntegerMaximum() {
        GameScore score = new GameScore(new GameProgress());

        assertThrows(IllegalArgumentException.class, () -> score.AddLineClearScore(0));
        assertThrows(IllegalArgumentException.class, () -> score.AddLineClearScore(5));
        score.AddGameScore(Integer.MAX_VALUE);
        score.AddGameScore(1);
        assertEquals(Integer.MAX_VALUE, score.GetGameScore());
    }
}