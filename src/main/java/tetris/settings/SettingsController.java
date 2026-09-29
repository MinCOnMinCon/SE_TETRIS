package tetris.settings;

import javafx.scene.control.Button; // 버튼 사용
import javafx.scene.control.ComboBox; // 해상도 선택용 드롭다운
import javafx.stage.Stage; // 창 크기 변경을 위해 필요
import javafx.scene.layout.VBox; // 콘텐츠 배치용 레이아웃

// 설정 화면의 동작 로직을 담당하는 컨트롤러 클래스
public class SettingsController {
    // 현재 창과 각 UI 요소를 저장
    private final Stage stage;
    private final VBox contentBox;
    private final Button colorBlindModeButton;
    private final ComboBox<String> resolutionComboBox;

    // 색맹 모드 상태
    private boolean colorBlindModeEnabled = false;

    // 생성자: 필요한 UI 객체를 받아 저장
    public SettingsController(Stage stage, VBox contentBox, Button colorBlindModeButton, ComboBox<String> resolutionComboBox) {
        this.stage = stage;
        this.contentBox = contentBox;
        this.colorBlindModeButton = colorBlindModeButton;
        this.resolutionComboBox = resolutionComboBox;
    }

    // 화면이 열릴 때 기본값과 이벤트를 설정
    public void initialize() {
        // 해상도 옵션을 드롭다운에 추가
        resolutionComboBox.getItems().addAll(SettingsConstants.RESOLUTION_PRESETS);

        // 기본 선택값은 720x1280으로 설정
        resolutionComboBox.setValue(SettingsConstants.RESOLUTION_PRESETS[0]);

        // 해상도 선택 시 이벤트 연결
        resolutionComboBox.setOnAction(e -> updateResolution());

        // 색맹 모드 버튼 초기 상태 표시
        updateColorBlindButtonLabel();

        // 색맹 모드 버튼 클릭 시 상태 전환
        colorBlindModeButton.setOnAction(e -> {
            colorBlindModeEnabled = !colorBlindModeEnabled;
            updateColorBlindButtonLabel();
        });
    }

    // 선택된 해상도에 맞춰 창 크기 변경
    private void updateResolution() {
        String selected = resolutionComboBox.getValue();
        int index = -1;

        // 선택 값이 어떤 해상도인지 찾기
        for (int i = 0; i < SettingsConstants.RESOLUTION_PRESETS.length; i++) {
            if (SettingsConstants.RESOLUTION_PRESETS[i].equals(selected)) {
                index = i;
                break;
            }
        }

        // 유효한 해상도면 창 크기 조절
        if (index >= 0) {
            int width = SettingsConstants.RESOLUTION_VALUES[index][0];
            int height = SettingsConstants.RESOLUTION_VALUES[index][1];
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }

    // 색맹 모드 상태에 따라 버튼 텍스트와 색상 변경
    private void updateColorBlindButtonLabel() {
        if (colorBlindModeEnabled) {
            colorBlindModeButton.setText("색맹 모드: ON");
            colorBlindModeButton.setStyle("-fx-background-color: #4da3ff; -fx-text-fill: white;");
        } else {
            colorBlindModeButton.setText("색맹 모드: OFF");
            colorBlindModeButton.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black;");
        }
    }
}
