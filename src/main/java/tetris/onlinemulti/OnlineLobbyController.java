package tetris.onlinemulti;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.application.Platform;
import tetris.settings.GameSettings.GameMode;

// 온라인 대기실 화면과 서버·클라이언트 연결을 제어한다.
public final class OnlineLobbyController implements AutoCloseable {
    private OnlineLobbyScreen lobby;
    private OnlineLobbyServer server;
    private OnlineLobbyClient client;
    private Runnable onMatchStart = () -> {};

    public void setOnMatchStart(Runnable onMatchStart) {
        this.onMatchStart = Objects.requireNonNull(onMatchStart, "onMatchStart cannot be null");
        if (server != null) {
            server.setOnMatchStart(this.onMatchStart);
        }
        if (client != null) {
            client.setOnMatchStart(this.onMatchStart);
        }
    }

    public OnlineLobbyScreen startServer(GameMode initialMode, Runnable onBack) throws IOException {
        Objects.requireNonNull(onBack, "onBack cannot be null");
        close();
        lobby = new OnlineLobbyScreen(OnlineLobbyScreen.Role.SERVER, initialMode, onBack);
        server = new OnlineLobbyServer(lobby);
        server.setOnMatchStart(onMatchStart);
        server.start();
        return lobby;
    }

    public void connectClient(
            String host,
            GameMode initialMode,
            Runnable onBack,
            Consumer<OnlineLobbyScreen> onConnected,
            Consumer<String> onError) {
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(onBack, "onBack cannot be null");
        Objects.requireNonNull(onConnected, "onConnected cannot be null");
        Objects.requireNonNull(onError, "onError cannot be null");

        close();
        OnlineLobbyScreen clientLobby = new OnlineLobbyScreen(
                OnlineLobbyScreen.Role.CLIENT, initialMode, onBack);
        OnlineLobbyClient newClient;
        try {
            newClient = new OnlineLobbyClient(host, clientLobby);
        } catch (RuntimeException exception) {
            onError.accept("ip를 입력해주세요");
            return;
        }

        lobby = clientLobby;
        client = newClient;
        newClient.setOnMatchStart(onMatchStart);
        clientLobby.setOnReady(newClient::sendReady);

        Thread connectThread = new Thread(() -> {
            try {
                newClient.connect();
                new RecentHostStore().save(host);
                Platform.runLater(() -> {
                    if (client == newClient) {
                        onConnected.accept(clientLobby);
                    } else {
                        newClient.close();
                    }
                });
            } catch (IOException exception) {
                Platform.runLater(() -> {
                    newClient.close();
                    if (client == newClient) {
                        client = null;
                        lobby = null;
                        onError.accept("접속에 실패했습니다.");
                    }
                });
            }
        }, "online-lobby-connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    public OnlineLobbyScreen getLobby() {
        return lobby;
    }

    public String chatDisplayName() {
        if (lobby == null) {
            return "Player";
        }
        return lobby.getRole() == OnlineLobbyScreen.Role.SERVER ? "Server" : "Client";
    }

    public void sendChat(String message) {
        if (server != null) {
            server.sendChat(message);
        } else if (client != null) {
            client.sendChat(message);
        }
    }

    public void setIncomingChatHandler(Consumer<String> handler) {
        if (server != null) {
            server.setIncomingChatHandler(handler);
        }
        if (client != null) {
            client.setIncomingChatHandler(handler);
        }
    }

    public void clearIncomingChatHandler() {
        setIncomingChatHandler(null);
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
        lobby = null;
    }
}
