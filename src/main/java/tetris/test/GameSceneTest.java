package tetris.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tetris.gamescene.GameController;
import tetris.pausescene.PauseScene;
import tetris.settings.SettingsStore;

// 시작 메뉴를 거치지 않고 게임 화면을 직접 확인한다.
public class GameSceneTest extends Application {
    private GameController gameController;
    private PauseScene pauseScene;

    public static void Run(String... args) {
        SettingsStore.load();
        Application.launch(GameSceneTest.class, args);
    }

    @Override
    public void start(Stage stage) {
        gameController = new GameController(() -> ShowPauseScreen(stage.getScene()),
                SettingsStore.getGameSettings());
        Scene scene = new Scene(gameController.GetRoot());
        stage.setTitle("GameSceneTest");
        stage.setScene(scene);
        stage.show();
        gameController.GetRoot().requestFocus();
        gameController.GameStart();
    }

    private void ShowPauseScreen(Scene scene) {
        if (pauseScene == null) {
            pauseScene = new PauseScene(() -> {
                scene.setRoot(gameController.GetRoot());
                gameController.GetRoot().requestFocus();
                gameController.ResumeGame();
            }, Platform::exit);
        }
        scene.setRoot(pauseScene.GetRoot());
        pauseScene.GetRoot().requestFocus();
    }

    @Override
    public void stop() {
        if (gameController != null) {
            gameController.PauseGame();
        }
    }
}
