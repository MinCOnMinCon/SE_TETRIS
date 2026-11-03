package tetris.scoreboard;

import java.io.IOException; // 파일 저장 시 예외 처리
import java.nio.file.Files; // 파일 생성/쓰기 기능
import java.nio.file.Path; // 파일 경로
import java.nio.file.StandardOpenOption; // 파일 열기 옵션
import java.util.List;
import java.util.Objects;
import java.nio.charset.StandardCharsets; // 닉네임을 UTF-8로 저장
import java.util.Comparator; // 점수 정렬을 위해 사용
import tetris.settings.GameSettings;

// 점수 파일을 저장하는 클래스
public class ScoreStorage {
    private final Path scoreFile;

    public ScoreStorage() {
        this(Path.of(System.getProperty("user.home"), ".se_tetris", "scores.csv"));
    }

    // 테스트가 사용자 점수 파일 대신 임시 파일을 쓰도록 경로를 받는다.
    ScoreStorage(Path scoreFile) {
        this.scoreFile = Objects.requireNonNull(scoreFile);
    }

    public Path scoreFile() {
        return scoreFile;
    }

    // 앱 시작 시 점수 파일이 없으면 빈 파일을 준비
    public void initialize() {
        try {
            Files.createDirectories(scoreFile.getParent()); // 사용자 데이터 폴더 생성
            Files.writeString(scoreFile, "", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND); // 파일이 없을 때 생성
        } catch (IOException e) {
            System.err.println("점수 파일을 준비하지 못했습니다: " + e.getMessage());
        }
    }

    // 닉네임, 점수, 난이도, 게임 모드를 점수 파일에 저장하고 순위를 반환한다.
    public int saveScore(String name, int score, GameSettings.Difficulty difficulty,
            GameSettings.GameMode gameMode) {
        ScoreRecord newRecord = new ScoreRecord(sanitizeName(name), score, difficulty, gameMode);
        try {
            List<ScoreRecord> scores = new ScoreLoader(scoreFile).loadScores();
            scores.add(newRecord);
            sortByScoreDescending(scores);
            writeScores(toScoreLines(scores));
            return rankOf(scores, newRecord);
        } catch (IOException e) {
            System.err.println("점수를 저장하지 못했습니다: " + e.getMessage());
            return -1;
        }
    }

    private String sanitizeName(String name) {
        String safeName = name == null ? "익명" : name.replace(',', ' ').trim();
        if (safeName.isEmpty()) {
            safeName = "익명";
        }
        return safeName;
    }

    private void sortByScoreDescending(List<ScoreRecord> scores) {
        scores.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());
    }

    private List<String> toScoreLines(List<ScoreRecord> scores) {
        return scores.stream()
                .map(record -> record.getName() + "," + record.getScore()
                        + "," + record.getDifficulty().name()
                        + "," + record.getGameMode().name())
                .toList();
    }

    private void writeScores(List<String> lines) throws IOException {
        Files.createDirectories(scoreFile.getParent());
        Files.write(
                scoreFile,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private int rankOf(List<ScoreRecord> scores, ScoreRecord newRecord) {
        return scores.indexOf(newRecord) + 1;
    }

    public boolean clearScores() {
        try {
            Files.createDirectories(scoreFile.getParent());
            Files.writeString(scoreFile, "", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("점수 파일을 초기화하지 못했습니다: " + e.getMessage());
            return false;
        }
    }
}
