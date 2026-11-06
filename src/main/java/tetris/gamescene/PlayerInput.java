package tetris.gamescene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tetris.settings.KeyBindingSettings; // 설정 화면과 공유하는 동작 종류
import tetris.settings.KeyBindingSettings.ActionType;
import javafx.scene.input.KeyCode;
import tetris.block.move.BlockController;

public class PlayerInput {
	
	private final Map<KeyCode, KeyBindingSettings.ActionType> keyBindings;
	// final은 참조 교체만 막는다. 공유 객체 내부까지 읽기 전용으로 만들지는 못한다.
	// PlayerInput에서는 setter나 배열 대입으로 상태를 직접 수정하지 않는다.
	// 상태 변경은 이동·회전 함수에 위임하고, 입력마다 현재 블록과 보드를 조회한다.
	private final BlockController blockController;
	private final Runnable pauseGame;
	private boolean inputEnabled = true;

	public PlayerInput(Map<KeyBindingSettings.ActionType, List<KeyCode>> keyBindingData, BlockController blockController, Runnable pauseGame) {
		this.blockController = blockController;
		this.pauseGame = pauseGame;
		Map<KeyCode, KeyBindingSettings.ActionType> bindings = new HashMap<>();

		keyBindingData.forEach((actionType, keyCodes) -> { // 저장한 각 동작의 키 목록을 읽음
			for (KeyCode keyCode : keyCodes) {
				bindings.put(keyCode, actionType); // 키를 누르면 동작을 찾을 수 있도록 역방향 맵 구성
			}
		});

		keyBindings = Map.copyOf(bindings);
        // 해당 클래스의 키 바인딩 키-밸류를 수정하는 걸 막기 위한 코드
	}

	public void HandleKeyCode(KeyCode keyCode) { // 1인 모드에서 플레이어의 키 입력을 처리
		
		KeyBindingSettings.ActionType action = keyBindings.get(keyCode);

		HandleAction(action);

	}

	public void HandleAction(ActionType action){ 
		// 대전 모드일 때, 대전모드 관리자가 플레이어 입력이 누구껀지 확인해 그 플레이어의 인풋 매니저의 이 함수를 호출함. 
		// 그때 파라미터로 입력에 맞는 액션 타입을 제공해서 처리하게 한다.
		if (!inputEnabled) {
			return;
		}
		if (action == null) {
			return;
		}

		switch (action) {
			case MOVE_LEFT:
				blockController.MoveLeft();
				break;
			case MOVE_RIGHT:
				blockController.MoveRight();
				break;
			case SOFT_DROP:
				blockController.MoveDown();
				break;
			case HARD_DROP:
				blockController.MoveDownMax();
				break;
			case ROTATE_CLOCK:
				blockController.RotateClock();
				break;
			case ROTATE_COUNTERCLOCK:
				blockController.RotateCounterClock();
				break;
			case HOLD:
				blockController.BlockHolding();
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
