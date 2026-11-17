package tetris.onlinemulti;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import tetris.block.data.ColorType;
import tetris.gamescene.GameController;
import tetris.gamescene.board.BoardElement;
import tetris.settings.GameSettings;

class NetworkDataExtractorTests {
    @BeforeAll
    static void initializeJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    // 입력: 실제 컨트롤러의 보드·블록·NEXT와 빈 HOLD. 출력: 독립된 복사본과 현재 점수.
    // 목적: 추출 후 원본 셀·위치·모양·아이템 위치가 변해도 전송 데이터가 유지되는지 검증한다.
    @Test
    void copiesMutableScreenData() throws Exception {
        runOnFxThread(() -> {
            AttackController controller = new AttackController();
            try {
                var state = controller.GetRenderState();
                state.currentBlock().SetItemPos(new int[] {1, 2});
                int originalX = state.currentBlock().GetX();
                boolean originalCell = state.currentBlock().GetShape()[0][0];
                int originalNextX = state.blockQueue()[0].GetX();
                NetworkData data = new NetworkData();
                new NetworkDataExtractor(controller).ExtractInto(data);

                state.board()[0][0].setElementColorType(ColorType.BlockJ);
                state.board()[0][0].setBlock(true);
                state.currentBlock().SetX(99);
                state.currentBlock().GetShape()[0][0] = !originalCell;
                state.currentBlock().GetItemPos()[0] = 9;
                state.blockQueue()[0].SetX(88);

                assertEquals(ColorType.NONE, data.GetBoard()[0][0].getElementColorType());
                assertFalse(data.GetBoard()[0][0].isBlock());
                assertEquals(originalX, data.GetBlock().GetX());
                assertEquals(originalCell, data.GetBlock().GetShape()[0][0]);
                assertArrayEquals(new int[] {1, 2}, data.GetBlock().GetItemPos());
                assertEquals(originalNextX, data.GetNextBlocks()[0].GetX());
                assertNull(data.GetBlockHolding());
                assertEquals(controller.GetScore(), data.GetVersusScore());
            } finally {
                controller.CleanGame();
            }
        });
    }

    // 입력: 한 번만 반환되는 공격 줄과 같은 NetworkData에 대한 두 번의 추출 호출.
    // 출력: 첫 호출은 공격 복사본, 두 번째는 null. 목적: 공격을 호출당 한 번 소비하고 이전 공격을 재사용하지 않는지 검증한다.
    @Test
    void consumesAttackOncePerExtractionAndClearsMissingAttack() throws Exception {
        runOnFxThread(() -> {
            AttackController controller = new AttackController();
            try {
                BoardElement[][] attack = {{new BoardElement(ColorType.BlockJ, true, 0)}};
                controller.attack = attack;
                NetworkData data = new NetworkData();
                NetworkDataExtractor extractor = new NetworkDataExtractor(controller);
                extractor.ExtractInto(data);
                assertEquals(1, controller.attackReads);
                assertNull(controller.attack);
                attack[0][0].setElementColorType(ColorType.BlockZ);
                assertEquals(ColorType.BlockJ, data.GetVersusBoard()[0][0].getElementColorType());

                extractor.ExtractInto(data);
                assertEquals(2, controller.attackReads);
                assertNull(data.GetVersusBoard());
            } finally {
                controller.CleanGame();
            }
        });
    }

    private static final class AttackController extends GameController {
        private BoardElement[][] attack;
        private int attackReads;

        AttackController() {
            super(() -> {}, new GameSettings("1280x720", 1280, 720, 0, Map.of(),
                    GameSettings.Difficulty.NORMAL, GameSettings.GameMode.NORMAL), 0);
        }

        @Override
        public BoardElement[][] GetDelBoard() {
            attackReads++;
            BoardElement[][] result = attack;
            attack = null;
            return result;
        }
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
