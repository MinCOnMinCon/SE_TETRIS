package tetris.onlinemulti;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.scene.Parent;
import tetris.settings.SettingsStore;

public final class OnlineMultiController implements AutoCloseable {
    private final SettingsStore settingsStore;
    private final BiConsumer<String, Parent> onScreenChanged;
    private Runnable onBackToStart;
    private OnlineLobbyServer server;
    private OnlineLobbyClient client;
    private OnlineConnectScreen connectScreen;

    public OnlineMultiController(SettingsStore settingsStore,
            BiConsumer<String, Parent> onScreenChanged) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore cannot be null");
        this.onScreenChanged = Objects.requireNonNull(onScreenChanged, "onScreenChanged cannot be null");
    }

    public void show(Runnable onBackToStart) {
        this.onBackToStart = Objects.requireNonNull(onBackToStart, "onBackToStart cannot be null");
        showRoleScreen(null);
    }

    @Override
    public void close() {
        if (server != null) {
            server.close();
            server = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    private void showRoleScreen(String errorMessage) {
        close();
        OnlineRoleScreen roleScreen = new OnlineRoleScreen(
                this::showServerLobby, this::showConnectScreen, this::returnToStart);
        roleScreen.showError(errorMessage);
        onScreenChanged.accept("Tetris - 온라인", roleScreen.getRoot());
    }

    private void showServerLobby() {
        close();
        OnlineLobbyScreen lobby = new OnlineLobbyScreen(
                OnlineLobbyScreen.Role.SERVER, settingsStore.getGameMode(), () -> showRoleScreen(null));
        try {
            server = new OnlineLobbyServer(lobby);
            server.start();
        } catch (IOException exception) {
            showRoleScreen("서버 포트를 열 수 없습니다.");
            return;
        }
        showLobby(lobby);
    }

    private void showConnectScreen() {
        close();
        connectScreen = new OnlineConnectScreen(
                new RecentHostStore(), this::connectClient, () -> showRoleScreen(null));
        onScreenChanged.accept("Tetris - 온라인 접속", connectScreen.getRoot());
    }

    private void connectClient(String host) {
        OnlineConnectScreen currentConnectScreen = connectScreen;
        OnlineLobbyScreen lobby = new OnlineLobbyScreen(
                OnlineLobbyScreen.Role.CLIENT, settingsStore.getGameMode(), () -> showRoleScreen(null));
        OnlineLobbyClient newClient;
        try {
            newClient = new OnlineLobbyClient(host, lobby);
        } catch (RuntimeException exception) {
            currentConnectScreen.showError("ip를 입력해주세요");
            return;
        }
        client = newClient;
        lobby.setOnReady(newClient::sendReady);
        Thread connectThread = new Thread(() -> {
            try {
                newClient.connect();
                new RecentHostStore().save(host);
                Platform.runLater(() -> {
                    if (client == newClient) {
                        showLobby(lobby);
                    } else {
                        newClient.close();
                    }
                });
            } catch (IOException exception) {
                Platform.runLater(() -> {
                    newClient.close();
                    if (client == newClient) {
                        client = null;
                        if (connectScreen == currentConnectScreen) {
                            currentConnectScreen.showError("접속에 실패했습니다.");
                        }
                    }
                });
            }
        }, "online-lobby-connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    private void returnToStart() {
        close();
        onBackToStart.run();
    }

    private void showLobby(OnlineLobbyScreen lobby) {
        onScreenChanged.accept("Tetris - 대전 대기", lobby.getRoot());
    }
}
