package tetris.test;

import java.io.IOException;
import java.util.Objects;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tetris.gameoverscene.VersusOverRoot;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.onlinemulti.match.OnlineVersusManager;
import tetris.pausescene.PauseScene;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.SettingsStore;
import tetris.versusmode.VersusResult;

// 테스트용: 같은 PC에서 게스트 준비, 호스트 시작, 양쪽 경기 진입과 조작을 확인한다.
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
        // 테스트용: 두 창의 경기 매니저를 보관해 일시정지와 창 종료에 연결한다.
        private OnlineVersusManager serverMatch;
        private OnlineVersusManager clientMatch;

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
            // 테스트용: 호스트가 START를 보낸 뒤 기존 서버 연결로 경기 화면을 연다.
            server.setOnMatchStart(() -> {
                serverMatch = new OnlineVersusManager(server, matchSettings(savedSettings, serverLobby.getMode()),
                        () -> showPause(serverScene, serverMatch), root -> showRoot(serverScene, root),
                        result -> showResult(serverScene, result, serverLobby.getRoot()),
                        () -> showRoot(serverScene, serverLobby.getRoot()));
                showRoot(serverScene, serverMatch.GetRoot());
                serverMatch.GameStart();
            });
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
            // 테스트용: 현재 서버에 READY 수신 처리가 없어 같은 프로세스의 호스트 화면에 준비 상태를 반영한다.
            clientLobby.setOnReady(() -> {
                client.sendReady();
                serverLobby.setClientReady(true);
            });
            // 테스트용: 게스트가 START를 받으면 같은 클라이언트 연결로 경기 화면을 연다.
            client.setOnMatchStart(() -> {
                clientMatch = new OnlineVersusManager(client, matchSettings(savedSettings, clientLobby.getMode()),
                        () -> showPause(clientScene, clientMatch), root -> showRoot(clientScene, root),
                        result -> showResult(clientScene, result, clientLobby.getRoot()),
                        () -> showRoot(clientScene, clientLobby.getRoot()));
                showRoot(clientScene, clientMatch.GetRoot());
                clientMatch.GameStart();
            });

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

        // 테스트용: 저장된 설정을 읽어 로비에서 선택한 모드를 적용한 경기 설정을 만든다.
        private GameSettings matchSettings(GameSettings savedSettings, GameMode mode) {
            Objects.requireNonNull(savedSettings, "savedSettings cannot be null");
            return new GameSettings(savedSettings.resolutionPreset(), savedSettings.screenWidth(),
                    savedSettings.screenHeight(), savedSettings.colorMode(), savedSettings.keyBindings(),
                    savedSettings.difficulty(), mode);
        }

        // 테스트용: 각 창에서 경기, 로비, 일시정지, 결과 화면을 교체하고 입력 포커스를 옮긴다.
        private void showRoot(Scene scene, Parent root) {
            Objects.requireNonNull(scene, "scene cannot be null");
            Objects.requireNonNull(root, "root cannot be null");
            scene.setRoot(root);
            root.requestFocus();
        }

        // 테스트용: 일시정지 화면의 재개는 기존 매니저에 위임하고 종료는 두 창을 닫는다.
        private void showPause(Scene scene, OnlineVersusManager match) {
            Objects.requireNonNull(match, "match cannot be null");
            PauseScene pause = new PauseScene(match::RequestResume, this::closeAll);
            showRoot(scene, pause.GetRoot());
        }

        // 테스트용: 기존 결과 화면을 표시하고 돌아가기 버튼을 해당 창의 로비에 연결한다.
        private void showResult(Scene scene, VersusResult result, Parent lobbyRoot) {
            Objects.requireNonNull(result, "result cannot be null");
            Objects.requireNonNull(lobbyRoot, "lobbyRoot cannot be null");
            VersusOverRoot resultScreen = new VersusOverRoot(result, () -> showRoot(scene, lobbyRoot));
            showRoot(scene, resultScreen.getRoot());
            resultScreen.focusBackButton();
        }

        @Override
        public void stop() {
            closeAll();
        }

        private void closeAll() {
            // 테스트용: 연결을 닫기 전에 두 창의 경기 루프와 입력을 정리한다.
            if (serverMatch != null) {
                serverMatch.HandleConnectionLost();
                serverMatch = null;
            }
            if (clientMatch != null) {
                clientMatch.HandleConnectionLost();
                clientMatch = null;
            }
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
