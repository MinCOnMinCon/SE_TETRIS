package tetris.onlinemulti;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

import javafx.scene.Parent;
import tetris.gameoverscene.VersusOverRoot;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyController;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.onlinemulti.match.OnlineVersusManager;
import tetris.onlinemulti.screen.OnlineConnectScreen;
import tetris.onlinemulti.screen.OnlineRoleScreen;
import tetris.onlinemulti.screen.RecentHostStore;
import tetris.pausescene.PauseScene;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.PlayMode;
import tetris.settings.SettingsStore;
import tetris.versusmode.VersusResult;

// 온라인 멀티 화면 전환과 경기 생명주기를 담당하고, scene 교체는 AppController에 요청한다.
public final class OnlineMultiController implements AutoCloseable {
    private static final String TITLE_ROLE = "Tetris - 온라인";
    private static final String TITLE_LOBBY = "Tetris - 대전 대기";
    private static final String TITLE_CONNECT = "Tetris - 온라인 접속";
    private static final String TITLE_MATCH = "Tetris - 온라인 대전";
    private static final String TITLE_PAUSE = "Tetris - 일시정지";
    private static final String TITLE_RESULT = "Tetris - 대전 결과";

    private final SettingsStore settingsStore;
    private final BiConsumer<String, Parent> onScreenChanged;
    private final OnlineLobbyController lobbyController = new OnlineLobbyController();
    private Runnable onBackToStart;
    private OnlineConnectScreen connectScreen;
    private OnlineVersusManager versusManager;
    private PauseScene pauseScene;

    public OnlineMultiController(SettingsStore settingsStore,
            BiConsumer<String, Parent> onScreenChanged) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore cannot be null");
        this.onScreenChanged = Objects.requireNonNull(onScreenChanged, "onScreenChanged cannot be null");
        lobbyController.setOnMatchStart(this::startOnlineMatch);
    }

    public void show(Runnable onBackToStart) {
        this.onBackToStart = Objects.requireNonNull(onBackToStart, "onBackToStart cannot be null");
        showRoleScreen(null);
    }

    public boolean isMatchActive() {
        return versusManager != null
                && versusManager.GetState() != OnlineVersusManager.MatchState.ENDED;
    }

    @Override
    public void close() {
        endMatchResources();
        lobbyController.close();
        connectScreen = null;
    }

    private void startOnlineMatch() {
        OnlineLobbyScreen lobby = lobbyController.getLobby();
        if (lobby != null) {
            settingsStore.saveGameMode(lobby.getMode());
        }
        GameSettings settings = settingsStore.gameSettingsFor(PlayMode.ONLINE);
        endMatchResources();

        OnlineLobbyServer server = lobbyController.getServer();
        OnlineLobbyClient client = lobbyController.getClient();
        if (server != null) {
            versusManager = new OnlineVersusManager(
                    server,
                    settings,
                    this::showPauseScreen,
                    this::resumeMatch,
                    this::showEndGame,
                    this::returnToLobby);
        } else if (client != null) {
            versusManager = new OnlineVersusManager(
                    client,
                    settings,
                    this::showPauseScreen,
                    this::resumeMatch,
                    this::showEndGame,
                    this::returnToLobby);
        } else {
            return;
        }

        requestScreen(TITLE_MATCH, versusManager.GetRoot());
        versusManager.GameStart();
    }

    

    private void showPauseScreen() {
        if (versusManager == null) {
            return;
        }
        pauseScene = new PauseScene(
                () -> resumeMatch(versusManager.GetRoot()),
                this::exitMatchFromPause,
                lobbyController.chatDisplayName(),
                lobbyController::sendChat);
        lobbyController.setIncomingChatHandler(pauseScene::addIncomingMessage);
        requestScreen(TITLE_PAUSE, pauseScene.GetRoot());
        pauseScene.focusChatInput();
    }

    private void resumeMatch(Parent matchRoot) {
        lobbyController.clearIncomingChatHandler();
        pauseScene = null;
        versusManager.RequestResume();
        requestScreen(TITLE_MATCH, matchRoot);
    }

    private void exitMatchFromPause() {
        lobbyController.clearIncomingChatHandler();
        endMatchResources();
        returnToStart();
    }

    private void showEndGame(VersusResult result) {
        lobbyController.clearIncomingChatHandler();
        pauseScene = null;
        VersusOverRoot versusOverRoot = new VersusOverRoot(result, this::returnToLobby);
        requestScreen(TITLE_RESULT, versusOverRoot.getRoot());
        versusOverRoot.focusBackButton();
    }

    private void returnToLobby() {
        lobbyController.clearIncomingChatHandler();
        endMatchResources();
        OnlineLobbyScreen lobby = lobbyController.getLobby();
        if (lobby == null) {
            returnToStart();
            return;
        }
        requestScreen(TITLE_LOBBY, lobby.getRoot());
        //TODO: 파라미터에 정상 종료인지 확인하는 불리언 값, 정상 종료가 아니면 아니라고 대화창에 공지 띄우기
        
    }

    private void endMatchResources() {
        if (versusManager != null) {
            versusManager = null;
        }
        pauseScene = null;
        lobbyController.clearIncomingChatHandler();
    }

    private void showRoleScreen(String errorMessage) {
        close();
        OnlineRoleScreen roleScreen = new OnlineRoleScreen(
                this::showServerLobby, this::showConnectScreen, this::returnToStart);
        roleScreen.showError(errorMessage);
        requestScreen(TITLE_ROLE, roleScreen.getRoot());
    }

    private void showServerLobby() {
        close();
        try {
            OnlineLobbyScreen lobby = lobbyController.startServer(
                    settingsStore.getGameMode(), () -> showRoleScreen(null));
            requestScreen(TITLE_LOBBY, lobby.getRoot());
        } catch (IOException exception) {
            showRoleScreen("서버 포트를 열 수 없습니다.");
        }
    }

    private void showConnectScreen() {
        close();
        connectScreen = new OnlineConnectScreen(
                new RecentHostStore(), this::connectClient, () -> showRoleScreen(null));
        requestScreen(TITLE_CONNECT, connectScreen.getRoot());
    }

    private void connectClient(String host) {
        OnlineConnectScreen currentConnectScreen = connectScreen;
        lobbyController.connectClient(
                host,
                settingsStore.getGameMode(),
                () -> showRoleScreen(null),
                lobby -> requestScreen(TITLE_LOBBY, lobby.getRoot()),
                error -> {
                    if (connectScreen == currentConnectScreen) {
                        currentConnectScreen.showError(error);
                    }
                });
    }

    private void returnToStart() {
        endMatchResources();
        lobbyController.close();
        connectScreen = null;
        onBackToStart.run();
    }

    private void requestScreen(String title, Parent root) {
        onScreenChanged.accept(title, root);
    }
}
