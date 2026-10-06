package tetris;

import javafx.application.Application; // JavaFX 애플리케이션을 시작하는 클래스
import tetris.settings.SettingsStore; // 저장된 사용자 설정을 불러오는 클래스
import tetris.scoreboard.ScoreStorage; // 점수 저장 파일을 준비하는 클래스

// 프로그램 실행을 시작하는 진입점 클래스
public class Main {
    // Java 프로그램이 처음 실행되는 메서드
    public static void main(String[] args) {
        SettingsStore.load(); // 저장된 해상도, 색상 모드, 키 설정을 불러옴
        ScoreStorage.initialize(); // 닉네임과 점수를 기록할 사용자 파일을 준비
        Application.launch(StartScreen.class, args); // JavaFX 시작 화면을 실행
    }
}