package tetris.block.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.block.data.CurrentBlock;
import tetris.block.data.PatternType;
import tetris.block.data.ItemType;
import tetris.gamescene.GameProgress;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import org.junit.jupiter.api.Test;

class BlockMoveTest {
    @Test
    void manualDropDefersNextBlockAndIncomingAttackUntilEffectFinishes() {
        assertDeferredLineClear(false, false);
    }

    @Test
    void automaticDropDefersNextBlockAndIncomingAttackUntilEffectFinishes() {
        assertDeferredLineClear(true, false);
    }

    @Test
    void itemDropDefersNextBlockAndIncomingAttackUntilEffectFinishes() {
        assertDeferredLineClear(false, true);
    }

    private void assertDeferredLineClear(boolean automatic, boolean item) {
        GameProgress progress = new GameProgress();
        GameBoard board = new GameBoard(0);
        for (int col = 1; col < GameBoard.boardCol; col++) {
            board.GetBoard()[19][col].setBlock(true);
        }
        BlockData data = new BlockData(new boolean[][] {{true}}, 0, 19);
        if (item) {
            data.SetItem(true);
            data.SetItemType(ItemType.DEL_HLINE);
            data.SetItemPos(new int[] {0, 0});
        }
        CurrentBlock current = new CurrentBlock(data,
                new BlockQueue(0, progress, Difficulty.NORMAL, GameMode.NORMAL),
                null, true, false, board, () -> {});
        GameBoard attack = new GameBoard(0);
        attack.GetBoard()[19][1].setBlock(true);
        current.AddVersusBoards(new BoardElement[][] {attack.GetBoard()[19]});
        AtomicInteger requests = new AtomicInteger();
        BlockController controller = new BlockController(current,
                new AutoMove(current, Difficulty.NORMAL), new GameScore(progress), progress,
                requests::incrementAndGet);

        if (automatic) controller.TimeUpdate(1000);
        else controller.MoveDown();

        assertEquals(1, requests.get());
        assertEquals(19, board.GetClearedRows()[0]);
        assertSame(data, current.GetCurrentBlock());
        assertEquals(1, current.GetVersusBoards().size());
        assertFalse(board.GetBoard()[19][1].isBlock());

        controller.FinishDeleteAfterEffect();

        BlockData next = current.GetCurrentBlock();
        assertNotSame(data, next);
        assertEquals(0, current.GetVersusBoards().size());
        assertEquals(true, board.GetBoard()[19][1].isBlock());
        assertNull(current.GetDelBoard());
        controller.FinishDeleteAfterEffect();
        assertSame(next, current.GetCurrentBlock());
    }

    @Test
    void movesCurrentBlockAndAwardsSoftDropScore() {
        GameBoard board = new GameBoard(0);
        BlockData data = new BlockData(new boolean[][] {{true}}, 4, 4);
        CurrentBlock current = new CurrentBlock(
                data, null, null, true, false, board, () -> {});
        GameProgress progress = new GameProgress();
        GameScore score = new GameScore(progress);
        BlockMove movement = new BlockMove(current, score, progress);

        movement.MoveRight();
        movement.MoveDown();

        assertEquals(5, data.GetX());
        assertEquals(5, data.GetY());
        assertEquals(10, score.GetGameScore());
    }

    @Test
    void clearsTwoRowsAndAdvancesCurrentBlock() {
        GameProgress progress = new GameProgress();
        GameBoard board = new GameBoard(0);
        for (int col = 0; col < GameBoard.boardCol; col++) {
            board.GetBoard()[18][col].setBlock(true);
            board.GetBoard()[18][col].setElementColorType(ColorType.BlockI);
            board.GetBoard()[18][col].setPatternType(PatternType.GRID);
            if (col > 0) {
                board.GetBoard()[19][col].setBlock(true);
                board.GetBoard()[19][col].setElementColorType(ColorType.BlockI);
                board.GetBoard()[19][col].setPatternType(PatternType.GRID);
            }
        }

        BlockData data = new BlockData(new boolean[][] {{true}}, 0, 19);
        CurrentBlock current = new CurrentBlock(
                data,
                new BlockQueue(0, progress, Difficulty.NORMAL, GameMode.NORMAL),
                null,
                true,
                false,
                board,
                () -> {});
        GameScore score = new GameScore(progress);
        BlockMove movement = new BlockMove(current, score, progress);

        movement.MoveDown();

        assertNotSame(data, current.GetCurrentBlock());
        BoardElement[][] deletedBoard = current.GetDelBoard();
        assertNotNull(deletedBoard);
        assertEquals(2, deletedBoard.length);
        for (int row = 0; row < deletedBoard.length; row++) {
            for (int col = 0; col < GameBoard.boardCol; col++) {
                if (deletedBoard[row][col].isBlock()) {
                    assertEquals(ColorType.GRAY, deletedBoard[row][col].getElementColorType());
                    assertEquals(PatternType.NONE, deletedBoard[row][col].getPatternType());
                }
            }
        }
        for (int row = 18; row < GameBoard.boardRow; row++) {
            for (int col = 0; col < GameBoard.boardCol; col++) {
                assertFalse(board.GetBoard()[row][col].isBlock());
            }
        }
    }
}
