package tetris.versusmode;

public record VersusResult(
    String hostName,
    int hostScore,
    String clientName,
    int clientScore,
    // 0: Local, 1: Win, 2: Lose
    int LocalWinLose) {
        public VersusResult(int hostScore, int clientScore, int LocalWinLose){
            this("호스트", hostScore, "클라이언트", clientScore, LocalWinLose);
        }
        public VersusResult(int hostScore, int clientScore){
            this("플레이어1", hostScore, "플레이어2", clientScore, 0);
        }
}