package tetris.gameoverscene;

public record VersusResult(
    String hostName,
    int hostScore,
    String clientName,
    int clientScore,
    LocalWinLose localWinLose) {
        public enum LocalWinLose {
        LOCALWIN,
        LOCALLOSE,
        WIN,
        LOSE
    }
        public VersusResult(int hostScore, int clientScore, LocalWinLose localWinLose) {
            this((localWinLose == LocalWinLose.LOCALWIN || localWinLose == LocalWinLose.LOCALLOSE) ? 
                "플레이어1" : "호스트", hostScore,
                (localWinLose == LocalWinLose.LOCALWIN || localWinLose == LocalWinLose.LOCALLOSE) ? 
                "플레이어2" : "클라이언트", clientScore, 
                localWinLose);
        }
}