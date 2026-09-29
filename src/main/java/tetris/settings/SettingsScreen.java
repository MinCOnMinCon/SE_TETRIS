package tetris.settings;

import javafx.geometry.Insets; // 여백 설정
import javafx.geometry.Pos; // 정렬 위치
import javafx.scene.Scene; // JavaFX 화면 객체
import javafx.scene.control.Button; // 버튼 컨트롤
import javafx.scene.control.ComboBox; // 선택 상자
import javafx.scene.control.Label; // 텍스트 라벨
import javafx.scene.layout.BorderPane; // 전체 화면 배치 레이아웃
import javafx.scene.layout.VBox; // 세로 배치 레이아웃
import javafx.stage.Stage; // 창 객체
import tetris.StartScreen; // 메인 화면으로 돌아가기 위해 필요

// 설정 화면을 보여주는 클래스
public class SettingsScreen {
    public void show(Stage stage) {
        // 화면 제목 라벨
        Label title = new Label("설정");
        //글자의 폰트와 크기
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        // 설명 문구
        Label description = new Label("게임 환경 설정 화면입니다.");
        description.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        // 해상도 선택 드롭다운 생성
        ComboBox<String> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.setPromptText("화면 해상도 선택");
        resolutionComboBox.setPrefWidth(220);

        // 색맹 모드 토글 버튼
        Button colorBlindModeButton = new Button("색맹 모드: OFF");
        colorBlindModeButton.setPrefSize(220, 50);

        // 뒤로가기 버튼
        Button backButton = new Button("뒤로가기");
        backButton.setPrefSize(180, 50);
        backButton.setOnAction(e -> {
            new StartScreen().start(stage);
        });

        // 옵션들을 세로로 묶는 컨테이너
        VBox content = new VBox(20, description, resolutionComboBox, colorBlindModeButton, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        // 설정 화면의 전체 배치 구조
        BorderPane root = new BorderPane();//보더 페인으로 설정
        root.setStyle("-fx-background-color: black;");//배경을 검은색으로 설정
        root.setTop(title);//위에서 선언한 title을 위쪽에 배치
        BorderPane.setAlignment(title, Pos.CENTER);//배치할 때 중앙에 배치
        root.setCenter(content);//content를 레이아웃 중앙에 배치

        // 화면 전환을 수행하기 전에 설정 컨트롤 객체를 연결
        SettingsController controller = new SettingsController(stage, content, colorBlindModeButton, resolutionComboBox);
        controller.initialize();

        // 최종 화면 생성
        stage.setTitle("Tetris - 설정");
        stage.setScene(new Scene(root, 400, 600));
        stage.show();
    }
}
