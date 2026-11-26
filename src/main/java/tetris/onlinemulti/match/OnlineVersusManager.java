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
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.GameSettings.PlayMode;
import tetris.versusmode.VersusRenderer;
import tetris.versusmode.VersusResult;
import tetris.versusmode.VersusResult.LocalWinLose;
import tetris.versusmode.VersusTimer;

public class OnlineVersusManager {
    public enum MatchState { READY, RUNNING, PAUSED, ENDING, ENDED }
    public enum MatchEndReason { BOARD_FULL, TIMEOUT }
    private final long DelayThreshold = 150;

    // 경기 실행과 화면 구성에 필요한 객체를 보관한다.
    private NetworkController networkController;
    private GameController localController;
    private NetworkDataExtractor dataExtractor;
    private RemotePlayerView remoteView;
    private VersusRenderer versusRenderer;

    // 호스트가 제한시간 모드의 남은 시간을 관리한다.
    private final VersusTimer timer;

    // 일시정지·재개·종료·로비 복귀를 상위 온라인 흐름에 알리는 콜백이다.
    protected final Runnable onPauseRequested;
    private final Consumer<Parent> onResumeRequested;
    private final Consumer<VersusResult> onMatchEnded;

    // 대기실 복귀를 상위 온라인 흐름에 요청한다.
    private final Runnable onReturnToLobby;

    // 서버 여부와 현재 경기의 상태 및 결과를 보관한다.
    private boolean isHost;
    private MatchState state;
    private boolean startRequested;// 경기 시작을 요청한 뒤 상대의 첫 DATA를 기다리는 동안 true다.

    private MatchEndReason pendingEndReason;
    // 내가 END를 보내고 응답을 기다리는 동안 종료 사유를 보관한다.
    // 상대 요청을 따르거나, 전송에 실패하거나, 결과가 확정되면 null로 되돌린다.

    private int localFinalScore;
    private long lastReceivedAttackId = -1;

    // 프레임 갱신과 상태 전송 주기를 관리한다.
    private AnimationTimer updateLoop;
    private long previousFrameTime;
    private GameMode gameMode;

    // 호스트의 게임과 화면을 먼저 구성한 뒤 로비 서버의 연결에 수신 함수를 연결한다.
    public OnlineVersusManager(OnlineLobbyServer server, GameSettings settings,
            Runnable onPauseRequested, Consumer<Parent> onResumeRequested,
            Consumer<VersusResult> onMatchEnded, Runnable onReturnToLobby) {
        this(settings, true, onPauseRequested, onResumeRequested, onMatchEnded, onReturnToLobby);
        Objects.requireNonNull(server, "server cannot be null");
        networkController = new NetworkController(server, this::HandleGameData,
                () -> Platform.runLater(this::HandleConnectionLost));
    }

    // 클라이언트의 게임과 화면을 먼저 구성한 뒤 로비 클라이언트의 연결에 수신 함수를 연결한다.
    public OnlineVersusManager(OnlineLobbyClient client, GameSettings settings,
            Runnable onPauseRequested, Consumer<Parent> onResumeRequested,
            Consumer<VersusResult> onMatchEnded, Runnable onReturnToLobby) {
        this(settings, false, onPauseRequested, onResumeRequested, onMatchEnded, onReturnToLobby);
        Objects.requireNonNull(client, "client cannot be null");
        networkController = new NetworkController(client, this::HandleGameData,
                () -> Platform.runLater(this::HandleConnectionLost));
    }

    // 양쪽 생성자가 공유하는 경기 설정과 화면 및 아직 시작하지 않은 갱신 루프를 준비한다.
    private OnlineVersusManager(GameSettings settings, boolean isHost, Runnable onPauseRequested,
            Consumer<Parent> onResumeRequested, Consumer<VersusResult> onMatchEnded,
            Runnable onReturnToLobby) {
        Objects.requireNonNull(settings, "settings cannot be null");
        this.isHost = isHost;
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested, "onPauseRequested cannot be null");
        this.onResumeRequested = Objects.requireNonNull(onResumeRequested, "onResumeRequested cannot be null");
        this.onMatchEnded = Objects.requireNonNull(onMatchEnded, "onMatchEnded cannot be null");
        this.onReturnToLobby = Objects.requireNonNull(onReturnToLobby, "onReturnToLobby cannot be null");
        gameMode = settings.gameMode();
        state = MatchState.READY;
        timer = isHost ? new VersusTimer(this::HandleTimeExpired) : null;

