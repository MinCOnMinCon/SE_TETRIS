package tetris.onlinemulti.lobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.application.Platform;
import tetris.settings.GameSettings.GameMode;

// 대기실 클라이언트. 호스트의 모드를 받고 준비 상태를 보낸다.
public final class OnlineLobbyClient implements AutoCloseable {
    private final String host;
    private final int port;
    private final OnlineLobbyScreen lobby;
    private final LobbyLineChannel channel;
    private volatile Socket socket;
    private volatile ConnectionHeartbeat heartbeat;
    private volatile boolean intentionallyClosed;
    private volatile boolean connected;
    private volatile Runnable onMatchStart = () -> {};

    public OnlineLobbyClient(String hostText, OnlineLobbyScreen lobby) {
        HostAddress address = HostAddress.parse(hostText);
        this.host = address.host();
        this.port = address.port();
        this.lobby = lobby;
        this.channel = new LobbyLineChannel(lobby);
    }

    public OnlineLobbyClient(String host, int port, OnlineLobbyScreen lobby) {
        this.host = host;
        this.port = port;
        this.lobby = lobby;
        this.channel = new LobbyLineChannel(lobby);
    }

    public void connect() throws IOException {
        if (intentionallyClosed) {
            throw new SocketException("Client connection has been closed");
        }
        Socket connectedSocket = new Socket();
        socket = connectedSocket;
        if (intentionallyClosed) {
            closeQuietly(connectedSocket);
            throw new SocketException("Client connection has been closed");
        }
        connectedSocket.connect(new InetSocketAddress(host, port), 3000);
        if (intentionallyClosed) {
            closeQuietly(connectedSocket);
            throw new SocketException("Client connection has been closed");
        }
        channel.setWriter(new PrintWriter(
                new OutputStreamWriter(connectedSocket.getOutputStream(), StandardCharsets.UTF_8), true));
        connected = true;
        ConnectionHeartbeat connectionHeartbeat = new ConnectionHeartbeat(
                () -> channel.sendLineIf(() -> socket == connectedSocket, "PING"),
                () -> channel.sendLineIf(() -> socket == connectedSocket, "PONG"),
                () -> closeQuietly(connectedSocket));
        heartbeat = connectionHeartbeat;
        connectionHeartbeat.start();
        lobby.setOnChat(channel::sendChat);
        Platform.runLater(() -> lobby.showServerAddress(host));
        Thread readerThread = new Thread(
                () -> readHost(connectedSocket, connectionHeartbeat), "online-lobby-client");
        readerThread.setDaemon(true);
        readerThread.start();
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

    public void sendReady() {
        channel.sendLine("READY");
        lobby.addAnnouncementMessage("Client가 준비를 완료했습니다.");
    }

    public void setNetworkPayloadHandler(Consumer<String> handler) {
        channel.setNetworkPayloadHandler(handler);
    }

    public void sendNetworkPayload(String payload) throws IOException {
        channel.sendNetworkPayload(payload);
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        connected = false;
        intentionallyClosed = true;
        if (heartbeat != null) {
            heartbeat.close();
            heartbeat = null;
        }
        channel.clearWriter();
        closeQuietly(socket);
    }

    private void readHost(Socket connectedSocket, ConnectionHeartbeat connectionHeartbeat) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connectedSocket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (connectionHeartbeat.handleMessage(line)) {
                    continue;
                }
                if (line.startsWith("MODE ")) {
                    GameMode mode = GameMode.valueOf(line.substring("MODE ".length()).trim());
                    Platform.runLater(() -> {
                        lobby.showMode(mode);
                        lobby.addAnnouncementMessage("방장이 게임 모드를 " + mode + "로 설정했습니다.");
                    });
                } else if (line.startsWith("CHAT ")) {
                    String message = line.substring("CHAT ".length());
                    Platform.runLater(() -> channel.deliverIncomingChat(message));
                } else if ("START".equals(line)) {
                    Platform.runLater(() -> {
                        lobby.clearMessages();
                        onMatchStart.run();
                    });
                } else if (line.startsWith("DATA ")) {
                    channel.dispatchNetworkPayload(line.substring("DATA ".length()));
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            // 호스트가 닫히면 읽기를 끝낸다.
        } finally {
            connected = false;
            connectionHeartbeat.close();
            if (heartbeat == connectionHeartbeat) {
                heartbeat = null;
            }
            if (!intentionallyClosed) {
                Platform.runLater(() -> {
                    lobby.setClientConnected(false);
                    lobby.addAnnouncementMessage("서버 연결이 끊어졌습니다.");
                });
            }
        }
    }

    private void closeQuietly(Socket connectedSocket) {
        if (connectedSocket == null) {
            return;
        }
        try {
            connectedSocket.close();
        } catch (IOException exception) {
            // 이미 닫힌 접속은 무시한다.
        }
    }

    private record HostAddress(String host, int port) {
        private static HostAddress parse(String text) {
            String trimmed = text == null ? "" : text.trim();
            int colon = trimmed.lastIndexOf(':');
            if (colon > 0 && trimmed.indexOf(':') == colon) {
                String host = trimmed.substring(0, colon).trim();
                int port = Integer.parseInt(trimmed.substring(colon + 1).trim());
                return new HostAddress(host, port);
            }
            return new HostAddress(trimmed, OnlineLobbyServer.DEFAULT_PORT);
        }
    }
}
