package tetris.test;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.onlinemulti.OnlineLobbyClient;
import tetris.onlinemulti.OnlineLobbyScreen;
import tetris.onlinemulti.OnlineLobbyServer;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.SettingsStore;

// 온라인 멀티 대기실을 서버·클라이언트 창으로 각각 열어 확인한다.
public class OnlineMultiScreenTest {
    private static final GameMode TEST_MODE = GameMode.NORMAL;

    public static void main(String[] args) {
        Run(args);
    }

    public static void Run(String... args) {
        Application.launch(TestApplication.class, args);
    }

    public static class TestApplication extends Application {
        private OnlineLobbyServer server;
        private OnlineLobbyClient client;

        @Override
        public void start(Stage serverStage) throws IOException {
            SettingsStore settingsStore = new SettingsStore();
            settingsStore.load();
            GameSettings savedSettings = settingsStore.getGameSettings();
            double width = savedSettings.screenWidth();
            double height = savedSettings.screenHeight();

            OnlineLobbyScreen serverLobby = new OnlineLobbyScreen(
                    OnlineLobbyScreen.Role.SERVER, TEST_MODE, Platform::exit);
            server = new OnlineLobbyServer(serverLobby);
            server.start();

            Scene serverScene = new Scene(serverLobby.getRoot(), width, height);
            serverStage.setTitle("OnlineMultiScreenTest - Server");
            serverStage.setScene(serverScene);
            serverStage.setX(80);
            serverStage.setY(80);
            serverStage.setOnCloseRequest(event -> closeAll());
            serverStage.show();
            serverLobby.getRoot().requestFocus();

            Stage clientStage = new Stage();
            OnlineLobbyScreen clientLobby = new OnlineLobbyScreen(
                    OnlineLobbyScreen.Role.CLIENT, TEST_MODE, Platform::exit);
            BorderPane connectingRoot = new BorderPane(new Label("서버에 접속 중..."));
            connectingRoot.setStyle("-fx-background-color: black;");
            ((Label) connectingRoot.getCenter()).setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
            Scene clientScene = new Scene(connectingRoot, width, height);
            clientStage.setTitle("OnlineMultiScreenTest - Client");
            clientStage.setScene(clientScene);
            clientStage.setX(serverStage.getX() + width + 40);
            clientStage.setY(80);
            clientStage.setOnCloseRequest(event -> closeAll());
            clientStage.show();

            String host = "127.0.0.1:" + server.getPort();
            client = new OnlineLobbyClient(host, clientLobby);
            clientLobby.setOnReady(client::sendReady);

            Thread connectThread = new Thread(() -> {
                try {
                    client.connect();
                    Platform.runLater(() -> {
                        clientScene.setRoot(clientLobby.getRoot());
                        clientLobby.getRoot().requestFocus();
                    });
                } catch (IOException exception) {
                    Platform.runLater(() -> {
                        Label error = new Label("접속 실패: " + exception.getMessage());
                        error.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 18px;");
                        BorderPane errorRoot = new BorderPane(error);
                        errorRoot.setStyle("-fx-background-color: black;");
                        clientScene.setRoot(errorRoot);
                    });
                }
            }, "online-multi-screen-test-connect");
            connectThread.setDaemon(true);
            connectThread.start();
        }

        @Override
        public void stop() {
            closeAll();
        }

        private void closeAll() {
            if (client != null) {
                client.close();
                client = null;
            }
            if (server != null) {
                server.close();
                server = null;
            }
            Platform.exit();
        }
    }
}
