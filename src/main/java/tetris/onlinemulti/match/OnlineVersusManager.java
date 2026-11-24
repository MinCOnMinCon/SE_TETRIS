package tetris.onlinemulti.match;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import tetris.block.data.BlockData;
import tetris.gamescene.GameController;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.onlinemulti.lobby.OnlineLobbyClient;
import tetris.onlinemulti.lobby.OnlineLobbyServer;
import tetris.onlinemulti.network.MessageType;
import tetris.onlinemulti.network.NetworkController;
import tetris.onlinemulti.network.NetworkData;
import tetris.onlinemulti.network.NetworkDataExtractor;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.GameSettings.PlayMode;
import tetris.versusmode.VersusRenderer;
import tetris.versusmode.VersusTimer;

public abstract class OnlineVersusManager implements AutoCloseable {
    public enum MatchState { READY, RUNNING, PAUSED, ENDED }
    public enum MatchOutcome { HOST_WIN, CLIENT_WIN, DRAW }

    public record MatchResult(MatchOutcome outcome, int hostScore, int clientScore) {}

    // 경기 실행과 화면 구성에 필요한 객체를 보관한다.
    private NetworkController networkController;
    private GameController localController;
    private NetworkDataExtractor dataExtractor;
    private RemotePlayerView remoteView;
    private VersusRenderer versusRenderer;
    private GameSettings settings;

    // 호스트가 제한시간 모드의 남은 시간을 관리한다.
    private final VersusTimer timer;

    // 일시정지 요청을 상위 온라인 흐름에 알리는 콜백이다.
    protected final Runnable onPauseRequested;

    // 경기 종료 후 방 복귀와 연결 이상을 상위 온라인 흐름에 전달한다.
    private Runnable onReturnToLobby;
    private Consumer<String> onConnectionLost;

    // 서버 여부와 현재 경기의 상태 및 결과를 보관한다.
    private boolean isHost;
    private MatchState state;
    private MatchResult result;
    private long lastReceivedAttackId = -1;

    // 프레임 갱신과 상태 전송 주기를 관리한다.
    private AnimationTimer updateLoop;
    private long previousFrameTime;
    //private long lastSendTime;
    //private long sendIntervalNanos; 이후 매 프레임마다 데이터 전송을 안하고 따로 할 때 필요함.
    private GameMode gameMode;

    // 호스트의 게임과 화면을 먼저 구성한 뒤 로비 서버의 연결에 수신 함수를 연결한다.
    public OnlineVersusManager(OnlineLobbyServer server, GameSettings settings,
            Runnable onPauseRequested, Runnable onReturnToLobby, Consumer<String> onConnectionLost) {
        this(settings, true, onPauseRequested, onReturnToLobby, onConnectionLost);
        Objects.requireNonNull(server, "server cannot be null");
        networkController = new NetworkController(server, this::HandleGameData,
                () -> Platform.runLater(() -> HandleConnectionLost("상대와의 연결이 끊어졌습니다.")));
    }

    // 클라이언트의 게임과 화면을 먼저 구성한 뒤 로비 클라이언트의 연결에 수신 함수를 연결한다.
    public OnlineVersusManager(OnlineLobbyClient client, GameSettings settings,
            Runnable onPauseRequested, Runnable onReturnToLobby, Consumer<String> onConnectionLost) {
        this(settings, false, onPauseRequested, onReturnToLobby, onConnectionLost);
        Objects.requireNonNull(client, "client cannot be null");
        networkController = new NetworkController(client, this::HandleGameData,
                () -> Platform.runLater(() -> HandleConnectionLost("서버와의 연결이 끊어졌습니다.")));
    }

    // 양쪽 생성자가 공유하는 경기 설정과 화면 및 아직 시작하지 않은 갱신 루프를 준비한다.
    private OnlineVersusManager(GameSettings settings, boolean isHost, Runnable onPauseRequested,
            Runnable onReturnToLobby, Consumer<String> onConnectionLost) {
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.isHost = isHost;
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested, "onPauseRequested cannot be null");
        this.onReturnToLobby = Objects.requireNonNull(onReturnToLobby, "onReturnToLobby cannot be null");
        this.onConnectionLost = Objects.requireNonNull(onConnectionLost, "onConnectionLost cannot be null");
        gameMode = settings.gameMode();
        state = MatchState.READY;
        timer = isHost ? new VersusTimer(this::HandleFinalScoreRequest) : null;

