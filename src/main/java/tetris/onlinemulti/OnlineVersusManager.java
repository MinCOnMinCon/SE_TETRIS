package tetris.onlinemulti;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import tetris.gamescene.GameController;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;
import tetris.versusmode.VersusRenderer;

public abstract class OnlineVersusManager implements AutoCloseable {
    public enum MatchState { READY, RUNNING, PAUSED, ENDED }
    public enum MatchOutcome { HOST_WIN, CLIENT_WIN, DRAW }

    public record MatchResult(long matchId, MatchOutcome outcome, int hostScore, int clientScore) {}

    // 경기 실행과 화면 구성에 필요한 객체를 보관한다.
    private NetworkController networkManager;
    private GameController localController;
    private NetworkDataExtractor dataExtractor;
    private RemotePlayerView remoteView;
    private VersusRenderer versusRenderer;
    private GameSettings settings;

    // 서버에서만 보유하며 경기 시작과 시간 및 결과 결정을 위임한다.
    private IngameServerFunc serverFunc;

    // 경기 종료 후 방 복귀와 연결 이상을 상위 온라인 흐름에 전달한다.
    private Runnable onReturnToLobby;
    private Consumer<String> onConnectionLost;

    // 서버 여부와 현재 경기의 식별 정보 및 결과를 보관한다.
    private boolean isHost;
    private long matchId;
    private MatchState state;
    private MatchResult result;

    // 프레임 갱신과 상태 전송 주기를 관리한다.
    private AnimationTimer updateLoop;
    private long previousFrameTime;
    private long lastSendTime;
    private long sendIntervalNanos;
    private GameMode gameMode;

    // 네트워크 참조와 경기 설정 및 콜백을 받아 자기 게임과 화면을 구성한다.
    public OnlineVersusManager(NetworkController networkManager, GameSettings settings,
            boolean isHost, Runnable onReturnToLobby,
            Consumer<String> onConnectionLost) {
    }

    // 서버가 확정한 경기 번호를 받아 자기 게임과 관리자 갱신 루프를 시작한다.
    public abstract void GameStart(long matchId);

    // 상대 화면을 갱신하고 전송 시점을 확인하며 서버에서는 서버 기능의 갱신도 호출한다.
    protected abstract void UpdateFrame(long now);

    // 공통 경기 정보와 메시지별 데이터를 채우고, 제한시간 모드의 호스트는 남은 시간도 보낸다.
    protected void SendGameData(MessageType messageType) {
        Objects.requireNonNull(messageType, "messageType cannot be null");

        NetworkData data = new NetworkData();
        data.SetMessageType(messageType);
        data.SetMatchId(matchId);
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

        if (gameMode == GameMode.TIMELIMIT && isHost) {
            data.SetRemainingTimeMs(serverFunc.GetRemainingTimeMs());
        }

        try {
            networkManager.send(data);
        } catch (IOException exception) {
            HandleConnectionLost("게임 데이터 전송에 실패했습니다.");
        }
    }

    // 경기 번호를 확인하고 수신 상태는 상대 화면에, 미처리 공격은 자기 게임에 적용한다.
    public abstract void HandleGameData(long matchId, NetworkData data);

    // 서버 기능이 전달하거나 서버 메시지로 수신한 현재 경기의 남은 시간을 표시한다.
    public abstract void HandleRemainingTime(long matchId, double remainingTimeMs);

    // 자기 게임을 정지하고 패배 사실을 상대에게 알린 뒤 서버 기능에 패배와 최종 점수를 보고한다.
    protected abstract void HandleLocalDefeat(int finalScore);

    // 상대 패배 통보로 자기 게임을 정지하고 자기 최종 점수를 보고하며 서버에서는 상대 패배도 전달한다.
    public abstract void HandleRemoteDefeat(long matchId, int finalScore);

    // 서버에서는 서버 기능에 직접 전달하고 클라이언트에서는 메시지로 자기 최종 점수를 보고한다.
    protected abstract void ReportFinalScore(int finalScore);

    // 서버 기능의 종료 요청이나 서버 메시지를 받아 자기 게임을 정지하고 최종 점수를 보고한다.
    public abstract void HandleFinalScoreRequest(long matchId);

    // 자신의 일시정지 또는 재개 요청을 상대에게 전달하고 공통 정지 상태 처리로 연결한다.
    public abstract void RequestPause(boolean paused);

    // 자신의 요청이나 상대의 메시지에 따른 정지 상태를 게임과 입력 및 이펙트에 적용한다.
    public abstract void HandlePauseState(long matchId, boolean paused);

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
    public abstract Parent GetRoot();
}
