package tetris.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.AppController;
import tetris.scoreboard.ScoreStorage;
import tetris.settings.GameSettings;
import tetris.settings.SettingsStore;

public class SettingsScreenTest {
    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        Application.launch(TestApplication.class, args);
    }

    public static class TestApplication extends Application {
        private AppController appController;

        @Override
        public void start(Stage stage) {
            SettingsStore settingsStore = new SettingsStore();
            settingsStore.load();
            new ScoreStorage().initialize();
            GameSettings savedSettings = settingsStore.getGameSettings();
            Scene scene = new Scene(new BorderPane(),
                    savedSettings.screenWidth(), savedSettings.screenHeight());
            appController = new AppController(stage, scene, settingsStore);
            stage.setTitle("SettingsScreenTest");
            stage.setScene(scene);
            stage.show();
            appController.ShowSettings();
        }
    }
}
