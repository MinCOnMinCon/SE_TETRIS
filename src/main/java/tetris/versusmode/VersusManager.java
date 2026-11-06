package tetris.versusmode;

import java.util.Objects;

import tetris.gamescene.GameController;
import tetris.settings.GameSettings;

public class VersusManager {
    public enum Player { PLAYER1, PLAYER2 }
    public enum MatchState { READY, RUNNING, PAUSED, ENDED }
    public enum MatchResult { PLAYER1_WIN, PLAYER2_WIN, DRAW }

    private final GameController player1;
    private final GameController player2;
    private MatchState state = MatchState.READY;
    private MatchResult result;

    // TODO: 공격 정보 타입과 플레이어별 공격 대기열, 전달·소비 시점 정의.
    // TODO: 공통 입력 처리자, 선택적인 제한시간 관리자, 대전 렌더러 연결.

    public VersusManager(GameSettings player1Settings, GameSettings player2Settings) {
        player1 = new GameController(ignored -> HandlePlayerDefeat(Player.PLAYER1), player1Settings);
        player2 = new GameController(ignored -> HandlePlayerDefeat(Player.PLAYER2), player2Settings);
        // TODO: 두 GetRoot()를 렌더러에 전달하고 공통 입력·패배·시간 종료 연결.
        
    }

    public void GameStart() {
        if (state != MatchState.READY) return;
        state = MatchState.RUNNING;
        player1.GameStart();
        player2.GameStart();
        // TODO: 공통 입력 활성화, 제한시간 모드의 타이머 시작.
    }

    public void PauseGame() {
        if (state != MatchState.RUNNING) return;
        state = MatchState.PAUSED;
        player1.PauseGame();
        player2.PauseGame();
        // TODO: 공통 입력 차단, 타이머 정지, 일시정지 UI 연결.
    }

    public void ResumeGame() {
        if (state != MatchState.PAUSED) return;
        state = MatchState.RUNNING;
        player1.ResumeGame();
        player2.ResumeGame();
        // TODO: 공통 입력 활성화, 타이머 재개, 포커스 복구.
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
        player1.CleanGame();
        player2.CleanGame();
        // TODO: 공통 입력 해제, 타이머 정지, 결과 표시와 화면 전환 연결.
    }

    // TODO: 렌더러 연결 후 GetRoot() 제공.
    public MatchState GetState() { return state; }
    public MatchResult GetResult() { return result; }
}