        localController = new GameController(() -> RequestPause(true),
                gameResult -> HandleLocalDefeat(gameResult.score()), settings, PlayMode.ONLINE);
        dataExtractor = new NetworkDataExtractor(localController);
        int queueSize = localController.GetRenderState().blockQueue().length;
        SceneRenderState initialRemoteState = new SceneRenderState(
                new GameBoard(settings.colorMode()).GetBoard(), null, 0, new BlockData[0], null);
        remoteView = new RemotePlayerView(new RenderConfig(settings, queueSize), initialRemoteState);
        versusRenderer = new VersusRenderer(localController.GetRoot(), remoteView.GetRoot(), settings);
        versusRenderer.GetRoot().setOnKeyPressed(
                event -> localController.playerInput.HandleKeyCode(event.getCode()));
        updateLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                UpdateFrame(now);
            }
        };
    }

    // 자기 게임과 관리자 갱신 루프를 시작한다.
    public abstract void GameStart();

    // 진행 중인 경기의 상대 화면을 갱신하고 매 프레임 자기 상태를 전송한다.
    protected void UpdateFrame(long now) {
        if (state != MatchState.RUNNING) {
            return;
        }

        double deltaTimeMs = previousFrameTime == 0
                ? 0.0
                : (now - previousFrameTime) / 1_000_000.0;
        previousFrameTime = now;

        if (timer != null && gameMode == GameMode.TIMELIMIT) {
            timer.Update(deltaTimeMs);
            if (state != MatchState.RUNNING) {
                return;
            }
            versusRenderer.RenderTime(timer.GetRemainingTimeMs()); // 서버는 타이머를 업데이트하고 화면에 표시
        }

        remoteView.Update(deltaTimeMs);
        SendGameData(MessageType.DATA);
    }

    // 공통 경기 정보와 메시지별 데이터를 채우고, 제한시간 모드의 호스트는 남은 시간도 보낸다.
    protected void SendGameData(MessageType messageType) {
        Objects.requireNonNull(messageType, "messageType cannot be null");

        NetworkData data = new NetworkData();
        data.SetMessageType(messageType);
        data.SetIsEnded(state == MatchState.ENDED);
        data.SetIsHost(isHost);
        data.SetIsPaused(state == MatchState.PAUSED);

        switch (messageType) {
            case DATA -> dataExtractor.ExtractInto(data);
            case PAUSE -> {
                // 일시정지와 재개는 공통 필드의 isPaused로 전달한다.
                // 그래서 여기는 암것도 안함.
            }
            case END -> {
                data.SetFinalScore(localController.GetScore());
                if (isHost && result != null) {
                    data.SetOutcome(result.outcome());
                    data.SetHostScore(result.hostScore());
                    data.SetClientScore(result.clientScore());
                }
            }
        }

        if (timer != null && gameMode == GameMode.TIMELIMIT) {
            data.SetRemainingTimeMs(timer.GetRemainingTimeMs());
        }

        try {
            networkController.send(data);
        } catch (IOException exception) {
            HandleConnectionLost("게임 데이터 전송에 실패했습니다.");
        }
    }

    // 수신 메시지를 JavaFX 스레드에서 확인하고 화면·공격 갱신이나 일시정지·종료 처리로 연결한다.
    public void HandleGameData(NetworkData data) {
        Objects.requireNonNull(data, "data cannot be null");
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> HandleGameData(data));
            return;
        }
        switch (Objects.requireNonNull(data.GetMessageType(), "messageType cannot be null")) {
            case DATA -> {
                if (state == MatchState.ENDED) {
                    return;
                }

                BoardElement[][] attack = data.GetVersusBoard();
                if (attack != null && data.GetAttackId() > lastReceivedAttackId) {
                    localController.AddVersusBoards(attack);
                    lastReceivedAttackId = data.GetAttackId();
                }

                SceneRenderState remoteState = new SceneRenderState(
                        data.GetBoard(), data.GetBlock(), data.GetVersusScore(),
                        data.GetNextBlocks(), data.GetBlockHolding(), data.GetPendingVersusBoard());
                remoteView.ApplyState(remoteState, data.GetEffectId(),
                        data.GetEffectRunning(), data.GetClearedRows());
                if (gameMode == GameMode.TIMELIMIT && !isHost) {
                    versusRenderer.RenderTime(data.GetRemainingTimeMs()); //클라이언트는 데이터로 남은 시간을 전송받아 표시한다.
                }
            }
            case PAUSE -> HandlePauseState(data.GetIsPaused());
            case END -> HandleRemoteDefeat(data.GetFinalScore());
        }
    }

    // 자기 게임을 정지하고 패배 사실과 최종 점수를 상대에게 알린다.
    protected abstract void HandleLocalDefeat(long finalScore);

    // 상대 패배 통보로 자기 게임을 정지하고 자기 최종 점수를 보고한다.
    public abstract void HandleRemoteDefeat(long finalScore);

    // 자기 최종 점수를 보고한다.
    protected abstract void ReportFinalScore(int finalScore);

    // 타이머 만료 또는 상대의 요청 시 자기 게임을 정지하고 최종 점수를 보고한다.
    public abstract void HandleFinalScoreRequest();

    // 자신의 일시정지 또는 재개 요청을 상대에게 전달하고 공통 정지 상태 처리로 연결한다.
    public abstract void RequestPause(boolean paused);

    // 자신의 요청이나 상대의 메시지에 따른 정지 상태를 게임과 입력 및 이펙트에 적용한다.
    public abstract void HandlePauseState(boolean paused);

    // 현재 경기의 확정 결과를 저장하고 자기 게임과 갱신 루프를 정지한 뒤 결과를 표시한다.
    public abstract void ApplyMatchResult(MatchResult result);

    // 경기 자원을 정리하고 연결을 유지한 채 방 복귀 콜백을 호출한다.
    public abstract void ReturnToLobby();

    // 연결 이상으로 경기를 중단하고 상위 온라인 흐름에 원인을 전달한다.
    public abstract void HandleConnectionLost(String reason);

    // 연결을 유지하면서 입력과 자기 게임 및 관리자 갱신 루프의 자원을 정리한다.
    @Override
    public abstract void close();

    // 자기 화면과 상대 화면 및 공통 UI를 합친 대전 화면 루트를 반환한다.
    public Parent GetRoot() {
        return versusRenderer.GetRoot();
    }
}
