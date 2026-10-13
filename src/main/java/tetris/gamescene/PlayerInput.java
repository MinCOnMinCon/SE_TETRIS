package tetris.gamescene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tetris.settings.KeyBindingSettings; // 설정 화면과 공유하는 동작 종류
import javafx.scene.input.KeyCode;
import tetris.block.data.CurrentBlock;
import tetris.block.move.BlockMove;
import tetris.block.move.UserMove;
import tetris.block.rotate.BlockRotate;
import tetris.gamescene.board.GameBoard;
import tetris.gamescene.score.GameScore;

public class PlayerInput {
	
	private final Map<KeyCode, KeyBindingSettings.ActionType> keyBindings;
	// final은 참조 교체만 막는다. 공유 객체 내부까지 읽기 전용으로 만들지는 못한다.
	// PlayerInput에서는 setter나 배열 대입으로 상태를 직접 수정하지 않는다.
	// 상태 변경은 이동·회전 함수에 위임하고, 입력마다 현재 블록과 보드를 조회한다.
	private final CurrentBlock currentBlock;
	private final GameBoard board;
	private final Runnable pauseGame;
	private final GameScore score;
	private boolean inputEnabled = true;

	public PlayerInput(Map<KeyBindingSettings.ActionType, List<KeyCode>> keyBindingData, CurrentBlock currentBlock, GameBoard board, GameScore score, Runnable pauseGame) {
		this.currentBlock = currentBlock;
		this.board = board;
		this.pauseGame = pauseGame;
		this.score = score;
		Map<KeyCode, KeyBindingSettings.ActionType> bindings = new HashMap<>();

		keyBindingData.forEach((actionType, keyCodes) -> { // 저장한 각 동작의 키 목록을 읽음
			for (KeyCode keyCode : keyCodes) {
				bindings.put(keyCode, actionType); // 키를 누르면 동작을 찾을 수 있도록 역방향 맵 구성
			}
		});

		keyBindings = Map.copyOf(bindings);
        // 해당 클래스의 키 바인딩 키-밸류를 수정하는 걸 막기 위한 코드
	}

	public void HandleKeyCode(KeyCode keyCode) {
		if (!inputEnabled) {
			return;
		}

		KeyBindingSettings.ActionType actionType = keyBindings.get(keyCode);

		if (actionType == null) {
			return;
		}

		switch (actionType) {
			case MOVE_LEFT:
				BlockMove.MoveLeft(currentBlock, board.GetBoard());
				break;
			case MOVE_RIGHT:
				BlockMove.MoveRight(currentBlock, board.GetBoard());
				break;
			case SOFT_DROP:
				UserMove.MoveDown(currentBlock, board.GetBoard(), score, board.GetClearedRows());
				break;
			case HARD_DROP:
				UserMove.MoveDownMax(currentBlock, board.GetBoard(), score, board.GetClearedRows());
				break;
			case ROTATE_CLOCK:
				BlockRotate.Rotate(currentBlock.GetCurrentBlock(), board.GetBoard(), 0);
				break;
			case ROTATE_COUNTERCLOCK:
				BlockRotate.Rotate(currentBlock.GetCurrentBlock(), board.GetBoard(), 1);
				break;
			case HOLD:
				UserMove.BlockHolding(currentBlock, board.GetBoard());
				break;
			case PAUSE:
				pauseGame.run();
				break;
		}
	}

	public void SetInputEnabled(boolean inputEnabled) {
		this.inputEnabled = inputEnabled;
	}

	
}