        localController = new GameController(this::RequestPause,
                gameResult -> HandleLocalEnd(MatchEndReason.BOARD_FULL, gameResult.score()), settings, PlayMode.ONLINE);
        dataExtractor = new NetworkDataExtractor(localController);
        int queueSize = localController.GetRenderState().blockQueue().length;
        SceneRenderState initialRemoteState = new SceneRenderState(
                new GameBoard(settings.colorMode()).GetBoard(), null, 0, new BlockData[0], null);
        remoteView = new RemotePlayerView(new RenderConfig(settings, queueSize), initialRemoteState);
        versusRenderer = new VersusRenderer(localController.GetRoot(), remoteView.GetRoot(), settings);
        versusRenderer.CreateConnectionStatusPane();
        versusRenderer.GetRoot().setOnKeyPressed(
                event -> localController.playerInput.HandleKeyCode(event.getCode()));
        updateLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                UpdateFrame(now);
            }
        };
    }

    // 호출자는 대전 화면을 표시한 뒤 시작을 요청한다. 첫 DATA는 클라이언트가 보낸다.
    public void GameStart() {
        if (state != MatchState.READY || startRequested) {
            return;
        }

        startRequested = true;
        if (isHost) {
            return;
        }



        if (!SendGameData(MessageType.DATA)) {
            startRequested = false;
        }
    }

    private void HandleInitialData() {
        startRequested = false;
        if (isHost) {
            if (!SendGameData(MessageType.DATA)) {
                return;
            }
        }

        previousFrameTime = 0;
        state = MatchState.RUNNING;
        localController.GameStart();
        updateLoop.start();
    }


    // 진행 중인 경기의 상대 화면과 연결 상태를 갱신하고 매 프레임 자기 상태를 전송한다.
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
        long latencyMs = networkController.getPingTime();
        versusRenderer.RenderConnectionStatus(latencyMs, latencyMs >= DelayThreshold);
        SendGameData(MessageType.DATA);
    }

    // 공통 경기 정보와 메시지별 데이터를 채우고, 제한시간 모드의 호스트는 남은 시간도 보낸다.
    protected boolean SendGameData(MessageType messageType) {
        Objects.requireNonNull(messageType, "messageType cannot be null");

        NetworkData data = new NetworkData();
        data.SetMessageType(messageType);
        data.SetIsEnded(state == MatchState.ENDING || state == MatchState.ENDED);
        data.SetIsHost(isHost);
        data.SetIsPaused(state == MatchState.PAUSED);

        switch (messageType) {
            case DATA -> dataExtractor.ExtractInto(data);
            case PAUSE -> {
                // 일시정지와 재개는 공통 필드의 isPaused로 전달한다.
                // 그래서 여기는 암것도 안함.
            }
            case END -> {
                data.SetFinalScore(localFinalScore);
                data.SetEndReason(pendingEndReason);
            }
            case FINAL_SCORE -> data.SetFinalScore(localFinalScore);
        }

        if (timer != null && gameMode == GameMode.TIMELIMIT) {
            data.SetRemainingTimeMs(timer.GetRemainingTimeMs());
        }

        try {
            networkController.send(data);
            return networkController.isConnected();
        } catch (IOException exception) {
            HandleConnectionLost();
            return false;
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
                if (state == MatchState.ENDING || state == MatchState.ENDED) {
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
                if (state == MatchState.READY && startRequested) {
                    HandleInitialData();
                }
            }
            case PAUSE -> {
                if (data.GetIsPaused()) {
                    ApplyPause();
                } else {
                    ApplyResume();
                }
            }
            case END -> HandleRemoteEnd(data.GetEndReason(), data.GetFinalScore());
            case FINAL_SCORE -> HandleFinalScore(data.GetFinalScore());
        }
    }

    // 자기 게임을 정지하고 종료 사유와 최종 점수를 상대에게 알린다.
    protected void HandleLocalEnd(MatchEndReason reason, int finalScore) {
        Objects.requireNonNull(reason, "reason cannot be null");
        if (state != MatchState.RUNNING) {
            return;
        }

        StopMatch(finalScore);
        pendingEndReason = reason;
        if (!SendGameData(MessageType.END)) {
            pendingEndReason = null;
        }
    }

    // 상대의 종료 요청을 적용하고 남은 점수를 교환한다.
    public void HandleRemoteEnd(MatchEndReason reason, long finalScore) {
        Objects.requireNonNull(reason, "reason cannot be null");

        if (state == MatchState.ENDING) {
            // 내가 상대에게 종료 메시지를 보냈는데, 상대에게 종료 메시지를 받은 상황
            if (pendingEndReason == MatchEndReason.TIMEOUT) {//내 종료 조건이 보드 풀일때만 아래 조건을 확인한다. 타임 아웃이라면 그냥 상대의 종료 요청을 받을 필요는 없을듯
                return;
            }
            if (reason == MatchEndReason.BOARD_FULL) { // 상대도 나도 보드가 차서 게임 끝난 경우 -> 비김
                ApplyMatchResult(finalScore, LocalWinLose.DRAW);
                return;
            }
            // 서버의 시간 만료를 따르되, 내 보드가 막힐 때 고정한 점수를 그대로 쓴다.
        } else if (state == MatchState.RUNNING || state == MatchState.PAUSED) { //내가 아직 진행 중인데 상대 쪽에서 종료 메시지 보냄 -> 매치 중단하고 정리하고 아래에서 상대에게 최종 점수 보내줌
            StopMatch(localController.GetScore());
        } else {
            return;
        }

        pendingEndReason = null;
        if (SendGameData(MessageType.FINAL_SCORE)) { // 상대의 종료 요청에 점수 제공
            ApplyMatchResult(finalScore, reason == MatchEndReason.TIMEOUT
                    ? CompareFinalScores(finalScore) : LocalWinLose.WIN); // 상대가 보낸 종료 메시지가 타임아웃이면 점수 비교, 타임아웃이 아니면 보드 풀로 상대가 졌다는 것이므로 내가 이겼다고 기록
        }
    }

    // 상대가 보낸 최종 점수를 정리하여 끝을 낸다.
    private void HandleFinalScore(long finalScore) {
        if (state == MatchState.ENDING && pendingEndReason != null) {
            ApplyMatchResult(finalScore, pendingEndReason == MatchEndReason.TIMEOUT
                    ? CompareFinalScores(finalScore) : LocalWinLose.LOSE); // 상대가 보낸 최종 점수 메시지에서 내가 처음에 종료 메시지를 보낸게 보드 풀이라면 내 패배이므로 그렇게 기록
        }
    }

    private LocalWinLose CompareFinalScores(long remoteFinalScore) {
        if (localFinalScore == remoteFinalScore) {
            return LocalWinLose.DRAW;
        }
        return localFinalScore > remoteFinalScore ? LocalWinLose.WIN : LocalWinLose.LOSE;
    }
    // 최종 점수를 주고 받는 동안 매치 진행을 멈추고 정리한다.
    private void StopMatch(int finalScore) {
        state = MatchState.ENDING;
        localFinalScore = finalScore;
        updateLoop.stop();
        localController.CleanGame();
        GetRoot().setOnKeyPressed(null);
    }

    private void HandleTimeExpired() { // 버서스 타이머가 시간 만료되면 이 함수 호출
        HandleLocalEnd(MatchEndReason.TIMEOUT, localController.GetScore());
    }

    // 양쪽 점수를 호스트·클라이언트 순서로 전달하고, 승패는 이 화면의 플레이어를 기준으로 표시한다.
    private void ApplyMatchResult(long remoteFinalScore, LocalWinLose outcome) {
        Objects.requireNonNull(outcome, "outcome cannot be null");
        int remoteScore = Math.toIntExact(remoteFinalScore);
        int hostScore = isHost ? localFinalScore : remoteScore;
        int clientScore = isHost ? remoteScore : localFinalScore;
        state = MatchState.ENDED;
        pendingEndReason = null;
        onMatchEnded.accept(new VersusResult(hostScore, clientScore, outcome));
    }

    // 연결이 끊기면 경기를 정리하고 대기실 복귀를 요청한다.
    public void HandleConnectionLost() {
        if (state == MatchState.ENDED) {
            return;
        }

        if (state != MatchState.ENDING) {
            StopMatch(localController.GetScore());
        }
        state = MatchState.ENDED;
        startRequested = false;
        pendingEndReason = null;
        onReturnToLobby.run();
    }


    // 내 일시정지를 적용한 뒤 상대에게 알린다.
    public void RequestPause() {
        if (ApplyPause()) {
            SendGameData(MessageType.PAUSE);
        }
    }

    // 내 재개를 적용한 뒤 상대에게 알린다.
    public void RequestResume() {
        if (ApplyResume()) {
            SendGameData(MessageType.PAUSE);
        }
    }

    // 내가 요청했거나 상대에게 받은 일시정지를 적용한다.
    private boolean ApplyPause() {
        if (state != MatchState.RUNNING) {
            return false;
        }

        // PauseGame의 콜백이 RequestPause를 다시 호출하기 전에 상태를 바꾼다.
        state = MatchState.PAUSED;
        updateLoop.stop();
        localController.PauseGame(); // 퍼즈 게임을 호출하면 해당 함수에서 다시 콜백으로 requestPause를 호출하나, 그때는 이 함수 상단에 state 조건으로 걸리므로 다시 실행되지 않는다.
        onPauseRequested.run();
        return true;
    }

    private boolean ApplyResume() {
        if (state != MatchState.PAUSED) {
            return false;
        }

        state = MatchState.RUNNING;
        onResumeRequested.accept(GetRoot());
        previousFrameTime = 0;
        localController.ResumeGame();
        updateLoop.start();
        return true;
    }

    

    

    // 자기 화면과 상대 화면 및 공통 UI를 합친 대전 화면 루트를 반환한다.
    public Parent GetRoot() {
        return versusRenderer.GetRoot();
    }

    public MatchState GetState() {
        return state;
    }
}
