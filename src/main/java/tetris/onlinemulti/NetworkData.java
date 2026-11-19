package tetris.onlinemulti;

import java.io.Serializable;
import java.util.List;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;
import tetris.onlinemulti.OnlineVersusManager.MatchOutcome;

public class NetworkData implements Serializable {
    // 클래스 버전 관리 (권장). 통신하는 양쪽의 클래스 형태가 같다는 것을 보증합니다.
    private static final long serialVersionUID = 1L;
    private MessageType messageType;
    private BoardElement[][] board;
    private BlockData blockData;
    private BlockData[] nextBlocks;
    private BlockData blockHolding;
    private List<BoardElement[][]> versusBoards;
    private long attackId;
    private BoardElement[][] deletedBoard; // 2줄 이상 삭제 시, 삭제된 보드의 줄들을 저장
    private boolean effectRunning;
    private long effectId;
    private int[] clearedRows;
    private long score;
    private double remainingTimeMs;
    private boolean isPaused;
    private long finalScore;
    private MatchOutcome outcome;
    private long hostScore;
    private long clientScore;
    private boolean isEnded;
    private boolean isHost;
    // 관리자가 매 프레임 빈 데이터를 만든 뒤 담당 객체들이 필요한 필드를 채운다.
    public NetworkData() {
    }

    public NetworkData(MessageType msgT) {
        this.messageType = msgT;
    }

    public NetworkData(CurrentBlock currentBlock, long score, int hostIP, int clientIP, boolean isHost) {
        this.board = currentBlock.GetBoard();
        this.blockData = currentBlock.GetCurrentBlock();
        this.nextBlocks = currentBlock.GetBlockQueue().GetBlocksInQueueOrder();
        this.blockHolding = currentBlock.GetBlockHolding();
        this.deletedBoard = currentBlock.GetDelBoard();
        this.score = score;
        this.isHost = isHost;
    }

    public void SetMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    // 추출기가 복사한 고정 보드를 설정한다.
    public void SetBoard(BoardElement[][] board) {
        this.board = board;
    }

    // 추출기가 복사한 현재 블록을 설정한다.
    public void SetBlock(BlockData blockData) {
        this.blockData = blockData;
    }

    // 추출기가 복사한 NEXT 목록을 설정한다.
    public void SetNextBlocks(BlockData[] nextBlocks) {
        this.nextBlocks = nextBlocks;
    }

    // 추출기가 복사한 HOLD 블록을 설정한다.
    public void SetBlockHolding(BlockData blockHolding) {
        this.blockHolding = blockHolding;
    }

    // 상대 화면에 표시할 공격 대기열을 설정한다.
    public void SetVersusBoards(List<BoardElement[][]> versusBoards) {
        this.versusBoards = versusBoards;
    }

    public void SetAttackId(long attackId) {
        this.attackId = attackId;
    }

    // 이번에 소비한 새 공격 줄을 설정하며 공격이 없으면 null로 설정한다.
    public void SetDeletedBoard(BoardElement[][] deletedBoard) {
        this.deletedBoard = deletedBoard;
    }

    // 자기 게임의 현재 점수를 설정한다.
    public void SetScore(long score) {
        this.score = score;
    }

    public void SetEffectRunning(boolean effectRunning) {
        this.effectRunning = effectRunning;
    }

    public void SetEffectId(long effectId) {
        this.effectId = effectId;
    }

    public void SetClearedRows(int[] clearedRows) {
        this.clearedRows = clearedRows;
    }

    public void SetRemainingTimeMs(double remainingTimeMs) {
        this.remainingTimeMs = remainingTimeMs;
    }

    public void SetIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void SetFinalScore(long finalScore) {
        this.finalScore = finalScore;
    }

    public void SetOutcome(MatchOutcome outcome) {
        this.outcome = outcome;
    }

    public void SetHostScore(long hostScore) {
        this.hostScore = hostScore;
    }

    public void SetClientScore(long clientScore) {
        this.clientScore = clientScore;
    }

    public void SetIsEnded(boolean isEnded) {
        this.isEnded = isEnded;
    }

    public void SetIsHost(boolean isHost) {
        this.isHost = isHost;
    }



    public MessageType GetMessageType() {
        return messageType;
    }
    public BoardElement[][] GetBoard() {
        return board;
    }
    public BlockData GetBlock() {
        return blockData;
    }
    public BlockData[] GetNextBlocks() {
        return nextBlocks;
    }
    public BlockData GetBlockHolding() {
        return blockHolding;
    }
    public List<BoardElement[][]> GetVersusBoards() {
        return versusBoards;
    }
    public long GetAttackId() {
        return attackId;
    }
    public BoardElement[][] GetVersusBoard() {
        return deletedBoard;
    }
    public long GetVersusScore() {
        return score;
    }
    public boolean GetEffectRunning() {
        return effectRunning;
    }
    public long GetEffectId() {
        return effectId;
    }
    public int[] GetClearedRows() {
        return clearedRows;
    }
    public double GetRemainingTimeMs() {
        return remainingTimeMs;
    }
    public long GetFinalScore() {
        return finalScore;
    }
    public MatchOutcome GetOutcome() {
        return outcome;
    }
    public long GetHostScore() {
        return hostScore;
    }
    public long GetClientScore() {
        return clientScore;
    }
    public boolean GetIsHost() {
        return isHost;
    }
    public boolean GetIsPaused() {
        return isPaused;
    }
    public boolean GetIsEnded() {
        return isEnded;
    }
}
