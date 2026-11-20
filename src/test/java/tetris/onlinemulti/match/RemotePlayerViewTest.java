package tetris.onlinemulti.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import tetris.block.data.BlockData;
import tetris.block.data.ColorType;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.settings.GameSettings;

class RemotePlayerViewTest {
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

    // 이펙트 중 수신 상태를 버리고 종료 후 새로 받은 상태만 표시하며 실제 보드는 수정하지 않는다.
    @Test
    void freezesEffectBoardAndWaitsForAnotherSnapshot() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.view.ApplyState(fixture.before, 1, true, new int[] {19, -1, -1, -1});
            fixture.view.Update(0);
            assertEquals(Color.WHITE, fixture.colorAt(19));

            fixture.view.ApplyState(fixture.after, 1, false, null);
            fixture.view.Update(1000);
            fixture.view.Update(16);
            assertEquals(Color.BLUE, fixture.colorAt(0), "이펙트 중 받은 새 보드를 보관하지 않는다");
            assertEquals(Color.BLACK, fixture.colorAt(19), "다음 수신 전까지 이펙트의 마지막 화면을 유지한다");
            assertTrue(fixture.before.board()[19][0].isBlock(), "표시 객체는 실제 줄을 삭제하지 않는다");

            fixture.view.ApplyState(fixture.after, 1, false, null);
            fixture.view.Update(0);
            assertEquals(Color.RED, fixture.colorAt(0));
            assertEquals(Color.BLACK, fixture.colorAt(19));
        });
    }

    // 끝난 이펙트가 지연 도착해도 재시작하거나 이전 보드로 되돌아가지 않는다.
    @Test
    void rejectsCompletedEffectButAcceptsANewEffect() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.view.ApplyState(fixture.before, 1, true, new int[] {19});
            fixture.view.Update(1000);
            fixture.view.ApplyState(fixture.after, 1, false, null);
            fixture.view.Update(0);

            fixture.view.ApplyState(fixture.before, 1, true, new int[] {19});
            fixture.view.Update(0);
            assertEquals(Color.RED, fixture.colorAt(0));
            assertEquals(Color.BLACK, fixture.colorAt(19));

            int[] rows = {18};
            fixture.view.ApplyState(fixture.after, 2, true, rows);
            rows[0] = 10;
            fixture.view.Update(0);
            assertEquals(Color.WHITE, fixture.colorAt(18), "새 이펙트는 처음부터 시작한다");
            assertEquals(Color.BLACK, fixture.colorAt(10), "줄 위치 배열은 복사한다");
        });
    }

    // 이펙트 종료 상태를 먼저 받은 경우 같은 ID의 지연된 시작 메시지도 무시한다.
    @Test
    void remembersAnEffectThatFinishedBeforeReception() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.view.ApplyState(fixture.after, 3, false, null);
            fixture.view.Update(0);
            fixture.view.ApplyState(fixture.before, 3, true, new int[] {19});
            fixture.view.Update(0);
            assertEquals(Color.RED, fixture.colorAt(0));
            assertEquals(Color.BLACK, fixture.colorAt(19));
        });
    }

    // 새 이펙트는 로컬에서 1초간 재생하고 중복 수신으로 남은 시간이 초기화되지 않는다.
    @Test
    void runsForOneLocalSecondWithoutRestartingOnDuplicateReception() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.view.ApplyState(fixture.before, 1, true, new int[] {19});
            fixture.view.Update(500);
            fixture.view.ApplyState(fixture.before, 1, true, new int[] {19});
            fixture.view.Update(499);
            fixture.view.ApplyState(fixture.after, 1, false, null);
            fixture.view.Update(0);
            assertEquals(Color.BLUE, fixture.colorAt(0), "1초가 되기 전에는 수신 상태를 무시한다");

            fixture.view.Update(1);
            fixture.view.ApplyState(fixture.after, 1, false, null);
            fixture.view.Update(0);
            assertEquals(Color.RED, fixture.colorAt(0), "중복 수신과 관계없이 1초 후 새 상태를 받는다");
        });
    }

    private static final class Fixture {
        final SceneRenderState before = state(ColorType.BlockJ, true);
        final SceneRenderState after = state(ColorType.BlockZ, false);
        final RenderConfig config = new RenderConfig(new GameSettings("1280x720", 1280, 720, 0,
                Map.of(), GameSettings.Difficulty.NORMAL, GameSettings.GameMode.NORMAL), 3);
        final RemotePlayerView view = new RemotePlayerView(config, before);

        Color colorAt(int row) {
            HBox content = (HBox) ((BorderPane) view.GetRoot()).getCenter();
            Canvas board = (Canvas) content.getChildren().get(1);
            return board.snapshot(null, null).getPixelReader().getColor(
                    (int) (config.blockSide / 2), (int) ((row + 0.5) * config.blockSide));
        }
    }

    private static SceneRenderState state(ColorType marker, boolean fullBottomRow) {
        BoardElement[][] board = new GameBoard(0).GetBoard();
        board[0][0] = new BoardElement(marker, true, 0);
        if (fullBottomRow) {
            for (int col = 0; col < board[19].length; col++) {
                board[19][col] = new BoardElement(ColorType.BlockJ, true, 0);
            }
        }
        return new SceneRenderState(board, null, 0, new BlockData[0], null);
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
