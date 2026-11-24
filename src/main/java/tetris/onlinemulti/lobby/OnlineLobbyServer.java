package tetris.onlinemulti.lobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.application.Platform;
import tetris.onlinemulti.network.PingLatency;
import tetris.settings.GameSettings.GameMode;

// 대기실 서버. 클라이언트 한 명의 준비 상태와 모드를 주고받는다.
public final class OnlineLobbyServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 7777;

    private final OnlineLobbyScreen lobby;
    private final LobbyLineChannel channel;
    private final PingLatency pingLatency = new PingLatency();
    private final int requestedPort;
    private ServerSocket serverSocket;
    private volatile Socket clientSocket;
    private volatile ConnectionHeartbeat clientHeartbeat;
    private volatile boolean running;
    private volatile boolean connected;
    private volatile Runnable onMatchStart = () -> {};
    private volatile Consumer<String> pingListener = text -> {};

    public OnlineLobbyServer(OnlineLobbyScreen lobby) {
        this(lobby, DEFAULT_PORT);
    }

    public OnlineLobbyServer(OnlineLobbyScreen lobby, int port) {
        this.lobby = lobby;
        this.channel = new LobbyLineChannel(lobby);
        this.requestedPort = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(requestedPort);
        running = true;
        String address = OnlineLobbyScreen.currentIpv4();
        lobby.showServerAddress(address);
        lobby.setOnModeChanged(this::sendMode);
        lobby.setOnChat(channel::sendChat);
        lobby.setOnStart(() -> {
            channel.sendLine("START");
            onMatchStart.run();
        });
        lobby.addAnnouncementMessage(
                "서버를 시작했습니다. 접속 대기 중... (" + address + ":" + getPort() + ")");
        Thread acceptThread = new Thread(this::acceptLoop, "online-lobby-server");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void setOnMatchStart(Runnable onMatchStart) {
        this.onMatchStart = Objects.requireNonNull(onMatchStart, "onMatchStart cannot be null");
    }

    public void setIncomingChatHandler(Consumer<String> handler) {
        channel.setIncomingChatHandler(handler);
    }

    public void sendChat(String message) {
        channel.sendChat(message);
    }

    public int getPort() {
        return serverSocket == null ? requestedPort : serverSocket.getLocalPort();
    }

    public void sendMode(GameMode mode) {
        channel.sendLine("MODE " + (mode == null ? GameMode.NORMAL.name() : mode.name()));
    }

    public void setNetworkPayloadHandler(Consumer<String> handler) {
        channel.setNetworkPayloadHandler(handler);
    }

    public void sendNetworkPayload(String payload) throws IOException {
        channel.sendNetworkPayload(payload);
    }

    public void setPingListener(Consumer<String> listener) {
        pingListener = listener == null ? text -> {} : listener;
    }

    public PingLatency getPingLatency() {
        return pingLatency;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        connected = false;
        running = false;
        if (clientHeartbeat != null) {
            clientHeartbeat.close();
            clientHeartbeat = null;
        }
        closeQuietly(clientSocket);
        closeQuietly(serverSocket);
        channel.clearWriter();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                closeQuietly(clientSocket);
                clientSocket = socket;
                channel.setWriter(new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true));
                connected = true;
                pingLatency.reset();
                ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                        () -> channel.sendLineIf(() -> clientSocket == socket, "PING"),
                        () -> channel.sendLineIf(() -> clientSocket == socket, "PONG"),
                        () -> closeQuietly(socket),
                        pingLatency,
                        this::publishPing);
                clientHeartbeat = heartbeat;
                heartbeat.start();
                String remote = describeRemote(socket);
                Platform.runLater(() -> {
                    lobby.setClientConnected(true);
                    lobby.updatePingDisplay(pingLatency.formatLatency());
                    lobby.addAnnouncementMessage("클라이언트 접속에 성공했습니다. (" + remote + ")");
                });
                sendMode(lobby.getMode());
                readClient(socket, heartbeat);
            } catch (IOException exception) {
                connected = false;
                if (running) {
                    Platform.runLater(() -> {
                        lobby.setClientConnected(false);
                        resetPingDisplay();
                        lobby.addAnnouncementMessage("클라이언트 접속 대기 중 오류가 발생했습니다.");
                    });
                }
            }
        }
    }

    private void readClient(Socket socket, ConnectionHeartbeat heartbeat) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                
                if (heartbeat.handleMessage(line)) {
                    continue;
                }
                if (line.startsWith("CHAT ")) {
                    String message = line.substring("CHAT ".length());
                    Platform.runLater(() -> channel.deliverIncomingChat(message));
                } else if (line.startsWith("DATA ")) {
                    channel.dispatchNetworkPayload(line.substring("DATA ".length()));
                   
                }
            }
        } finally {
            connected = false;
            heartbeat.close();
            if (clientHeartbeat == heartbeat) {
                clientHeartbeat = null;
            }
            if (clientSocket == socket) {
                channel.clearWriter();
                clientSocket = null;
            }
            Platform.runLater(() -> {
                lobby.setClientConnected(false);
                resetPingDisplay();
                if (running) {
                    lobby.addAnnouncementMessage("클라이언트 연결이 끊어졌습니다.");
                }
            });
        }
    }

    private void publishPing(String text) {
        Platform.runLater(() -> {
            lobby.updatePingDisplay(text);
            pingListener.accept(text);
        });
    }

    private void resetPingDisplay() {
        pingLatency.reset();
        lobby.updatePingDisplay(pingLatency.formatLatency());
        pingListener.accept(pingLatency.formatLatency());
    }

    private static String describeRemote(Socket socket) {
        if (socket == null || socket.getInetAddress() == null) {
            return "unknown";
        }
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception exception) {
            // 이미 닫힌 접속은 무시한다.
        }
    }
}
