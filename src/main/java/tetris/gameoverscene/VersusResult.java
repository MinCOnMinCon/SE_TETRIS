package tetris.gameoverscene;

public record VersusResult(
    String hostName,
    int hostScore,
    String clientName,
    int clientScore,
    boolean isHostWin) {
}