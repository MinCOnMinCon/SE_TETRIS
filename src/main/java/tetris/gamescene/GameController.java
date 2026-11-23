package tetris.gamescene;

import java.util.Objects;
import java.util.Arrays;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.scenerender.RenderConfig;
import tetris.gamescene.scenerender.SceneRenderState;
import tetris.gamescene.scenerender.SceneRenderer;
import tetris.gamescene.score.GameScore;
import tetris.gamescene.blockqueue.BlockQueue;
import tetris.block.data.CurrentBlock;
import tetris.block.move.BlockController;

import tetris.settings.GameSettings;
import tetris.settings.GameSettings.PlayMode;


public class GameController {

	private final GameBoard board;
	private final GameScore score;
	private final GameProgress progress;
	
	private final CurrentBlock currentBlock;

    private final BlockController blockController;

	private final SceneRenderer renderer;
	private final Parent gameRoot;
    private SceneRenderState renderState;

	private final GameSettings gameSettings;
    public final PlayerInput playerInput;
	
	
    private long previousFrameTime;
    private final FrameProfiler profiler;
    private boolean paused;
    private boolean stopped;
    private final int lineClearEffectDuration = 1000; // 밀리초
    private int lineClearEffectTime = 0;
    private boolean lineClearEffectRunning = false;
    private long effectId = 0;

    private final Runnable onPauseRequested;
    private final Consumer<GameResult> onGameEnded;

    

    // 화면 테스트에서 누적 삭제 줄 수를 지정한다.
    public GameController(Runnable onPauseRequested, GameSettings settings, int initialClearedLineCount){
        this(onPauseRequested, result -> {}, settings, initialClearedLineCount);
    }
    // 대전모드 관리자가 사용할 생성자 함수
    public GameController(Consumer<GameResult> onGameEnded, GameSettings settings) {
        this(() -> {}, onGameEnded, settings, 0, 
        Boolean.getBoolean("tetris.profile") ? new FrameProfiler() : null, false, PlayMode.LOCAL);
        //온퍼즈리퀘스티드에 아무것도 안 넣어서 플레이어 인풋이 일시정지 화면을 못 띄우도록 막는다.
        //레지스터 키 인풋을 폴스로 설정해서 컨트롤러가 가지는 루트에 입력이 들어가지 않도록 막는다. 대전모드일 때는 
        // 입력을 관리자가 갖고 있는 루트만 받아야 한다.
    }   

