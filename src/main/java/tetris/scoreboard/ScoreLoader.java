package tetris.scoreboard;

import java.io.IOException; // 파일 입출력 예외 처리
import java.nio.file.Files; // 파일 읽기/쓰기 관련 클래스
import java.nio.file.Path; // 파일 경로 표현
import java.nio.charset.StandardCharsets; // UTF-8 점수 파일 읽기
import java.util.ArrayList; // 동적 배열
import java.util.List; // 리스트 인터페이스

// 점수 파일을 읽는 클래스
public class ScoreLoader {
    // 사용자 데이터 폴더의 scores.csv에서 점수 데이터를 읽어 목록으로 반환
    public List<ScoreRecord> loadScores() { // 파일 점수 목록 로드
        // 결과를 담을 리스트 생성
        List<ScoreRecord> scores = new ArrayList<>();

        // 점수 저장 파일 경로 지정
        Path path = ScoreStorage.SCORE_FILE; // 저장 기능과 같은 사용자 점수 파일 사용

        // 파일이 없으면 빈 리스트 반환
        if (!Files.exists(path)) {
            return scores;
        }

        try {
            // 파일의 모든 줄을 읽어 리스트로 받음
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8); // 닉네임을 UTF-8로 읽음

            // 각 줄을 순회하며 이름과 점수를 파싱
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue; // 빈 줄은 건너뜀
                }

                String[] parts = line.split(","); // 이름과 점수를 쉼표로 분리
                if (parts.length >= 2) {
                    String name = parts[0].trim(); // 이름 추출
                    try {
                        int score = Integer.parseInt(parts[1].trim()); // 점수 추출
                        scores.add(new ScoreRecord(name, score)); // 닉네임과 점수를 객체로 저장
                    } catch (NumberFormatException e) {
                        System.err.println("잘못된 점수 항목을 건너뜁니다: " + line); // 손상된 줄은 무시
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // 파일 읽기 실패 시 에러 출력
        }

        return scores; // 점수 리스트 반환
    }
}
