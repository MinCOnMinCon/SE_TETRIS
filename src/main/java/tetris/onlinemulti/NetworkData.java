package tetris.onlinemulti;

import java.io.Serializable;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock;
import tetris.gamescene.board.BoardElement;

public class NetworkData implements Serializable {
    // 클래스 버전 관리 (권장). 통신하는 양쪽의 클래스 형태가 같다는 것을 보증합니다.
    private static final long serialVersionUID = 1L;
    private BoardElement[][] board;
    private BlockData blockData;
    private BlockData[] nextBlocks;
    private BlockData blockHolding;
    private BoardElement[][] deletedBoard; // 2줄 이상 삭제 시, 삭제된 보드의 줄들을 저장
    private int score;
    private boolean isPaused;
    private boolean isEnded;
    private int hostIP;
    private int clientIP;
    private boolean isHost;

    // 관리자가 매 프레임 빈 데이터를 만든 뒤 담당 객체들이 필요한 필드를 채운다.
    public NetworkData() {
    }

    public NetworkData(CurrentBlock currentBlock, int score, int hostIP, int clientIP, boolean isHost) {
        this.board = currentBlock.GetBoard();
        this.blockData = currentBlock.GetCurrentBlock();
        this.nextBlocks = currentBlock.GetBlockQueue().GetBlocksInQueueOrder();
        this.blockHolding = currentBlock.GetBlockHolding();
        this.deletedBoard = currentBlock.GetDelBoard();
        this.score = score;
        this.hostIP = hostIP;
        this.clientIP = clientIP;
        this.isHost = isHost;
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

    // 이번에 소비한 새 공격 줄을 설정하며 공격이 없으면 null로 설정한다.
    public void SetDeletedBoard(BoardElement[][] deletedBoard) {
        this.deletedBoard = deletedBoard;
    }

    // 자기 게임의 현재 점수를 설정한다.
    public void SetScore(int score) {
        this.score = score;
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
    public BoardElement[][] GetVersusBoard() {
        return deletedBoard;
    }
    public int GetVersusScore() {
        return score;
    }
    public int GetHostIP() {
        return hostIP;
    }
    public int GetClientIP() {
        return clientIP;
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
