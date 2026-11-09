package tetris.versusmode;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.util.Duration;
import tetris.gamescene.GameController;
import tetris.settings.GameSettings;

public class VersusManager {
    public enum Player { PLAYER1, PLAYER2 }
    public enum MatchState { READY, RUNNING, PAUSED, ENDED }
    public enum MatchResult { PLAYER1_WIN, PLAYER2_WIN, DRAW }

    private final GameController player1;
    private final GameController player2;
    private final VersusInput input;
    private final VersusRenderer renderer;
    private final Consumer<MatchResult> onMatchEnded;
    private long previousFrameTime;
    private MatchState state = MatchState.READY;
    private MatchResult result;

    private final AnimationTimer updateLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (state != MatchState.RUNNING) return;
            if (previousFrameTime == 0) {
                previousFrameTime = now;
                return;
            }
            double deltaTimeMs = (now - previousFrameTime) / 1_000_000.0;
            previousFrameTime = now;
            input.Update(deltaTimeMs);
            // TODO: 제한시간 관리자 갱신 연결.
        }
    };

    // TODO: 공격 정보 타입과 플레이어별 공격 대기열, 전달·소비 시점 정의.
    // TODO: 선택적인 제한시간 관리자 연결.

    public VersusManager(GameSettings settings, Consumer<MatchResult> onMatchEnded) {
        Objects.requireNonNull(settings);
        this.onMatchEnded = Objects.requireNonNull(onMatchEnded);
        player1 = new GameController(ignored -> HandlePlayerDefeat(Player.PLAYER1), settings);
        player2 = new GameController(ignored -> HandlePlayerDefeat(Player.PLAYER2), settings);
        input = new VersusInput(this::PauseGame, player1.playerInput, player2.playerInput,
                settings.playerKeyBindings());
        input.SetInputEnabled(false);
        renderer = new VersusRenderer(player1.GetRoot(), player2.GetRoot(), settings);
        Parent root = renderer.GetRoot();
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> input.HandleKeyPressed(event.getCode()));
        root.setOnKeyReleased(event -> input.HandleKeyReleased(event.getCode()));
    }

    public void GameStart() {
        if (state != MatchState.READY) return;
        state = MatchState.RUNNING;
        player1.GameStart();
        player2.GameStart();
        input.SetInputEnabled(true);
        previousFrameTime = 0;
        updateLoop.start();
        GetRoot().requestFocus();
        // TODO: 제한시간 모드의 타이머 시작.
    }

    public void PauseGame() {
        if (state != MatchState.RUNNING) return;
        state = MatchState.PAUSED;
        input.SetInputEnabled(false);
        updateLoop.stop();
        player1.PauseGame();
        player2.PauseGame();
        // TODO: 제한시간 정지, 일시정지 UI 연결.
    }

    public void ResumeGame() {
        if (state != MatchState.PAUSED) return;
        state = MatchState.RUNNING;
        player1.ResumeGame();
        player2.ResumeGame();
        input.SetInputEnabled(true);
        previousFrameTime = 0;
        updateLoop.start();
        GetRoot().requestFocus();
        // TODO: 제한시간 재개.
    }

    public void HandlePlayerDefeat(Player loser) { // 플레이어 한 명이 패배해서 이 함수를 호출
        Objects.requireNonNull(loser);
        if (state != MatchState.RUNNING) return;
        EndMatch(loser == Player.PLAYER1 ? MatchResult.PLAYER2_WIN : MatchResult.PLAYER1_WIN);
    }

    public void HandleTimeExpired() { // 제한 시간 모드일 때, 시간이 만료될 때 이 함수 호출
        if (state != MatchState.RUNNING) return;
        int comparison = Integer.compare(player1.GetScore(), player2.GetScore());
        EndMatch(comparison > 0 ? MatchResult.PLAYER1_WIN
                : comparison < 0 ? MatchResult.PLAYER2_WIN : MatchResult.DRAW);
    }

    private void EndMatch(MatchResult result) { // HandlePlayerDefeat, HandleTimeExpired 함수로 호출됨. 게임을 정리하는 역할
        if (state == MatchState.ENDED) return;
        state = MatchState.ENDED;
        this.result = result;
        input.SetInputEnabled(false);
        updateLoop.stop();
        GetRoot().setOnKeyPressed(null);
        GetRoot().setOnKeyReleased(null);
        player1.CleanGame();
        player2.CleanGame();
        renderer.RenderFinish();
        PauseTransition finishDelay = new PauseTransition(Duration.seconds(3));
        finishDelay.setOnFinished(event -> onMatchEnded.accept(this.result));
        finishDelay.play();
    }

    public Parent GetRoot() { return renderer.GetRoot(); }
    public MatchState GetState() { return state; }
    public MatchResult GetResult() { return result; }
}
