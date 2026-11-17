package tetris.onlinemulti;


import tetris.versusmode.VersusTimer;

// 서버 관리자만 보유하며 경기 전체에 대한 결정을 맡는 기능의 선언이다.
public abstract class IngameServerFunc {
    private OnlineVersusManager manager;
    private VersusTimer timer;
    private long matchId;

    // 양쪽 최종 점수와 제출 여부 및 패배 사실을 결과 확정 전까지 보관한다.
    private int hostScore;
    private int clientScore;
    private boolean hostScoreReceived;
    private boolean clientScoreReceived;
    private boolean hostDefeated;
    private boolean clientDefeated;

    // 서버의 공통 관리자와 네트워크 참조 및 경기 설정을 받는다.
    public IngameServerFunc(OnlineVersusManager manager) {
    }

    // 새 경기 번호를 생성하고 경기별 기록과 타이머를 준비한 뒤 양쪽 공통 관리자에 시작을 통보한다.
    public abstract void StartMatch();

    // 공통 관리자의 정지 상태를 반영해 타이머를 갱신하고 남은 시간을 양쪽에 전달한다.
    public abstract void Update(double deltaTimeMs, boolean paused);

    // 현재 경기에서 패배한 참가자와 최종 점수를 기록하고 양쪽 최종 점수 수집으로 연결한다.
    public abstract void HandleDefeat(long matchId, boolean isHost, int finalScore);

    // 현재 경기의 제출자별 최종 점수를 기록하고 양쪽 점수가 모이면 결과를 확정한다.
    public abstract void HandleFinalScore(long matchId, boolean isHost, int score);

    // 제한시간 만료 시 양쪽 공통 관리자에 게임 정지와 최종 점수 보고를 요청한다.
    protected abstract void HandleTimeExpired();

    // 패배 사실과 최종 점수로 승패를 확정해 서버의 공통 관리자와 클라이언트에 전달한다.
    protected abstract void FinalizeMatch();
}
