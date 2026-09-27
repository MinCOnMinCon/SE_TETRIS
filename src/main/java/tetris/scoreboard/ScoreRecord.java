package tetris.scoreboard;

public class ScoreRecord {
    private final String name;
    private final int score;

    public ScoreRecord(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
