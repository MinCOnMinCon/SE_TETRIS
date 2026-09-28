package tetris.gamescene;

import java.util.HashMap;
import java.util.Map;

import tetris.settingData.InputSettingData;
import tetris.settingData.InputSettingData.ActionType;
import javafx.scene.input.KeyCode;

public class PlayerInput {
	
	private final Map<KeyCode, ActionType> keyBindings;

	public PlayerInput(InputSettingData inputSettingData) {
		Map<KeyCode, ActionType> bindings = new HashMap<>();

		for (Map.Entry<ActionType, KeyCode> binding
				: inputSettingData.GetKeyBindings().entrySet()) {
			bindings.put(binding.getValue(), binding.getKey());
		} // 키바인딩을 받아 모든 엔트리를 binding 하나씩 받아 playerInput의 keybindings에 넣음

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
			case PAUSE:
				break;
		}
	}
    
}
