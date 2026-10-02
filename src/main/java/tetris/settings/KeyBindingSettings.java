package tetris.settings;

import java.util.Collections; // 수정 불가능한 결과 제공
import java.util.EnumMap; // enum을 키로 사용하는 맵
import java.util.LinkedHashMap; // 입력 순서를 유지하는 맵
import java.util.List; // 역할별 키 목록
import java.util.Map; // 키 배정 자료 구조

import javafx.scene.input.KeyCode; // JavaFX 키 코드

// 설정 화면에서 키 역할과 배정된 기본/보조 키를 관리
public final class KeyBindingSettings {
    // 게임에서 수행할 수 있는 동작 목록
    public enum ActionType {
        MOVE_LEFT("왼쪽 이동"),
        MOVE_RIGHT("오른쪽 이동"),
        SOFT_DROP("소프트 드롭"),
        HARD_DROP("하드 드롭"),
        ROTATE_CLOCK("시계 방향 회전"),
        ROTATE_COUNTERCLOCK("반시계 방향 회전"),
        HOLD("홀드"),
        PAUSE("일시정지");

        private final String displayName; // 설정 화면에 표시할 역할 이름

        ActionType(String displayName) { // 역할 이름을 저장
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Map<ActionType, List<KeyCode>> bindings = new EnumMap<>(ActionType.class); // 역할별 키 저장

    public KeyBindingSettings() { // 기본 키 배정을 생성
        bindings.put(ActionType.MOVE_LEFT, List.of(KeyCode.LEFT));
        bindings.put(ActionType.MOVE_RIGHT, List.of(KeyCode.RIGHT));
        bindings.put(ActionType.SOFT_DROP, List.of(KeyCode.DOWN));
        bindings.put(ActionType.HARD_DROP, List.of(KeyCode.SPACE));
        bindings.put(ActionType.ROTATE_CLOCK, List.of(KeyCode.X, KeyCode.UP));
        bindings.put(ActionType.ROTATE_COUNTERCLOCK, List.of(KeyCode.Z, KeyCode.CONTROL));
        bindings.put(ActionType.HOLD, List.of(KeyCode.SHIFT, KeyCode.C));
        bindings.put(ActionType.PAUSE, List.of(KeyCode.ESCAPE));
    }

    public List<KeyCode> getKeyCodes(ActionType actionType) { // 특정 역할의 키 목록 반환
        return bindings.getOrDefault(actionType, List.of()); // 없으면 빈 목록 반환
    }

    public Map<ActionType, List<KeyCode>> getAllBindings() { // 전체 키 배정의 읽기 전용 복사본 반환
        Map<ActionType, List<KeyCode>> copy = new LinkedHashMap<>();
        bindings.forEach((actionType, keyCodes) -> copy.put(actionType, List.copyOf(keyCodes)));
        return Collections.unmodifiableMap(copy);
    }

    public void setKeyCode(ActionType actionType, KeyCode keyCode) { // 특정 역할의 키를 하나로 변경
        if (actionType != null && keyCode != null && bindings.containsKey(actionType)) {
            bindings.put(actionType, List.of(keyCode));
        }
    }

    public void setKeyCodes(ActionType actionType, List<KeyCode> keyCodes) { // 특정 역할의 키 목록을 변경
        if (actionType != null && keyCodes != null && !keyCodes.isEmpty()
                && keyCodes.stream().noneMatch(keyCode -> keyCode == null)) {
            bindings.put(actionType, List.copyOf(keyCodes));
        }
    }
}