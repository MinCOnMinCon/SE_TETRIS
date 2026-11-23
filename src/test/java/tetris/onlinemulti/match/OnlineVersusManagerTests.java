package tetris.onlinemulti.match;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javafx.application.Platform;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.onlinemulti.network.MessageType;
import tetris.onlinemulti.network.NetworkData;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

class OnlineVersusManagerTests {
    @BeforeAll
    static void initializeJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    // 입력: 일반·제한시간 모드의 실제 로비 연결과 양방향 DATA·PAUSE·END 메시지.
    // 출력: 역할이 올바른 DATA와 종류별 콜백 호출. 목적: 메시지 분기, 최종 점수, 호스트의 초기 제한시간 전송을 검증한다.
    @ParameterizedTest
    @EnumSource(value = GameMode.class, names = {"NORMAL", "TIMELIMIT"})
    void constructsControllersAndRoutesMessagesFromBothLobbyConnections(GameMode gameMode) throws Exception {
        Objects.requireNonNull(gameMode);
        OnlineLobbyServer[] server = new OnlineLobbyServer[1];
        OnlineLobbyClient[] client = new OnlineLobbyClient[1];
        ManagerStub[] managers = new ManagerStub[2];
        GameSettings settings = new GameSettings("1280x720", 1280, 720, 0, Map.of(),
                Difficulty.NORMAL, gameMode);
        try {
            runOnFxThread(() -> {
                OnlineLobbyScreen serverLobby = new OnlineLobbyScreen(
                        OnlineLobbyScreen.Role.SERVER, gameMode, () -> {});
                OnlineLobbyScreen clientLobby = new OnlineLobbyScreen(
                        OnlineLobbyScreen.Role.CLIENT, gameMode, () -> {});
                server[0] = new OnlineLobbyServer(serverLobby, 0);
                server[0].start();
                client[0] = new OnlineLobbyClient("127.0.0.1", server[0].getPort(), clientLobby);
                managers[0] = new ManagerStub(server[0], settings);
                managers[1] = new ManagerStub(client[0], settings);
                assertNotNull(managers[0].GetRoot());
                assertNotNull(managers[1].GetRoot());
            });
            client[0].connect();
            long connectionDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!server[0].isConnected() && System.nanoTime() < connectionDeadline) {
                Thread.sleep(10);
            }
            assertTrue(server[0].isConnected());

            runOnFxThread(() -> {
                for (ManagerStub manager : managers) {
                    manager.sendMessages();
                }
            });
            assertTrue(managers[0].received.await(5, TimeUnit.SECONDS));
            assertTrue(managers[1].received.await(5, TimeUnit.SECONDS));

            runOnFxThread(() -> {
                for (ManagerStub manager : managers) {
                    assertEquals(List.of(MessageType.DATA, MessageType.PAUSE, MessageType.END),
                            manager.receivedTypes);
                    assertTrue(manager.receivedOnFxThread);
                    assertFalse(manager.paused);
                    assertEquals(0, manager.endData.GetFinalScore());
                    assertEquals(manager.endData.GetFinalScore(), manager.endFinalScore);
                    assertNull(manager.endData.GetOutcome());
                    assertNull(manager.endData.GetBoard());
                }
                assertFalse(managers[0].gameData.GetIsHost());
                assertTrue(managers[1].gameData.GetIsHost());
                assertNotNull(managers[0].gameData.GetBoard());
                assertNotNull(managers[1].gameData.GetVersusBoards());
                assertEquals(0.0, managers[0].gameData.GetRemainingTimeMs());
                assertEquals(gameMode == GameMode.TIMELIMIT ? 180_000.0 : 0.0,
                        managers[1].gameData.GetRemainingTimeMs());
            });
        } finally {
            if (client[0] != null) client[0].close();
            if (server[0] != null) server[0].close();
        }
    }

    private static final class ManagerStub extends OnlineVersusManager {
        private final CountDownLatch received = new CountDownLatch(3);
        private final List<MessageType> receivedTypes = new ArrayList<>();
        private boolean receivedOnFxThread = true;
        private boolean paused;
        private NetworkData gameData;
        private NetworkData endData;
        private long endFinalScore;

        ManagerStub(OnlineLobbyServer server, GameSettings settings) {
            super(server, settings, () -> {}, root -> {}, result -> {}, () -> {});
        }

        ManagerStub(OnlineLobbyClient client, GameSettings settings) {
            super(client, settings, () -> {}, root -> {}, result -> {}, () -> {});
        }

        void sendMessages() {
            SendGameData(MessageType.DATA);
            SendGameData(MessageType.PAUSE);
            SendGameData(MessageType.END);
        }

        @Override
        public void HandleGameData(NetworkData data) {
            Objects.requireNonNull(data);
            if (Platform.isFxApplicationThread() && data.GetMessageType() == MessageType.END) {
                endData = data;
            }
            super.HandleGameData(data);
            if (!Platform.isFxApplicationThread() || data.GetMessageType() != MessageType.DATA) {
                return;
            }
            gameData = data;
            receivedTypes.add(MessageType.DATA);
            receivedOnFxThread &= Platform.isFxApplicationThread();
            received.countDown();
        }

        @Override
        public void HandlePauseState(boolean paused) {
            this.paused = paused;
            receivedTypes.add(MessageType.PAUSE);
            receivedOnFxThread &= Platform.isFxApplicationThread();
            received.countDown();
        }

        @Override
        public void HandleRemoteDefeat(long finalScore) {
            endFinalScore = finalScore;
            receivedTypes.add(MessageType.END);
            receivedOnFxThread &= Platform.isFxApplicationThread();
            received.countDown();
        }

        @Override public void GameStart() {}
        @Override protected void UpdateFrame(long now) {}
        @Override protected void HandleLocalDefeat(long finalScore) {}
        @Override protected void ReportFinalScore(int finalScore) {}
        @Override public void HandleFinalScoreRequest() {}
        @Override public void RequestPause(boolean paused) {}
        @Override public void ApplyMatchResult(MatchResult result) { Objects.requireNonNull(result); }
        @Override public void ReturnToLobby() {}
        @Override public void HandleConnectionLost(String reason) {
            throw new AssertionError(Objects.requireNonNull(reason));
        }
        @Override public void close() {}
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }

    private static void runOnFxThread(FxAction action) throws Exception {
        Objects.requireNonNull(action);
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
