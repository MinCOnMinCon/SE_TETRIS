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
import tetris.StartScreen; // 메인 화면으로 돌아가기 위해 필요
import tetris.settings.KeyBindingSettings.ActionType; // 키 역할 목록

// 설정 화면을 보여주는 클래스
public class SettingsScreen {
    public void show(Stage stage) { // 설정 화면을 구성하고 현재 Stage에 표시
        // 화면 제목 라벨
        Label title = new Label("설정");
        //글자의 폰트와 크기
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        /* 설명 문구(추가할지 말지는 회의에서 결정->설정 text와 겹치는 뜻의 내용이라)
        Label description = new Label("게임 환경 설정 화면입니다.");
        description.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        */

        // 해상도 선택 드롭다운 생성
        ComboBox<String> resolutionComboBox = new ComboBox<>();//새로운 드롭다운 객체 생성
        resolutionComboBox.setPromptText("화면 해상도 선택");//화면 사이즈 드롭다운 설명
        resolutionComboBox.setPrefWidth(220);//드롭다운의 크기를 220픽셀로 설정

        // 일반, 적록색맹, 청황색맹 모드 선택 상자
        ComboBox<SettingsConstants.ColorBlindMode> colorBlindModeComboBox = new ComboBox<>();//새로운 드롭다운 객체 생성
        colorBlindModeComboBox.setPromptText("색상 모드 선택");//색맹 모드 드롭다운 설명
        colorBlindModeComboBox.setPrefWidth(220);//드롭다운의 크기를 220픽셀로 설정

        // 키 설정 항목을 담을 세로 목록
        Label keySettingsTitle = new Label("키 설정");
        keySettingsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label keySettingsStatus = new Label("역할 버튼을 누른 뒤 새 키를 입력하세요.");
        keySettingsStatus.setStyle("-fx-text-fill: white;");
        VBox keyBindingRows = new VBox(8);

        // 역할을 왼쪽, 현재 배정된 키를 오른쪽에 표시
        for (ActionType actionType : ActionType.values()) { // 모든 키 역할을 한 줄씩 생성
            Label actionLabel = new Label(actionType.getDisplayName());
            actionLabel.setStyle("-fx-text-fill: white;");
            actionLabel.setPrefWidth(190);

            Button keyButton = new Button();
            keyButton.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(keyButton, Priority.ALWAYS);

            HBox row = new HBox(12, actionLabel, keyButton);
            row.setAlignment(Pos.CENTER_LEFT);
            keyBindingRows.getChildren().add(row);
        }

        // 뒤로가기 버튼
        Button backButton = new Button("뒤로가기");
        backButton.setPrefSize(180, 50);
        backButton.setOnAction(e -> { // 뒤로가기 버튼 클릭 처리
            new StartScreen().start(stage);
        });

        // 해상도, 색상 모드, 키 배정을 한 번에 저장하는 버튼
        Button saveSettingsButton = new Button("설정 저장");
        saveSettingsButton.setPrefSize(180, 44);

        // 설정 항목을 세로로 묶고 스크롤할 수 있게 구성
        VBox content = new VBox(14, resolutionComboBox, colorBlindModeComboBox,
            keySettingsTitle, keySettingsStatus, keyBindingRows, saveSettingsButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(content); // 긴 설정 목록을 스크롤 가능하게 구성
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black; -fx-background-color: black;");

        // 설정 화면의 전체 배치 구조
        BorderPane root = new BorderPane();//보더 페인으로 설정
        root.setStyle("-fx-background-color: black;");//배경을 검은색으로 설정
        root.setTop(title);//위에서 선언한 title을 위쪽에 배치
        BorderPane.setAlignment(title, Pos.CENTER);//배치할 때 중앙에 배치
        root.setCenter(scrollPane);//스크롤 가능한 설정 내용을 중앙에 배치
        root.setBottom(backButton);//뒤로가기 버튼은 화면 아래에 고정
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(12));

        // 화면 전환을 수행하기 전에 설정 컨트롤 객체를 연결
        Scene scene = new Scene(root, 400, 600); // 설정 화면 장면 생성
        SettingsController controller = new SettingsController(stage, scene, resolutionComboBox,
            colorBlindModeComboBox, SettingsConstants.getInputSettingData(), keyBindingRows,
            keySettingsStatus, saveSettingsButton);

        // 최종 화면 생성
        stage.setTitle("Tetris - 설정");
        stage.setScene(scene);
        controller.initialize();
        stage.show();
    }
}
