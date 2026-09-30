package tetris.scoreboard;

import java.io.IOException; // 파일 저장 시 예외 처리
import java.nio.file.Files; // 파일 생성/쓰기 기능
import java.nio.file.Path; // 파일 경로
import java.nio.file.StandardOpenOption; // 파일 열기 옵션
import java.util.List; // 리스트 자료형

// 점수 파일을 저장하는 클래스
public class ScoreStorage {
    // 이름과 점수를 받아 scores.txt에 한 줄씩 저장
    public void saveScore(String name, int score) { // 새 점수 저장
        // 저장할 파일 경로 지정
        Path path = Path.of("scores.txt");

        // "이름,점수" 형식으로 문자열 생성
        String line = name + "," + score + System.lineSeparator();

        try {
            // 파일이 없으면 생성하고, 있으면 기존 파일에 이어서 추가
            Files.write( // 파일 생성 후 기존 내용 뒤에 점수 추가
                path,
                List.of(line),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace(); // 저장 실패 시 에러 출력
        }
    }
}
