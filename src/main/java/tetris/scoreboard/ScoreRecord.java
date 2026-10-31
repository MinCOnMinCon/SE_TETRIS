package tetris.scoreboard;

import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

// 점수 한 건을 표현하는 데이터 클래스
public class ScoreRecord {
    // 플레이어 이름
    private final String name;

    // 점수
    private final int score;

    private final Difficulty difficulty;
    private final GameMode gameMode;

    public ScoreRecord(String name, int score, Difficulty difficulty, GameMode gameMode) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
        this.gameMode = gameMode == null ? GameMode.NORMAL : gameMode;
    }

    // 이름 getter
    public String getName() {
        return name;
    }

    // 점수 getter
    public int getScore() {
        return score;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
