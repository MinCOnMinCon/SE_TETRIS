package tetris.onlinemulti.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyServer;

public final class NetworkController {
    private static final System.Logger LOGGER = System.getLogger(NetworkController.class.getName());
    private static final int MAX_PAYLOAD_LENGTH = 1_398_104;
    private Runnable endNetwork;

    @FunctionalInterface
    private interface PayloadSender {
        void send(String payload) throws IOException;
    }

    private final PayloadSender sender;
    private final BooleanSupplier connectionStatus;
    private volatile Consumer<NetworkData> dataHandler;

    public NetworkController(OnlineLobbyServer server, Consumer<NetworkData> handler, Runnable onBack) {
        Objects.requireNonNull(server, "server cannot be null");
        sender = server::sendNetworkPayload;
        connectionStatus = server::isConnected;
        setOnDataReceived(handler);
        endNetwork = Objects.requireNonNull(onBack, "onBack cannot be null");
        server.setNetworkPayloadHandler(this::handlePayload);
    }

    public NetworkController(OnlineLobbyClient client, Consumer<NetworkData> handler, Runnable onBack) {
        Objects.requireNonNull(client, "client cannot be null");
        sender = client::sendNetworkPayload;
        connectionStatus = client::isConnected;
        setOnDataReceived(handler);
        endNetwork = Objects.requireNonNull(onBack, "onBack cannot be null");
        client.setNetworkPayloadHandler(this::handlePayload);
    }

    public boolean isConnected() {
        return connectionStatus.getAsBoolean();
    }

    public void setOnDataReceived(Consumer<NetworkData> handler) {
        dataHandler = Objects.requireNonNull(handler, "handler cannot be null");
    }

    public void send(NetworkData data) throws IOException {
        if (!connectionStatus.getAsBoolean()) {
            endNetwork.run();
            return;
        }
        Objects.requireNonNull(data, "data cannot be null");
        sender.send(encode(data));
    }

    private void handlePayload(String payload) {
        try {
            dataHandler.accept(decode(payload));
        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            LOGGER.log(System.Logger.Level.WARNING, "Unable to receive network data.", exception);
        }
    }

    private static String encode(NetworkData data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(data);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    private static NetworkData decode(String payload) throws IOException, ClassNotFoundException {
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            throw new IOException("Received network data exceeds the maximum size.");
        }
        byte[] bytes = Base64.getDecoder().decode(payload);
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            input.setObjectInputFilter(NetworkController::filterSerializedClass);
            Object value = input.readObject();
            if (!(value instanceof NetworkData data)) {
                throw new IOException("Received data is not a NetworkData instance.");
            }
            return data;
        }
    }

    private static ObjectInputFilter.Status filterSerializedClass(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > 20 || info.references() > 10_000 || info.streamBytes() > 1_048_576) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> type = info.serialClass();
        if (type == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        return isAllowedSerializedClass(type)
                ? ObjectInputFilter.Status.ALLOWED
                : ObjectInputFilter.Status.REJECTED;
    }

    private static boolean isAllowedSerializedClass(Class<?> type) {
        if (type.isPrimitive()) {
            return true;
        }
        if (type.isArray()) {
            return type == Object[].class || isAllowedSerializedClass(type.getComponentType());
        }
        return type == NetworkData.class
                || type == BlockData.class
                || type == BoardElement.class
                || type == ArrayList.class
                || type == Enum.class
                || type.isEnum();
    }
}
