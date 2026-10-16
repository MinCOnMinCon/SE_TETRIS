package tetris.settings;

import javafx.geometry.Insets; // 여백 설정
import javafx.geometry.Pos; // 정렬 위치
import javafx.scene.Scene; // JavaFX 화면 객체
import javafx.scene.control.ComboBox; // 선택 상자
import javafx.scene.control.Button; // 버튼 컨트롤
import javafx.scene.control.Label; // 텍스트 라벨
import javafx.scene.layout.BorderPane; // 전체 화면 배치 레이아웃
import javafx.scene.layout.HBox; // 역할 이름과 키 버튼을 한 줄로 배치
import javafx.scene.layout.Priority; // 키 버튼이 남은 너비를 사용하도록 설정
import javafx.scene.layout.VBox; // 세로 배치 레이아웃
import javafx.scene.control.ScrollPane; // 설정 항목이 창보다 많을 때 스크롤
import javafx.stage.Stage; // 창 객체
import tetris.settings.KeyBindingSettings.ActionType; // 키 역할 목록

// 설정 화면을 보여주는 클래스
public class SettingsScreen {
    private BorderPane root;
    private SettingsController controller;

    public BorderPane showSS() {
        return root;
    }

    public void activate() {
        controller.activate();
    }

    public void makeSS(Stage stage, Scene scene, Runnable onBack) {
        Label title = new Label("설정");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        ComboBox<String> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.setPromptText("화면 해상도 선택");
        resolutionComboBox.setPrefWidth(220);

        ComboBox<GameSettings.ColorBlindMode> colorBlindModeComboBox = new ComboBox<>();
        colorBlindModeComboBox.setPromptText("색상 모드 선택");
        colorBlindModeComboBox.setPrefWidth(220);

        Label keySettingsTitle = new Label("키 설정");
        keySettingsTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label keySettingsStatus = new Label("역할 버튼을 누른 뒤 새 키를 입력하세요.");
        keySettingsStatus.setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
        VBox keyBindingRows = new VBox(8);

        for (ActionType actionType : ActionType.values()) {
            Label actionLabel = new Label(actionType.getDisplayName());
            actionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
            actionLabel.setPrefWidth(190);

            Button keyButton = new Button();
            keyButton.setMaxWidth(Double.MAX_VALUE);
            keyButton.setStyle("-fx-font-size: 16px;");
            HBox.setHgrow(keyButton, Priority.ALWAYS);

            HBox row = new HBox(12, actionLabel, keyButton);
            row.setAlignment(Pos.CENTER_LEFT);
            keyBindingRows.getChildren().add(row);
        }

        Button backButton = new Button("뒤로가기");
        backButton.setPrefSize(180, 50);
        backButton.setStyle("-fx-font-size: 16px;");

        Button saveSettingsButton = new Button("설정 저장");
        saveSettingsButton.setPrefSize(180, 44);
        saveSettingsButton.setStyle("-fx-font-size: 16px;");

        Button resetSettingsButton = new Button("설정 초기화");
        resetSettingsButton.setPrefSize(180, 50);
        resetSettingsButton.setStyle("-fx-font-size: 16px;");

        Button resetScoreButton = new Button("스코어보드 초기화");
        resetScoreButton.setPrefSize(180, 50);
        resetScoreButton.setStyle("-fx-font-size: 16px;");

        VBox content = new VBox(14, resolutionComboBox, colorBlindModeComboBox,
                keySettingsTitle, keySettingsStatus, keyBindingRows,
                saveSettingsButton, resetSettingsButton);
        content.setAlignment(Pos.CENTER);
        content.setFillWidth(false);
        content.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black; -fx-background-color: black;");

        root = new BorderPane();
        root.setStyle("-fx-background-color: black; -fx-font-size: 16px;");
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setCenter(scrollPane);

        VBox bottomButtons = new VBox(10, resetScoreButton, backButton);
        bottomButtons.setAlignment(Pos.CENTER);
        root.setBottom(bottomButtons);
        BorderPane.setAlignment(bottomButtons, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(bottomButtons, new Insets(12));

        controller = new SettingsController(stage, scene, resolutionComboBox,
                colorBlindModeComboBox, GameSettings.getInputSettingData(), keyBindingRows,
            keySettingsStatus, saveSettingsButton, resetSettingsButton, resetScoreButton,
            backButton);
        backButton.setOnAction(e -> {
            controller.dispose();
            onBack.run();
        });

        controller.initialize();
    }
}
