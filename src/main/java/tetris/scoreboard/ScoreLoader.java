package tetris.scoreboard;

import java.io.IOException; // 파일 입출력 예외 처리
import java.nio.file.Files; // 파일 읽기/쓰기 관련 클래스
import java.nio.file.Path; // 파일 경로 표현
import java.util.ArrayList; // 동적 배열
import java.util.List; // 리스트 인터페이스

public class ScoreLoader {
    // scores.txt 파일에서 점수 데이터를 읽어 ScoreRecord 목록으로 반환
    public List<ScoreRecord> loadScores() {
        // 결과를 담을 리스트 생성
        List<ScoreRecord> scores = new ArrayList<>();

        // 점수 저장 파일 경로 지정
        Path path = Path.of("scores.txt");

        // 파일이 없으면 빈 리스트 반환
        if (!Files.exists(path)) {
            return scores;
        }

        try {
            // 파일의 모든 줄을 읽어 리스트로 받음
            List<String> lines = Files.readAllLines(path);

            // 각 줄을 순회하며 이름과 점수를 파싱
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue; // 빈 줄은 건너뜀
                }

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim(); // 이름 추출
                    int score = Integer.parseInt(parts[1].trim()); // 점수 추출
                    scores.add(new ScoreRecord(name, score)); // 객체로 저장
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // 파일 읽기 실패 시 에러 출력
        }

        return scores; // 점수 리스트 반환
    }
}
