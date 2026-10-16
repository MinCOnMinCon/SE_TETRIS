package tetris.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.AppController;
import tetris.StartScreen;
import tetris.scoreboard.ScoreStorage;
import tetris.settings.GameSettings;
import tetris.settings.SettingsStore;

// 시작 메뉴를 건너뛰고 실제 AppController로 게임·일시정지·종료 화면을 확인한다.
public class GameSceneTest {
    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        SettingsStore.load();
        ScoreStorage.initialize();
        Application.launch(TestApplication.class, args);
    }

    // 실행 진입점과 JavaFX Application을 같은 파일 안에서 분리한다.
    public static class TestApplication extends Application {
        private AppController appController;

        @Override
        public void start(Stage stage) {
            Scene scene = new Scene(new BorderPane(),
                    GameSettings.getScreenWidth(), GameSettings.getScreenHeight());
            appController = new AppController(new StartScreen(stage, scene));
            stage.setTitle("GameSceneTest");
            stage.setScene(scene);
            stage.show();
            appController.ShowGame();
        }
    }
}
