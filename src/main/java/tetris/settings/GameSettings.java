package tetris.settings;

import java.util.Collections; // 읽기 전용 설정 복사본 생성
import java.util.EnumMap; // 동작 enum을 키로 하는 설정 맵 생성
import java.util.List; // 동작별 키 목록 표현
import java.util.Map; // 동작별 키 설정 표현

import javafx.scene.input.KeyCode; // 저장된 키 입력 종류 표현
import tetris.settings.KeyBindingSettings.ActionType; // 키 설정 동작 종류

// 저장 버튼을 누른 시점의 설정을 게임 로직에 전달하는 불변 객체
public record GameSettings(
        String resolutionPreset,
        int screenWidth,
        int screenHeight,
        int colorMode,
        Map<ActionType, List<KeyCode>> keyBindings) {

    // 전달받은 키 설정을 복사해 게임 실행 중 외부에서 바뀌지 않게 함
    public GameSettings {
        EnumMap<ActionType, List<KeyCode>> copiedBindings = new EnumMap<>(ActionType.class);
        keyBindings.forEach((action, keys) -> copiedBindings.put(action, List.copyOf(keys)));
        keyBindings = Collections.unmodifiableMap(copiedBindings);
    }
}