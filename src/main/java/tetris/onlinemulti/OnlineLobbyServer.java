package tetris.onlinemulti;

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
import tetris.settings.GameSettings.GameMode;

// 대기실 서버. 클라이언트 한 명의 준비 상태와 모드를 주고받는다.
public final class OnlineLobbyServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 7777;

    private final OnlineLobbyScreen lobby;
    private final int requestedPort;
    private ServerSocket serverSocket;
    private volatile Socket clientSocket;
    private PrintWriter clientWriter;
    private volatile ConnectionHeartbeat clientHeartbeat;
    private volatile boolean running;
    private final Object writeLock = new Object();
    private volatile Consumer<String> networkPayloadHandler = payload -> {};
    private volatile Consumer<String> incomingChatHandler;
    private volatile Runnable onMatchStart = () -> {};

    public OnlineLobbyServer(OnlineLobbyScreen lobby) {
        this(lobby, DEFAULT_PORT);
    }

    public OnlineLobbyServer(OnlineLobbyScreen lobby, int port) {
        this.lobby = lobby;
        this.requestedPort = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(requestedPort);
        running = true;
        lobby.showServerAddress(OnlineLobbyScreen.currentIpv4());
        lobby.setOnModeChanged(this::sendMode);
        lobby.setOnChat(this::sendChat);
        lobby.setOnStart(() -> {
            send("START");
            onMatchStart.run();
        });
        Thread acceptThread = new Thread(this::acceptLoop, "online-lobby-server");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void setOnMatchStart(Runnable onMatchStart) {
        this.onMatchStart = Objects.requireNonNull(onMatchStart, "onMatchStart cannot be null");
    }

    public void setIncomingChatHandler(Consumer<String> handler) {
        incomingChatHandler = handler;
    }

    public void sendChat(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        send("CHAT " + message);
    }

    public int getPort() {
        return serverSocket == null ? requestedPort : serverSocket.getLocalPort();
    }

    public void sendMode(GameMode mode) {
        send("MODE " + (mode == null ? GameMode.NORMAL.name() : mode.name()));
    }

    void setNetworkPayloadHandler(Consumer<String> handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        networkPayloadHandler = handler;
    }

    void sendNetworkPayload(String payload) throws IOException {
        synchronized (writeLock) {
            Objects.requireNonNull(clientWriter, "Lobby client is not connected.");
            clientWriter.println("DATA " + payload);
            if (clientWriter.checkError()) {
                throw new IOException("Failed to send network data.");
            }
        }
    }

    @Override
    public void close() {
        running = false;
        if (clientHeartbeat != null) {
            clientHeartbeat.close();
            clientHeartbeat = null;
        }
        closeQuietly(clientSocket);
        closeQuietly(serverSocket);
        synchronized (writeLock) {
            clientWriter = null;
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                closeQuietly(clientSocket);
                clientSocket = socket;
                synchronized (writeLock) {
                    clientWriter = new PrintWriter(
                            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                }
                ConnectionHeartbeat heartbeat = new ConnectionHeartbeat(
                        () -> sendHeartbeat(socket, "PING"),
                        () -> sendHeartbeat(socket, "PONG"),
                        () -> closeQuietly(socket));
                clientHeartbeat = heartbeat;
                heartbeat.start();
                Platform.runLater(() -> lobby.setClientConnected(true));
                sendMode(lobby.getMode());
                readClient(socket, heartbeat);
            } catch (IOException exception) {
                if (running) {
                    Platform.runLater(() -> lobby.setClientConnected(false));
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
                } else if ("READY".equals(line)) {
                    Platform.runLater(() -> lobby.setClientReady(true));
                } else if (line.startsWith("CHAT ")) {
                    String message = line.substring("CHAT ".length());
                    Platform.runLater(() -> deliverIncomingChat(message));
                } else if (line.startsWith("DATA ")) {
                    networkPayloadHandler.accept(line.substring("DATA ".length()));
                }
            }
        } finally {
            heartbeat.close();
            if (clientHeartbeat == heartbeat) {
                clientHeartbeat = null;
            }
            synchronized (writeLock) {
                if (clientSocket == socket) {
                    clientWriter = null;
                    clientSocket = null;
                }
            }
            if (running) {
                Platform.runLater(() -> {
                    lobby.setClientConnected(false);
                    lobby.addAnnouncementMessage("클라이언트 연결이 끊어졌습니다.");
                });
            } else {
                Platform.runLater(() -> lobby.setClientConnected(false));
            }
        }
    }

    private void deliverIncomingChat(String message) {
        Consumer<String> handler = incomingChatHandler;
        if (handler != null) {
            handler.accept(message);
        } else {
            lobby.addIncomingMessage(message);
        }
    }

    private void send(String line) {
        synchronized (writeLock) {
            if (clientWriter != null) {
                clientWriter.println(line);
            }
        }
    }

    private void sendHeartbeat(Socket socket, String line) {
        synchronized (writeLock) {
            if (clientSocket == socket && clientWriter != null) {
                clientWriter.println(line);
            }
        }
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
