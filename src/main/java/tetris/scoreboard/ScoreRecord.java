package tetris.scoreboard;

import tetris.settings.GameSettings.Difficulty;

// 점수 한 건을 표현하는 데이터 클래스
public class ScoreRecord {
    // 플레이어 이름
    private final String name;

    // 점수
    private final int score;

    private final Difficulty difficulty;

    //하이라이트 여부
    private boolean highlight;

    public ScoreRecord(String name, int score, Difficulty difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
        this.highlight = true;
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

    public boolean isHighlight() {
        return highlight;
    }

    //저장된 후 하이라이트 취소
    public void setFalseHighlight() {
        this.highlight = false;
    }
}
