package tetris.onlinemulti.lobby;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// 로비 서버·클라이언트가 공유하는 한 줄 프로토콜 송수신 보조.
final class LobbyLineChannel {
    private final OnlineLobbyScreen lobby;
    private final Object writeLock = new Object();
    private PrintWriter writer;
    private volatile Consumer<String> networkPayloadHandler = payload -> {};
    private volatile Consumer<String> incomingChatHandler;

    LobbyLineChannel(OnlineLobbyScreen lobby) {
        this.lobby = Objects.requireNonNull(lobby, "lobby cannot be null");
    }

    void setWriter(PrintWriter writer) {
        synchronized (writeLock) {
            this.writer = writer;
        }
    }

    void clearWriter() {
        synchronized (writeLock) {
            this.writer = null;
        }
    }

    void sendLine(String line) {
        synchronized (writeLock) {
            if (writer != null) {
                writer.println(line);
            }
        }
    }

    void sendLineIf(BooleanSupplier allowed, String line) {
        synchronized (writeLock) {
            if (allowed.getAsBoolean() && writer != null) {
                writer.println(line);
            }
        }
    }

    void sendChat(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        sendLine("CHAT " + message);
    }

    void sendNetworkPayload(String payload) throws IOException {
        synchronized (writeLock) {
            Objects.requireNonNull(writer, "Lobby connection is not established.");
            writer.println("DATA " + payload);
            if (writer.checkError()) {
                throw new IOException("Failed to send network data.");
            }
        }
    }

    void setNetworkPayloadHandler(Consumer<String> handler) {
        networkPayloadHandler = Objects.requireNonNull(handler, "handler cannot be null");
    }

    void setIncomingChatHandler(Consumer<String> handler) {
        incomingChatHandler = handler;
    }

    void deliverIncomingChat(String message) {
        Consumer<String> handler = incomingChatHandler;
        if (handler != null) {
            handler.accept(message);
        } else {
            lobby.addIncomingMessage(message);
        }
    }

    void dispatchNetworkPayload(String payload) {
        networkPayloadHandler.accept(payload);
    }
}
