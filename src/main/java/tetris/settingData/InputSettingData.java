package tetris.settingData;

import java.util.EnumMap;
import java.util.Map;
import java.util.Collections;

import javafx.scene.input.KeyCode;

public class InputSettingData {
    public enum ActionType {
        MOVE_LEFT,
        MOVE_RIGHT,
        SOFT_DROP,
        HARD_DROP,
        ROTATE_CLOCK,
        ROTATE_COUNTERCLOCK,
        PAUSE
    }

    private final Map<ActionType, KeyCode> keyBindings = new EnumMap<>(ActionType.class);

    public InputSettingData() {
        this(new KeyCode[] {
            KeyCode.LEFT,
            KeyCode.RIGHT,
            KeyCode.DOWN,
            KeyCode.SPACE,
            KeyCode.COMMA,
            KeyCode.PERIOD,
            KeyCode.ESCAPE
        });
    }

    public InputSettingData(KeyCode[] keyCodes) {
        ActionType[] actionTypes = ActionType.values();

        if (keyCodes == null || keyCodes.length != actionTypes.length) {
            throw new IllegalArgumentException("keyCodes must contain one value for each ActionType");
        }

        for (int index = 0; index < actionTypes.length; index++) {
            keyBindings.put(actionTypes[index], keyCodes[index]);
        }
    }

    public KeyCode GetKeyCode(ActionType actionType) {
        return keyBindings.get(actionType);
    }

    public Map<ActionType, KeyCode> GetKeyBindings() {
        return Collections.unmodifiableMap(keyBindings);
    }

    public void SetKeyCode(ActionType actionType, KeyCode keyCode){
        if(keyBindings.containsKey(actionType)){
            keyBindings.replace(actionType, keyCode);
        }
    }
}
/*
시나리오
- 기본 생성자 : 최초 게임 실행 혹은 저장된 세팅 데이터 삭제시 입력 매핑 기본 설정
- 설정에서 키바인딩 변경 : 변경된 키바인딩에 저장되어 다음 게임을 실행할 땐 그 저장된 값을 가져옴.
시작화면에서 게임으로 넘어오면 해당 키 바인딩(키-액션 타입, 밸류-키코드)으로
키와 밸류가 반대가 된(키 - 키코드, 밸류 - 액션타입)맵 생성후 사용
이렇게 쓰는 이유는 설정에서는 액션타입을 지정해서 키코드를 바꾸고
게임에서는 키코드를 입력받아 특정 액션을 하기 때문이다.
 */ 