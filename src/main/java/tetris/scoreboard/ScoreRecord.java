package tetris.scoreboard;

// 점수 한 건을 표현하는 데이터 클래스
public class ScoreRecord {
    // 플레이어 이름
    private final String name;

    // 점수
    private final int score;

    //하이라이트 여부
    private boolean highlight;

    // 생성자: 이름과 점수 초기화
    public ScoreRecord(String name, int score) {
        this.name = name;
        this.score = score;
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

    public boolean isHighlight() {
        return highlight;
    }

    //저장된 후 하이라이트 취소
    public void setFalseHighlight() {
        this.highlight = false;
    }
}
