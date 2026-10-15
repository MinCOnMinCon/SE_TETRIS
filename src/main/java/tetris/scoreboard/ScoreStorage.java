package tetris.scoreboard;

import java.io.IOException; // 파일 저장 시 예외 처리
import java.nio.file.Files; // 파일 생성/쓰기 기능
import java.nio.file.Path; // 파일 경로
import java.nio.file.StandardOpenOption; // 파일 열기 옵션
import java.util.List;
import java.nio.charset.StandardCharsets; // 닉네임을 UTF-8로 저장
import java.util.Comparator; // 점수 정렬을 위해 사용

// 점수 파일을 저장하는 클래스
public class ScoreStorage {
    static final Path SCORE_FILE = Path.of( // 설정 파일과 같은 사용자 데이터 폴더에 점수 저장
            System.getProperty("user.home"), ".se_tetris", "scores.csv");

    // 앱 시작 시 점수 파일이 없으면 빈 파일을 준비
    public static void initialize() {
        try {
            Files.createDirectories(SCORE_FILE.getParent()); // 사용자 데이터 폴더 생성
            Files.writeString(SCORE_FILE, "", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND); // 파일이 없을 때 생성
        } catch (IOException e) {
            System.err.println("점수 파일을 준비하지 못했습니다: " + e.getMessage());
        }
    }

    // 사용자 데이터 폴더의 scores.csv에 닉네임과 점수를 한 줄씩 저장
    public int saveScore(String name, int score) {
        String safeName = name == null ? "익명" : name.replace(',', ' ').trim();
        if (safeName.isEmpty()) {
            safeName = "익명";
        }

        ScoreRecord newRecord = new ScoreRecord(safeName, score);

        try {
            List<ScoreRecord> scores = new ScoreLoader().loadScores();
            scores.add(newRecord);

            scores.sort(
                Comparator.comparingInt(ScoreRecord::getScore).reversed()
            );

            Files.createDirectories(SCORE_FILE.getParent());

            List<String> lines = scores.stream()
                .map(record -> record.getName() + "," + record.getScore())
                .toList();

            Files.write(
                SCORE_FILE,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );

            return scores.indexOf(newRecord) + 1;
        } catch (IOException e) {
            System.err.println("점수를 저장하지 못했습니다: " + e.getMessage());
            return -1;
        }
    }

    public boolean clearScores() {
        try {
            Files.createDirectories(SCORE_FILE.getParent());
            Files.writeString(SCORE_FILE, "", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("점수 파일을 초기화하지 못했습니다: " + e.getMessage());
            return false;
        }
    }
}