    // 온라인 관리자가 입력을 연결하고 일시정지와 종료를 받아 처리하는 게임을 구성한다.
    public GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings, PlayMode playMode) {
        this(Objects.requireNonNull(onPauseRequested, "onPauseRequested cannot be null"),
                Objects.requireNonNull(onGameEnded, "onGameEnded cannot be null"),
                Objects.requireNonNull(settings, "settings cannot be null"), 0,
                Boolean.getBoolean("tetris.profile") ? new FrameProfiler() : null, false,
                Objects.requireNonNull(playMode, "playMode cannot be null"));
    }

    // 솔로모드용에서 사용하는 생성자 함수
    // 대전 테스트에서 각 플레이어의 누적 삭제 줄 수를 지정한다.
    public GameController(Consumer<GameResult> onGameEnded, GameSettings settings,
            int initialClearedLineCount) {
        this(() -> {}, onGameEnded, settings, initialClearedLineCount,
                Boolean.getBoolean("tetris.profile") ? new FrameProfiler() : null,
                false, PlayMode.LOCAL);
    }

    // 앱 컨트롤러에서 사용하는 생성자 함수
    public GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings){
        this(onPauseRequested, onGameEnded, settings, 0);
    }

    
    //GameSceneTest에서 사용하기 위한 생성자 함수
    public GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings, int initialClearedLineCount){
        this(onPauseRequested, onGameEnded, settings, initialClearedLineCount,
                Boolean.getBoolean("tetris.profile") ? new FrameProfiler() : null);
    }

    // FramePerformanceTest 전용: 테스트가 측정기를 주입한다. 일반 생성자의 프로파일링 설정은 유지한다.
    GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings, int initialClearedLineCount, FrameProfiler profiler){
        this(onPauseRequested, onGameEnded, settings, initialClearedLineCount, profiler, true, PlayMode.SOLO);
    }
    

    private GameController(Runnable onPauseRequested, Consumer<GameResult> onGameEnded,
            GameSettings settings, int initialClearedLineCount, FrameProfiler profiler,
            boolean registerKeyInput, PlayMode playMode) { // registerkeyInput이 트루면 게임 컨트롤러에서 입력을 처리하고 아니면 다른 클래스에서 처리한다.
        this.profiler = profiler;
        this.onPauseRequested = Objects.requireNonNull(onPauseRequested);
        this.onGameEnded = Objects.requireNonNull(onGameEnded);

		gameSettings = settings;
        previousFrameTime = 0;
		board = new GameBoard(settings.colorMode());
		progress = new GameProgress();
        progress.OnLinesCleared(initialClearedLineCount);
		score = new GameScore(progress);
		
		BlockQueue blockQueue = new BlockQueue(settings.colorMode(), progress, settings.difficulty(), settings.gameMode());
		
		currentBlock = new CurrentBlock(blockQueue, board, this::StopGame);

        blockController = new BlockController(currentBlock, score, progress,
                this::StartLineClearEffect, gameSettings.difficulty());


		
		RenderConfig renderConfig = new RenderConfig(gameSettings, blockQueue.GetMaxQueueSize());
		renderer = new SceneRenderer(renderConfig, playMode);
		// renderconfig에 블럭 큐 최대 사이즈 필요해 이렇게 전달함.
		
        //대전모드면 퍼즈 게임 콜백
        gameRoot = renderer.CreateRoot(GetRenderState());
        if (registerKeyInput) { 
            playerInput = new PlayerInput(gameSettings.keyBindings(), blockController, this::PauseGame);
            gameRoot.setOnKeyPressed(event -> playerInput.HandleKeyCode(event.getCode()));
        }
        else{
            playerInput = new PlayerInput(gameSettings.keyBindings(), blockController, onPauseRequested);
        }
        // 싱글모드라 레지스터 키 인풋이 참이면 퍼즈 게임을 전달하고 루트에 입력을 연결
        // 대전 모드에서는 관리자가 입력을 연결하고 전달받은 콜백으로 일시정지를 처리한다.
        // TODO: 온라인 대전모드면 registerKeyInput이 참이고, 그때는 퍼즈를 온라인 버서스 매니저에게 알려야 함. 퍼즈 게임을 고치던가 여기서 고치던가 해야해.
        playerInput.SetInputEnabled(false);
    }

	private final AnimationTimer gameLoop = new AnimationTimer() {
		@Override
		public void handle(long now) {
            if (stopped || paused) {
                return;
            }
            if (profiler == null) {
                updateFrame(now);
            } else {
                profiler.measure(() -> updateFrame(now));
            }
        }
    };

    private void StartLineClearEffect() {
        effectId++;
        lineClearEffectRunning = true;
        lineClearEffectTime = 0;
        playerInput.SetInputEnabled(false);
    }

    private void updateFrame(long now) {
        if (previousFrameTime == 0) { // 첫 프레임에는 업데이트 동작을 하지 않고 이전 프레임 타임만 업데이트 함. 그 다음부터 이전 프레임 타임을 사용해 델타 타임을 계산해 사용
            previousFrameTime = now;
            return;
        }

        int deltaTime = (int) ((now - previousFrameTime) / 1_000_000);
        previousFrameTime = now;
        int[] clearedRows = board.GetClearedRows();

        if (lineClearEffectRunning) {
            renderer.UpdateRoot(GetRenderState());
            renderer.DrawLineClearEffect(clearedRows, lineClearEffectTime);
            lineClearEffectTime += deltaTime;

            if (lineClearEffectTime >= lineClearEffectDuration) {
                blockController.FinishDeleteAfterEffect();
                Arrays.fill(clearedRows, -1);
                lineClearEffectRunning = false;
                playerInput.SetInputEnabled(!stopped && !paused);
                renderer.UpdateRoot(GetRenderState());
            }
        } else {
            blockController.TimeUpdate(deltaTime); // automove 호출
            if (stopped || lineClearEffectRunning) { // 자동 낙하로 게임 종료 또는 줄 삭제 이펙트가 시작될 수 있다.
                return;
            }

            renderer.UpdateRoot(GetRenderState());
        }
    }


    public void StopGame() {
        if (stopped) {
            return;
        }
        SceneRenderState state = new SceneRenderState(board.GetBoard(), null, score.GetGameScore(),
                currentBlock.GetBlockQueue().GetBlocksInQueueOrder(), currentBlock.GetBlockHolding(), currentBlock.GetVersusBoards());
            renderer.UpdateRoot(state);
        GameResult result = new GameResult(
            score.GetGameScore(), gameSettings.difficulty(), gameSettings.gameMode());

        CleanGame();
        onGameEnded.accept(result);
    }

    public void CleanGame() {
        if (stopped) {
            return;
        }
        stopped = true;
        gameLoop.stop();
        playerInput.SetInputEnabled(false);
        gameRoot.setOnKeyPressed(null);
        if (profiler != null) profiler.saveReport();
    }

	public void PauseGame() {
		if (stopped || paused) {
			return;
		}

		paused = true;
		playerInput.SetInputEnabled(false);
		gameLoop.stop();
		if (profiler != null) {
            profiler.resetContinuity();
            profiler.saveReport();
        }

		onPauseRequested.run(); // 
	}

	public void ResumeGame() {
		if (stopped || !paused) {
			return;
		}

		previousFrameTime = 0;
		if (profiler != null) profiler.resetContinuity();
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

	public void GameStart() {
        if (stopped) {
            return;
        }
		previousFrameTime = 0;
		if (profiler != null) profiler.resetContinuity();
		paused = false;
		playerInput.SetInputEnabled(!lineClearEffectRunning);
		gameLoop.start();
	}

    // 조회 시점의 게임 상태를 저장해 반환하며 내부 배열과 블록은 복사하지 않는다.
    public SceneRenderState GetRenderState() {
        renderState = new SceneRenderState(board.GetBoard(), currentBlock.GetCurrentBlock(), score.GetGameScore(),
                currentBlock.GetBlockQueue().GetBlocksInQueueOrder(), currentBlock.GetBlockHolding(), currentBlock.GetVersusBoards());
        return renderState;
    }

    // 현재 줄 삭제 이펙트가 진행 중인지 반환한다.
    public boolean GetLineClearEffectRunning() {
        return lineClearEffectRunning;
    }

    // 줄 삭제 이펙트를 표시할 행 번호 배열을 반환한다.
    public int[] GetClearedRows() {
        return board.GetClearedRows();
    }

    // 마지막으로 시작한 줄 삭제 이펙트의 ID를 반환한다.
    public long GetEffectId() {
        return effectId;
    }

    public int GetScore() {
        return score.GetGameScore();
    }

    public BoardElement[][] GetDelBoard() {
        return currentBlock.GetDelBoard();
    }

    public void AddVersusBoards(BoardElement[][] versusBoard) {
        currentBlock.AddVersusBoards(versusBoard);
    }

	public Parent GetRoot() {
		return gameRoot;
	}

    // FramePerformanceTest 전용: FX 스레드에서만 시나리오 준비·입력에 사용하며 리플렉션을 피한다.
    FramePerformanceTestAccess accessForFramePerformanceTest() {
        return new FramePerformanceTestAccess(board, currentBlock, playerInput,
                !stopped && !paused && !lineClearEffectRunning && board.GetClearedRows()[0] == -1);
    }

    // FramePerformanceTest 전용 접근 묶음. 게임 코드의 일반 상태 조회 API로 사용하지 않는다.
    record FramePerformanceTestAccess(GameBoard board, CurrentBlock currentBlock,
            PlayerInput input, boolean ready) {}

	
}
