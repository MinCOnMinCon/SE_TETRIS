package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.Difficulty;
import tetris.settings.GameSettings.GameMode;
import tetris.settings.KeyBindingSettings.ActionType;

class GameControllerTest {
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
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX 초기화 시간 초과");
    }

    // 시작 전에는 ESC 입력을 무시하고, GameStart 후에는 일시정지 요청을 처리하는지 확인한다.
    @Test
    void startEnablesPreviouslyIgnoredInput() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                pressKey(fixture.controller, KeyCode.ESCAPE);
                assertEquals(0, fixture.pauseRequests, "시작 전에는 입력 무시");

                fixture.controller.GameStart();
                pressKey(fixture.controller, KeyCode.ESCAPE);
                assertEquals(1, fixture.pauseRequests, "시작 후에는 입력 처리");
            } finally {
                fixture.controller.StopGame();
            }
        });
    }

    // 일시정지 중에는 낙하 입력을 차단하고, 반복된 일시정지 요청에도 콜백을 한 번만 호출하는지 확인한다.
    @Test
    void pauseBlocksInputAndCallsCallbackOnlyOnce() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.controller.GameStart();
                fixture.controller.PauseGame();
                fixture.controller.PauseGame();
                pressKey(fixture.controller, KeyCode.ESCAPE);
                pressKey(fixture.controller, KeyCode.DOWN);
                assertEquals(1, fixture.pauseRequests, "중복 일시정지 요청 차단");//퍼즈 게임 2번과 esc를 눌렀으나 처음 퍼즈게임만 적용되고 나머지는 무시되는 거 확인

                fixture.controller.StopGame();
                assertEquals(0, fixture.results.get(0).score(), "일시정지 중 낙하 입력 차단");
            } finally {
                fixture.controller.StopGame();
            }
        });
    }

    // 재개 후 낙하 입력으로 점수를 얻을 수 있고, ESC로 다시 일시정지할 수 있는지 확인한다.
    @Test
    void resumeRestoresInputAndAllowsPausingAgain() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.controller.GameStart();
                fixture.controller.PauseGame();
                fixture.controller.ResumeGame();
                pressKey(fixture.controller, KeyCode.DOWN);
                pressKey(fixture.controller, KeyCode.ESCAPE);
                assertEquals(2, fixture.pauseRequests, "재개 후 다시 일시정지 가능");

                fixture.controller.StopGame();
                assertTrue(fixture.results.get(0).score() > 0, "재개 후 낙하 입력 처리");
            } finally {
                fixture.controller.StopGame();
            }
        });
    }

    // 종료 시 현재 점수·난이도·게임 모드를 콜백에 전달하고, 키 입력 연결을 해제하는지 확인한다.
    @Test
    void stopReportsResultAndRemovesKeyHandler() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.controller.GameStart();
                pressKey(fixture.controller, KeyCode.DOWN);
                assertNotNull(fixture.controller.GetRoot().getOnKeyPressed()); // 게임 종료되기 전 키 입력이 있을 때, 그게 진짜 있었는지 확인

                fixture.controller.StopGame();

                assertEquals(1, fixture.results.size()); // 종료시 results 리스트에 gameResult가 들어갔는지 체크
                GameResult result = fixture.results.get(0);
                // 초기 레벨 0에서 소프트 드롭 한 칸은 10점이다.
                assertEquals(10, result.score());
                assertEquals(Difficulty.HARD, result.difficulty());
                assertEquals(GameMode.ITEM, result.gameMode());
                assertNull(fixture.controller.GetRoot().getOnKeyPressed());
            } finally {
                fixture.controller.StopGame();
            }
        });
    }

    // 종료 후 생명주기 메서드를 호출해도 콜백이 중복 실행되거나 키 입력 연결이 복구되지 않는지 확인한다.
    @Test
    void stoppedGameIgnoresFurtherLifecycleCalls() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            try {
                fixture.controller.GameStart();
                fixture.controller.PauseGame();
                fixture.controller.StopGame();

                fixture.controller.StopGame();
                fixture.controller.GameStart(); // 게임이 한번 종료되면 게임 컨트롤러를 새로 생성해서 시작해야함. 이렇게는 시작이 안됨.
                fixture.controller.ResumeGame(); 
                fixture.controller.PauseGame();
                pressKey(fixture.controller, KeyCode.ESCAPE);
                pressKey(fixture.controller, KeyCode.DOWN);

                assertEquals(1, fixture.results.size(), "종료 콜백 중복 방지");
                assertEquals(1, fixture.pauseRequests, "종료 후 일시정지 요청 무시");
                assertNull(fixture.controller.GetRoot().getOnKeyPressed());
                assertEquals(0, fixture.results.get(0).score());
            } finally {
                fixture.controller.StopGame();
            }
        });
    }

    private static final class Fixture {
        private int pauseRequests;
        private final List<GameResult> results = new ArrayList<>();
        private final GameController controller;

        private Fixture() {
            GameSettings settings = new GameSettings(
                    "1280x720", 1280, 720, 0,
                    Map.of(ActionType.PAUSE, List.of(KeyCode.ESCAPE),
                           ActionType.SOFT_DROP, List.of(KeyCode.DOWN)),
                    Difficulty.HARD, GameMode.ITEM);
            controller = new GameController(() -> pauseRequests++, results::add, settings);
        }
    }

    private static void pressKey(GameController controller, KeyCode key) {
        controller.GetRoot().fireEvent(new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
    }

    // 한 작업 안에서 시작부터 종료까지 검증하여 실제 프레임 대기 없이 실행한다.
    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    // 필수인 일시정지 콜백에 null을 전달하면 생성자가 예외를 던지는지 확인한다.
    @Test
    void requiresPauseCallback() {
        assertThrows(NullPointerException.class,
                () -> new GameController(null, new GameSettings(
                        "1280x720", 1280, 720, 0,
                        GameSettings.getInputSettingData().getAllBindings(),
                        GameSettings.Difficulty.NORMAL), 0));
    }
}
