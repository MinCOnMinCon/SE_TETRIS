package tetris.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.AppController;
import tetris.scoreboard.ScoreStorage;
import tetris.settings.GameSettings;
import tetris.settings.SettingsStore;

public class ScoreBoardScreenTest {
    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        SettingsStore.load();
        ScoreStorage.initialize();
        Application.launch(TestApplication.class, args);
    }

    public static class TestApplication extends Application {
        private AppController appController;

        @Override
        public void start(Stage stage) {
            Scene scene = new Scene(new BorderPane(),
                    GameSettings.getScreenWidth(), GameSettings.getScreenHeight());
            appController = new AppController(stage, scene);
            stage.setTitle("ScoreBoardScreenTest");
            stage.setScene(scene);
            stage.show();
            appController.ShowScoreBoard();
        }
    }
}
