package tetris.scoreboard;

import java.io.IOException; // 파일 저장 시 예외 처리
import java.nio.file.Files; // 파일 생성/쓰기 기능
import java.nio.file.Path; // 파일 경로
import java.nio.file.StandardOpenOption; // 파일 열기 옵션
import java.nio.charset.StandardCharsets; // 닉네임을 UTF-8로 저장

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
    public void saveScore(String name, int score) { // 새 점수 저장
        String safeName = name == null ? "익명" : name.replace(',', ' ').trim(); // CSV 구분자와 빈 이름 보정
        if (safeName.isEmpty()) safeName = "익명"; // 비어 있는 닉네임은 익명으로 저장
        String line = safeName + "," + score + System.lineSeparator(); // 닉네임,점수 한 줄 생성

        try {
            Files.createDirectories(SCORE_FILE.getParent()); // 저장 폴더가 없으면 생성
            Files.writeString(SCORE_FILE, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND); // 기존 점수 뒤에 추가
        } catch (IOException e) {
            System.err.println("점수를 저장하지 못했습니다: " + e.getMessage()); // 저장 실패 로그
        }
    }
}
