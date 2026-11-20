package tetris.onlinemulti;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.scene.Parent;
import tetris.onlinemulti.lobby.OnlineLobbyController;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.screen.OnlineConnectScreen;
import tetris.onlinemulti.screen.OnlineRoleScreen;
import tetris.onlinemulti.screen.RecentHostStore;
import tetris.settings.SettingsStore;

// 온라인 멀티 관련 화면을 전환하고, scene 교체는 AppController에 요청한다.
public final class OnlineMultiController implements AutoCloseable {
    private static final String TITLE_ROLE = "Tetris - 온라인";
    private static final String TITLE_LOBBY = "Tetris - 대전 대기";
    private static final String TITLE_CONNECT = "Tetris - 온라인 접속";

    private final SettingsStore settingsStore;
    private final BiConsumer<String, Parent> onScreenChanged;
    private final OnlineLobbyController lobbyController = new OnlineLobbyController();
    private Runnable onBackToStart;
    private OnlineConnectScreen connectScreen;

    public OnlineMultiController(SettingsStore settingsStore,
            BiConsumer<String, Parent> onScreenChanged,
            Runnable onMatchStart) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore cannot be null");
        this.onScreenChanged = Objects.requireNonNull(onScreenChanged, "onScreenChanged cannot be null");
        lobbyController.setOnMatchStart(
                Objects.requireNonNull(onMatchStart, "onMatchStart cannot be null"));
    }

    public void show(Runnable onBackToStart) {
        this.onBackToStart = Objects.requireNonNull(onBackToStart, "onBackToStart cannot be null");
        showRoleScreen(null);
    }

    public OnlineLobbyScreen getLobby() {
        return lobbyController.getLobby();
    }

    public boolean hasActiveLobby() {
        return getLobby() != null;
    }

    public String chatDisplayName() {
        return lobbyController.chatDisplayName();
    }

    public void sendChat(String message) {
        lobbyController.sendChat(message);
    }

    public void setIncomingChatHandler(Consumer<String> handler) {
        lobbyController.setIncomingChatHandler(handler);
    }

    public void clearIncomingChatHandler() {
        setIncomingChatHandler(null);
    }

    public void showCurrentLobby() {
        OnlineLobbyScreen lobby = getLobby();
        if (lobby == null) {
            showRoleScreen(null);
            return;
        }
        requestScreen(TITLE_LOBBY, lobby.getRoot());
    }

    @Override
    public void close() {
        lobbyController.close();
        connectScreen = null;
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
        close();
        onBackToStart.run();
    }

    private void requestScreen(String title, Parent root) {
        onScreenChanged.accept(title, root);
    }
}
