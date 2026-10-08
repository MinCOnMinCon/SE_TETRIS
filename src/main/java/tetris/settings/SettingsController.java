package tetris.settings;

import javafx.event.EventHandler; // 키 입력 필터 해제에 같은 인스턴스를 쓰기 위함
import javafx.scene.control.ComboBox; // 해상도와 색상 모드 선택 상자
import javafx.scene.Scene; // 키 입력을 감지할 화면
import javafx.scene.control.Button; // 역할별 키 버튼
import javafx.scene.control.Label; // 상태 안내 문구
import javafx.scene.input.KeyCode; // 입력된 키 종류
import javafx.scene.input.KeyEvent; // 키보드 이벤트
import javafx.scene.layout.HBox; // 역할 행
import javafx.scene.layout.VBox; // 역할 행 목록
import javafx.stage.Stage; // 창 크기 변경을 위해 필요
import tetris.settings.KeyBindingSettings.ActionType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 설정 화면의 동작 로직을 담당하는 컨트롤러 클래스
public class SettingsController {
    // 현재 창과 각 UI 요소를 저장
    private final Stage stage; // 현재 앱 창
    private final Scene scene; // 현재 설정 화면
    private final ComboBox<String> resolutionComboBox; // 해상도 선택 상자
    private final ComboBox<GameSettings.ColorBlindMode> colorBlindModeComboBox; // 색상 모드 선택 상자
    private final KeyBindingSettings inputSettingData; // 키 배정 데이터
    private final VBox keyBindingRows; // 역할별 행 목록
    private final Label keySettingsStatus; // 키 변경 상태 표시
    private final Button saveSettingsButton; // 현재 설정을 저장하는 버튼
    private final Map<ActionType, Button> keyButtons = new EnumMap<>(ActionType.class); // 역할과 버튼 연결
    private ActionType actionWaitingForKey; // 새 키 입력을 기다리는 역할
    private final EventHandler<KeyEvent> keyCaptureHandler = this::captureKeyBinding;

    // 생성자: 필요한 UI 객체를 받아 저장
    public SettingsController(
            Stage stage,
            Scene scene,
            ComboBox<String> resolutionComboBox,
            ComboBox<GameSettings.ColorBlindMode> colorBlindModeComboBox,
            KeyBindingSettings inputSettingData,
            VBox keyBindingRows,
            Label keySettingsStatus,
            Button saveSettingsButton) {
        this.stage = stage;
        this.scene = scene;
        this.resolutionComboBox = resolutionComboBox;
        this.colorBlindModeComboBox = colorBlindModeComboBox;
        this.inputSettingData = inputSettingData;
        this.keyBindingRows = keyBindingRows;
        this.keySettingsStatus = keySettingsStatus;
        this.saveSettingsButton = saveSettingsButton;
    }

    // 화면이 열릴 때 기본값과 이벤트를 설정
    public void initialize() { // 설정 화면의 기본값과 이벤트 초기화
        SettingsStore.load(); // 저장된 설정을 먼저 복원

        // 해상도 옵션을 드롭다운에 추가
        resolutionComboBox.getItems().addAll(GameSettings.RESOLUTION_PRESETS);

        // 저장된 해상도와 색상 모드를 선택 상태로 표시
        resolutionComboBox.setValue(GameSettings.getResolutionPreset());
        colorBlindModeComboBox.getItems().setAll(GameSettings.ColorBlindMode.values());
        colorBlindModeComboBox.setValue(GameSettings.getColorBlindMode());
        updateResolution();

        // 해상도 선택 시 이벤트 연결
        resolutionComboBox.setOnAction(e -> {
            GameSettings.setResolutionPreset(resolutionComboBox.getValue());
            updateResolution();
        });

        colorBlindModeComboBox.setOnAction(e -> {
            GameSettings.setColorBlindMode(colorBlindModeComboBox.getValue());
        });

        saveSettingsButton.setOnAction(e -> { // 버튼을 눌렀을 때만 파일 저장과 객체 생성을 수행
            GameSettings savedSettings = SettingsStore.save();
            if (savedSettings == null) {
                keySettingsStatus.setText("설정을 저장하지 못했습니다.");
                return;
            }
            keySettingsStatus.setText("설정이 저장되었습니다.");
        });

        // 역할별 키 버튼을 만들고 씬에서 다음 키 입력을 감지
        createKeyBindingRows();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyCaptureHandler);
    }

    // 같은 Scene을 다른 화면과 공유하므로, 설정 화면을 떠날 때 키 필터를 제거한다.
    public void dispose() {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyCaptureHandler);
    }

    // 선택된 해상도에 맞춰 창 크기 변경
    private void updateResolution() { // 선택한 해상도로 창 크기 변경
        String selected = resolutionComboBox.getValue();
        int index = -1;

        // 선택 값이 어떤 해상도인지 찾기
        for (int i = 0; i < GameSettings.RESOLUTION_PRESETS.length; i++) {
            if (GameSettings.RESOLUTION_PRESETS[i].equals(selected)) {
                index = i;
                break;
            }
        }

        // 유효한 해상도면 창 크기 조절
        if (index >= 0) {
            int width = GameSettings.RESOLUTION_VALUES[index][0];
            int height = GameSettings.RESOLUTION_VALUES[index][1];
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }

    private void createKeyBindingRows() { // 역할별 버튼에 현재 키와 이벤트 연결
        for (ActionType actionType : ActionType.values()) {
            HBox row = (HBox) keyBindingRows.getChildren().get(actionType.ordinal());
            Button keyButton = (Button) row.getChildren().get(1);
            keyButtons.put(actionType, keyButton);
            updateKeyButton(actionType);
            keyButton.setOnAction(event -> {
                actionWaitingForKey = actionType;
                keySettingsStatus.setText(actionType.getDisplayName() + "에 지정할 키를 입력하세요.");
                scene.getRoot().requestFocus();
            });
        }
    }

    private void captureKeyBinding(KeyEvent event) { // 다음 키 입력을 새 배정값으로 처리
        if (actionWaitingForKey == null) {
            return;
        }

        KeyCode newKey = event.getCode();
        boolean alreadyUsed = inputSettingData.getAllBindings().entrySet().stream()
                .anyMatch(binding -> binding.getKey() != actionWaitingForKey
                        && binding.getValue().contains(newKey));

        if (alreadyUsed) {
            keySettingsStatus.setText(newKey.getName() + " 키는 다른 역할에 이미 배정되어 있습니다.");
        } else {
            ActionType updatedAction = actionWaitingForKey;
            inputSettingData.setKeyCode(updatedAction, newKey);
            updateKeyButton(updatedAction);
            keySettingsStatus.setText(updatedAction.getDisplayName() + " 키를 " + newKey.getName() + "(으)로 변경했습니다.");
            actionWaitingForKey = null;
        }

        event.consume();
    }

    private void updateKeyButton(ActionType actionType) { // 버튼에 현재 키 목록 표시
        List<KeyCode> keyCodes = inputSettingData.getKeyCodes(actionType);
        String displayedKeys = keyCodes.stream()
                .map(KeyCode::getName)
                .collect(Collectors.joining(" / "));
        keyButtons.get(actionType).setText(displayedKeys);
    }

}
