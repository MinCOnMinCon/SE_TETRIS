package tetris.onlinemulti.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javafx.application.Platform;
import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyScreen;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;

final class NetworkTestSupport {
    private static final long CONNECTION_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);
    private static final Path REPORT_DIRECTORY = Path.of("performance-reports", "network-tests");

    private NetworkTestSupport() {}

    static Session connect(Consumer<NetworkData> serverHandler,
            Consumer<NetworkData> clientHandler) throws Exception {
        Objects.requireNonNull(serverHandler, "serverHandler cannot be null");
        Objects.requireNonNull(clientHandler, "clientHandler cannot be null");
        initializeJavaFx();

        OnlineLobbyServer[] server = new OnlineLobbyServer[1];
        OnlineLobbyClient[] client = new OnlineLobbyClient[1];
        NetworkController[] serverController = new NetworkController[1];
        NetworkController[] clientController = new NetworkController[1];
        boolean connected = false;
        try {
            runOnFxThread(() -> {
                OnlineLobbyScreen serverLobby = new OnlineLobbyScreen(
                        OnlineLobbyScreen.Role.SERVER, GameMode.NORMAL, () -> {});
                OnlineLobbyScreen clientLobby = new OnlineLobbyScreen(
                        OnlineLobbyScreen.Role.CLIENT, GameMode.NORMAL, () -> {});
                server[0] = new OnlineLobbyServer(serverLobby, 0);
                server[0].start();
                // 127.0.0.1: 루프백 주소
                client[0] = new OnlineLobbyClient("127.0.0.1", server[0].getPort(), clientLobby);
                serverController[0] = new NetworkController(server[0], serverHandler, () -> {});
                clientController[0] = new NetworkController(client[0], clientHandler, () -> {});
            });

            long start = System.nanoTime();
            client[0].connect();
            long deadline = start + CONNECTION_TIMEOUT_NANOS;
            while (!server[0].isConnected() && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            long connectionTimeNanos = System.nanoTime() - start;
            assertTrue(server[0].isConnected(), "Server did not accept the client connection within 5 seconds");
            assertTrue(client[0].isConnected(), "Client did not report a connected state");
            connected = true;
            return new Session(server[0], client[0], serverController[0], clientController[0],
                    connectionTimeNanos);
        } finally {
            if (!connected) {
                if (client[0] != null) {
                    client[0].close();
                }
                if (server[0] != null) {
                    server[0].close();
                }
            }
        }
    }

    static BoardElement[][] createGameBoard() {
        BoardElement[][] board = new GameBoard(0).GetBoard();
        ColorType[] colors = {
                ColorType.BlockI, ColorType.BlockJ, ColorType.BlockL,
                ColorType.BlockO, ColorType.BlockS, ColorType.BlockT, ColorType.BlockZ
        };
        for (int row = GameBoard.boardRow - 4; row < GameBoard.boardRow; row++) {
            int gap = (row * 3 + 1) % GameBoard.boardCol;
            for (int col = 0; col < GameBoard.boardCol; col++) {
                if (col != gap) {
                    board[row][col] = new BoardElement(
                            colors[(row + col) % colors.length], true, 0);
                }
            }
        }
        return board;
    }

    static CurrentBlock createCurrentBlock(Difficulty difficulty, GameMode gameMode) {
        GameProgress progress = new GameProgress();
        BlockQueue queue = new BlockQueue(0, progress, difficulty, gameMode);
        BlockData currentBlockData = queue.GetNextBlock();
        BlockData holdingBlock = new BlockData(
                new boolean[][] {{true, true}, {true, true}}, ColorType.BlockO, 0);
        CurrentBlock currentBlock = new CurrentBlock(
                currentBlockData, queue, holdingBlock, true, false, new GameBoard(0), () -> {});
        currentBlock.SetBoard(createGameBoard());
        return currentBlock;
    }

    static void assertBoardsEqual(BoardElement[][] expected, BoardElement[][] actual) {
        assertNotNull(actual, "Received board is missing");
        assertEquals(expected.length, actual.length, "Board row count differs");
        for (int row = 0; row < expected.length; row++) {
            assertEquals(expected[row].length, actual[row].length,
                    "Board column count differs at row " + row);
            for (int col = 0; col < expected[row].length; col++) {
                assertNotNull(actual[row][col], "Board cell is missing at " + row + "," + col);
                assertEquals(expected[row][col].isBlock(), actual[row][col].isBlock(),
                        "Block state differs at " + row + "," + col);
                assertEquals(expected[row][col].getElementColorType(),
                        actual[row][col].getElementColorType(),
                        "Block color differs at " + row + "," + col);
            }
        }
    }

    static void assertCurrentBlockDataEquals(CurrentBlock expected, NetworkData actual, long score) {
        assertEquals(MessageType.DATA, actual.GetMessageType(), "Message type differs");
        assertEquals(score, actual.GetVersusScore(), "Score or sequence marker differs");
        assertBoardsEqual(expected.GetBoard(), actual.GetBoard());
        assertBlockDataEquals(expected.GetCurrentBlock(), actual.GetBlock());

        BlockData[] expectedQueue = expected.GetBlockQueue().GetBlocksInQueueOrder();
        BlockData[] actualQueue = actual.GetNextBlocks();
        assertNotNull(actualQueue, "Received next-block queue is missing");
        assertEquals(expectedQueue.length, actualQueue.length, "Next-block count differs");
        for (int i = 0; i < expectedQueue.length; i++) {
            assertBlockDataEquals(expectedQueue[i], actualQueue[i]);
        }

        assertBlockDataEquals(expected.GetBlockHolding(), actual.GetBlockHolding());
        assertNull(actual.GetVersusBoard(), "Unexpected deleted-board data");
        assertArrayEquals(expected.GetGameBoard().GetClearedRows(), actual.GetClearedRows(),
                "Cleared-row state differs");
    }

    private static void assertBlockDataEquals(BlockData expected, BlockData actual) {
        assertNotNull(actual, "Block data is missing");
        assertEquals(expected.GetColorType(), actual.GetColorType(), "Block color differs");
        assertEquals(expected.GetX(), actual.GetX(), "Block X position differs");
        assertEquals(expected.GetY(), actual.GetY(), "Block Y position differs");
        assertEquals(expected.IsItem(), actual.IsItem(), "Block item state differs");
        assertEquals(expected.GetItemType(), actual.GetItemType(), "Block item type differs");
        assertEquals(expected.GetPatternType(), actual.GetPatternType(), "Block pattern differs");
        assertArrayEquals(expected.GetItemPos(), actual.GetItemPos(), "Block item position differs");

        boolean[][] expectedShape = expected.GetShape();
        boolean[][] actualShape = actual.GetShape();
        assertEquals(expectedShape.length, actualShape.length, "Block shape height differs");
        for (int row = 0; row < expectedShape.length; row++) {
            assertArrayEquals(expectedShape[row], actualShape[row], "Block shape differs at row " + row);
        }
    }

    static void writeReport(String fileName, String contents) throws IOException {
        Files.createDirectories(REPORT_DIRECTORY);
        Files.writeString(REPORT_DIRECTORY.resolve(fileName), contents, StandardCharsets.UTF_8);
    }

    private static void initializeJavaFx() throws Exception {
        FutureTask<Void> ready = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        try {
            Platform.startup(() -> Platform.runLater(ready));
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    private static void runOnFxThread(FxAction action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }

    static final class Session implements AutoCloseable {
        final OnlineLobbyServer server;
        final OnlineLobbyClient client;
        final NetworkController serverController;
        final NetworkController clientController;
        final long connectionTimeNanos;

        Session(OnlineLobbyServer server, OnlineLobbyClient client,
                NetworkController serverController, NetworkController clientController,
                long connectionTimeNanos) {
            this.server = server;
            this.client = client;
            this.serverController = serverController;
            this.clientController = clientController;
            this.connectionTimeNanos = connectionTimeNanos;
        }

        @Override
        public void close() {
            client.close();
            server.close();
        }
    }
}
