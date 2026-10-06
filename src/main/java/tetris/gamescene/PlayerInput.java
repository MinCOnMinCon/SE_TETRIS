package tetris.gamescene;

import java.util.HashMap;
import java.util.Map;

import tetris.settings.GameSettings; // 저장된 키 배정 객체
import tetris.settings.KeyBindingSettings.ActionType; // 설정 화면과 공유하는 동작 종류
import javafx.scene.input.KeyCode;

public class PlayerInput {
	
	private final Map<KeyCode, ActionType> keyBindings;

	public PlayerInput(GameSettings settings) {
		Map<KeyCode, ActionType> bindings = new HashMap<>();

		settings.keyBindings().forEach((actionType, keyCodes) -> { // 저장한 각 동작의 키 목록을 읽음
			for (KeyCode keyCode : keyCodes) {
				bindings.put(keyCode, actionType); // 키를 누르면 동작을 찾을 수 있도록 역방향 맵 구성
			}
		});

		keyBindings = Map.copyOf(bindings);
        // 해당 클래스의 키 바인딩 키-밸류를 수정하는 걸 막기 위한 코드
	}

	public void HandleKeyCode(KeyCode keyCode) {
		ActionType actionType = keyBindings.get(keyCode);

		if (actionType == null) {
			return;
		}

		switch (actionType) {
			case MOVE_LEFT:
				break;
			case MOVE_RIGHT:
				break;
			case SOFT_DROP:
				break;
			case HARD_DROP:
				break;
			case ROTATE_CLOCK:
				break;
			case ROTATE_COUNTERCLOCK:
				break;
			case HOLD:
				break;
			case PAUSE:
				break;
		}
	}
    
}
