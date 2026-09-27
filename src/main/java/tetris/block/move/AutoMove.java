package tetris.block.move;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tetris.block.data.BlockData;

/*
    AutoMove 클래스는 테트리스 블럭의 자동 이동 기능을 제공하는 클래스
    AutoMove(BlockData, int[][]) 생성자: 현재 블럭과 보드 상태를 설정
        currentBlock, currentBoard의 상태를 변경할 수 있는 getter/setter 제공
    StartAutoMove(int, BlockData, int[][]) 블럭을 자동으로 아래로 이동
    StopAutoMoveDown() 블럭의 자동 이동을 중지
*/

public class AutoMove {
    private Timer autoDropTimer;
    private BlockData currentBlock;
    private int[][] currentBoard;

    public AutoMove(BlockData blockData, int[][] board) {
        this.currentBlock = blockData;
        this.currentBoard = board;
    }
    public void SetCurrentBlock(BlockData blockData) {
        this.currentBlock = blockData;
    }
    public BlockData GetCurrentBlock() {
        return this.currentBlock;
    }
    public void SetCurrentBoard(int[][] board) {
        this.currentBoard = board;
    }
    public int[][] GetCurrentBoard() {
        return this.currentBoard;
    }

    public void StartAutoMove(int intervalMillis, BlockData blockData, int[][] board) {
        if (this.autoDropTimer != null) {
            this.autoDropTimer.stop();
        }

        this.autoDropTimer = new Timer(intervalMillis, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentBlock != null) {
                    BlockMove.MoveDown(currentBlock, currentBoard);
                }
            }
        });
        
        this.autoDropTimer.start();
    }

    public void StopAutoMoveDown() {
        if (this.autoDropTimer != null) {
            this.autoDropTimer.stop();
        }
    }
}
