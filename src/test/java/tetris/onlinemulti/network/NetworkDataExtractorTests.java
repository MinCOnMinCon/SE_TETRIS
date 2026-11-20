package tetris.onlinemulti.network;

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
                assertTrue(data.GetVersusBoards().isEmpty());
            } finally {
                controller.CleanGame();
            }
        });
    }

    // 입력: 새 공격, 공격 없는 상태, 다음 새 공격에 대한 연속 추출 호출.
    // 출력: 공격이 있을 때만 증가하는 ID와 공격 복사본. 목적: 공격을 한 번만 소비하고 빈 추출에서 재사용하지 않는지 검증한다.
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
                long firstAttackId = data.GetAttackId();
                assertTrue(firstAttackId > 0);
                attack[0][0].setElementColorType(ColorType.BlockZ);
                assertEquals(ColorType.BlockJ, data.GetVersusBoard()[0][0].getElementColorType());

                extractor.ExtractInto(data);
                assertEquals(2, controller.attackReads);
                assertNull(data.GetVersusBoard());
                assertEquals(firstAttackId, data.GetAttackId());

                controller.attack = attack;
                extractor.ExtractInto(data);
                assertEquals(3, controller.attackReads);
                assertEquals(firstAttackId + 1, data.GetAttackId());
                assertNotNull(data.GetVersusBoard());
            } finally {
                controller.CleanGame();
            }
        });
    }

    // 입력: 표시용 공격 대기열 한 건과 별도로 발생한 새 공격 한 건.
    // 출력: 서로 분리된 복사본. 목적: 원본 셀 변경과 대기열 소비가 전송 데이터에 영향을 주지 않는지 검증한다.
    @Test
    void copiesDisplayedAttackQueueSeparatelyFromNewAttack() throws Exception {
        runOnFxThread(() -> {
            AttackController controller = new AttackController();
            try {
                BoardElement[][] pending = {{new BoardElement(ColorType.BlockJ, true, 0)}};
                BoardElement[][] outgoing = {{new BoardElement(ColorType.BlockZ, true, 0)}};
                controller.AddVersusBoards(pending);
                controller.attack = outgoing;
                NetworkData data = new NetworkData();
                new NetworkDataExtractor(controller).ExtractInto(data);

                pending[0][0].setElementColorType(ColorType.BlockL);
                outgoing[0][0].setElementColorType(ColorType.BlockL);
                controller.GetRenderState().versusBoards().clear();

                assertEquals(1, data.GetVersusBoards().size());
                assertEquals(ColorType.BlockJ, data.GetVersusBoards().get(0)[0][0].getElementColorType());
                assertEquals(ColorType.BlockZ, data.GetVersusBoard()[0][0].getElementColorType());
                assertEquals(1, controller.attackReads);
            } finally {
                controller.CleanGame();
            }
        });
    }

    // 입력: ID 7의 실행 중 이펙트와 삭제 행 배열, 이후 같은 ID의 종료 상태.
    // 출력: 복사된 행 배열과 실행 여부, 유지된 ID. 목적: 같은 추출 시점의 정보와 이펙트 종료 후 ID 유지를 검증한다.
    @Test
    void copiesEffectSnapshotAndKeepsIdAfterEffectEnds() throws Exception {
        runOnFxThread(() -> {
            AttackController controller = new AttackController();
            try {
                controller.effectRunning = true;
                controller.effectId = 7;
                controller.clearedRows = new int[] {18, 19};
                NetworkData data = new NetworkData();
                data.SetRemainingTimeMs(5000);
                NetworkDataExtractor extractor = new NetworkDataExtractor(controller);
                extractor.ExtractInto(data);

                controller.clearedRows[0] = -1;
                assertTrue(data.GetEffectRunning());
                assertEquals(7, data.GetEffectId());
                assertArrayEquals(new int[] {18, 19}, data.GetClearedRows());
                assertEquals(5000, data.GetRemainingTimeMs());

                controller.effectRunning = false;
                extractor.ExtractInto(data);
                assertFalse(data.GetEffectRunning());
                assertEquals(7, data.GetEffectId());
                assertArrayEquals(new int[] {-1, 19}, data.GetClearedRows());
            } finally {
                controller.CleanGame();
            }
        });
    }

    private static final class AttackController extends GameController {
        private BoardElement[][] attack;
        private int attackReads;
        private boolean effectRunning;
        private long effectId;
        private int[] clearedRows = new int[0];

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

        @Override
        public boolean GetLineClearEffectRunning() {
            return effectRunning;
        }

        @Override
        public long GetEffectId() {
            return effectId;
        }

        @Override
        public int[] GetClearedRows() {
            return clearedRows;
        }
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
