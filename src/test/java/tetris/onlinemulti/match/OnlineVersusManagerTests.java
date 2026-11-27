package tetris.onlinemulti.match;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.onlinemulti.match.OnlineVersusManager.MatchEndReason;
import tetris.onlinemulti.network.MessageType;
import tetris.onlinemulti.network.NetworkData;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.KeyBindingSettings.ActionType;

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

    // 입력: 일반·제한시간 모드의 실제 로비 연결과 양방향 DATA·PAUSE·END·FINAL_SCORE 메시지.
    // 출력: 역할, 퍼즈 플래그, 종료 요청·점수 응답별 수신 결과. 목적: 초기 제한시간과 종료 사유 검증을 함께 확인한다.
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
                managers[0] = new ManagerStub(server[0], settings, () -> {});
                managers[1] = new ManagerStub(client[0], settings, () -> {});
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
                var sendError = assertThrows(NullPointerException.class,
                        () -> managers[0].SendGameData(MessageType.END));
                assertEquals("endReason cannot be null", sendError.getMessage());
                var receiveError = assertThrows(NullPointerException.class,
                        () -> managers[0].HandleGameData(new NetworkData(MessageType.END)));
                assertEquals("reason cannot be null", receiveError.getMessage());
                managers[0].GameStart();
                managers[1].GameStart();
            });
            assertTrue(managers[0].initialDataReceived.await(5, TimeUnit.SECONDS));
            assertTrue(managers[1].initialDataReceived.await(5, TimeUnit.SECONDS));

            runOnFxThread(() -> {
                for (ManagerStub manager : managers) {
                    manager.sendMessages();
                }
            });
            assertTrue(managers[0].received.await(5, TimeUnit.SECONDS));
            assertTrue(managers[1].received.await(5, TimeUnit.SECONDS));

            runOnFxThread(() -> {
                for (ManagerStub manager : managers) {
                    assertEquals(List.of(MessageType.DATA, MessageType.PAUSE, MessageType.END, MessageType.FINAL_SCORE),
                            manager.receivedTypes);
                    assertTrue(manager.receivedOnFxThread);
                    assertFalse(manager.paused);
                    assertEquals(0, manager.endData.GetFinalScore());
                    assertEquals(manager.endData.GetFinalScore(), manager.endFinalScore);
                    assertEquals(MatchEndReason.BOARD_FULL, manager.endReason);
                    assertNull(manager.endData.GetBoard());
                    assertEquals(0, manager.finalScoreData.GetFinalScore());
                }
                assertFalse(managers[0].gameData.GetIsHost());
                assertTrue(managers[1].gameData.GetIsHost());
                assertNotNull(managers[0].gameData.GetBoard());
                assertNotNull(managers[1].gameData.GetBoard());
                assertEquals(0.0, managers[0].gameData.GetRemainingTimeMs());
                assertEquals(gameMode == GameMode.TIMELIMIT ? 180_000.0 : 0.0,
                        managers[1].gameData.GetRemainingTimeMs());
            });
        } finally {
            if (client[0] != null) client[0].close();
            if (server[0] != null) server[0].close();
        }
    }

    // 입력: 입력 핸들러가 등록된 매니저의 연결 끊김 처리를 두 번 호출한다.
    // 기대: 입력을 정리하고 복귀 콜백을 한 번 호출한다. 목적: 중복 종료를 검증한다.
    @Test
    void connectionLostCleansInputAndReturnsToLobbyOnce() throws Exception {
        runOnFxThread(() -> {
            var settings = new GameSettings("1280x720", 1280, 720, 0, Map.of(),
                    Difficulty.NORMAL, GameMode.NORMAL);
            var lobby = new OnlineLobbyScreen(OnlineLobbyScreen.Role.SERVER, settings.gameMode(), () -> {});
            var lobbyReturns = new AtomicInteger();
            var manager = new ManagerStub(new OnlineLobbyServer(lobby, 0), settings, lobbyReturns::incrementAndGet);
            assertNotNull(manager.GetRoot().getOnKeyPressed());

            manager.HandleConnectionLost();
            manager.HandleConnectionLost();

            assertNull(manager.GetRoot().getOnKeyPressed());
            assertEquals(1, lobbyReturns.get());
        });
    }

    // 입력: 실제 로컬 소켓으로 연결된 두 매니저에서 호스트의 왼쪽 키 핸들러를 호출한다.
    // 기대: 상태를 그린 클라이언트가 보낸 ACK로 호스트만 지연을 계산한다. 목적: 입력부터 ACK까지의 연결을 검증한다.
    @Test
    void measuresHostInputAfterTheClientRendersIt() throws Exception {
        var settings = new GameSettings("1280x720", 1280, 720, 0,
                Map.of(ActionType.MOVE_LEFT, List.of(KeyCode.LEFT)), Difficulty.NORMAL, GameMode.NORMAL);
        OnlineLobbyServer[] server = new OnlineLobbyServer[1];
        OnlineLobbyClient[] client = new OnlineLobbyClient[1];
        OnlineVersusManager[] managers = new OnlineVersusManager[2];
        try {
            runOnFxThread(() -> {
                var serverLobby = new OnlineLobbyScreen(OnlineLobbyScreen.Role.SERVER, settings.gameMode(), () -> {});
                var clientLobby = new OnlineLobbyScreen(OnlineLobbyScreen.Role.CLIENT, settings.gameMode(), () -> {});
                server[0] = new OnlineLobbyServer(serverLobby, 0);
                server[0].start();
                client[0] = new OnlineLobbyClient("127.0.0.1", server[0].getPort(), clientLobby);
                managers[0] = new OnlineVersusManager(server[0], settings, () -> {}, root -> {}, result -> {}, () -> {});
                managers[1] = new OnlineVersusManager(client[0], settings, () -> {}, root -> {}, result -> {}, () -> {});
            });
            client[0].connect();
            long connectionDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (server[0].getPingLatency().getLatencyMs() < 0 && System.nanoTime() < connectionDeadline) {
                Thread.sleep(10);
            }
            assertTrue(server[0].getPingLatency().getLatencyMs() >= 0);
            runOnFxThread(() -> {
                managers[0].GameStart();
                managers[1].GameStart();
            });

            boolean[] sentInput = {false};
            boolean[] measured = {false};
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!measured[0] && System.nanoTime() < deadline) {
                runOnFxThread(() -> {
                    if (!sentInput[0] && managers[0].GetState() == OnlineVersusManager.MatchState.RUNNING
                            && managers[1].GetState() == OnlineVersusManager.MatchState.RUNNING) {
                        assertTrue(managers[0].GetInputRenderLatencyMs().isEmpty());
                        managers[0].GetRoot().getOnKeyPressed().handle(new KeyEvent(KeyEvent.KEY_PRESSED,
                                "", "", KeyCode.LEFT, false, false, false, false));
                        sentInput[0] = true;
                    }
                    measured[0] = managers[0].GetInputRenderLatencyMs().isPresent();
                });
                Thread.sleep(10);
            }
            assertTrue(measured[0], "호스트가 입력에 대한 렌더링 ACK를 받아야 한다");
            runOnFxThread(() -> assertTrue(managers[1].GetInputRenderLatencyMs().isEmpty()));
        } finally {
            if (client[0] != null) client[0].close();
            if (server[0] != null) server[0].close();
        }
    }

    private static final class ManagerStub extends OnlineVersusManager {
        private final CountDownLatch initialDataReceived = new CountDownLatch(1);
        private final CountDownLatch received = new CountDownLatch(4);
        private final List<MessageType> receivedTypes = new ArrayList<>();
        private boolean receivedOnFxThread = true;
        private boolean paused;
        private NetworkData gameData;
        private NetworkData endData;
        private NetworkData finalScoreData;
        private long endFinalScore;
        private MatchEndReason endReason;

        ManagerStub(OnlineLobbyServer server, GameSettings settings, Runnable onReturnToLobby) {
            super(server, settings, () -> {}, root -> {}, result -> {}, onReturnToLobby);
        }

        ManagerStub(OnlineLobbyClient client, GameSettings settings, Runnable onReturnToLobby) {
            super(client, settings, () -> {}, root -> {}, result -> {}, onReturnToLobby);
        }

        void sendMessages() {
            SendGameData(MessageType.PAUSE);
            HandleLocalEnd(MatchEndReason.BOARD_FULL, 0);
            SendGameData(MessageType.FINAL_SCORE);
        }

        @Override
        public void HandleGameData(NetworkData data) {
            Objects.requireNonNull(data);
            if (Platform.isFxApplicationThread() && data.GetMessageType() == MessageType.END) {
                endData = data;
            }
            super.HandleGameData(data);
            if (!Platform.isFxApplicationThread()) {
                return;
            }

            MessageType type = data.GetMessageType();
            if (type == MessageType.DATA) {
                gameData = data;
                initialDataReceived.countDown();
            } else if (type == MessageType.PAUSE) {
                paused = data.GetIsPaused();
            } else if (type == MessageType.FINAL_SCORE) {
                finalScoreData = data;
            } else {
                return;
            }
            receivedTypes.add(type);
            receivedOnFxThread &= Platform.isFxApplicationThread();
            received.countDown();
        }

        @Override
        public void HandleRemoteEnd(MatchEndReason reason, long finalScore) {
            super.HandleRemoteEnd(reason, finalScore);
            endReason = reason;
            endFinalScore = finalScore;
            receivedTypes.add(MessageType.END);
            receivedOnFxThread &= Platform.isFxApplicationThread();
            received.countDown();
        }

        @Override protected void UpdateFrame(long now) {}
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
