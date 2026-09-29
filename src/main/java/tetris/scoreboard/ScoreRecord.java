package tetris.scoreboard;

// 점수 한 건을 표현하는 데이터 클래스
public class ScoreRecord {
    // 플레이어 이름
    private final String name;

    // 점수
    private final int score;

    // 생성자: 이름과 점수 초기화
    public ScoreRecord(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // 이름 getter
    public String getName() {
        return name;
    }

    // 점수 getter
    public int getScore() {
        return score;
    }
}
