package tetris.scoreboard;

import javafx.collections.FXCollections; // JavaFX의 ObservableList를 쉽게 다루기 위한 클래스
import javafx.geometry.Insets; // 컴포넌트 여백 설정
import javafx.geometry.Pos; // 정렬 위치
import javafx.scene.Scene; // 화면을 구성하는 Scene 객체
import javafx.scene.control.Button; // 뒤로가기 버튼
import javafx.scene.control.Label; // 텍스트 라벨
import javafx.scene.control.TableColumn; // 표의 열
import javafx.scene.control.TableView; // 표 전체
import javafx.scene.control.cell.PropertyValueFactory; // 객체 속성을 표 컬럼과 연결하는 도구
import javafx.scene.layout.BorderPane; // 전체 화면 배치
import javafx.scene.layout.VBox; // 세로로 컴포넌트를 배치하는 레이아웃
import javafx.stage.Stage; // 창 객체
import tetris.StartScreen; // 메인 화면으로 돌아가기 위해 필요

import java.util.Comparator; // 정렬을 위해 사용하는 클래스
import java.util.List; // 리스트 자료형

// 점수판 화면을 생성하고 표시하는 클래스
public class ScoreBoardWindow {
    public void show(Stage stage) { // 현재 창을 점수판 화면으로 전환
        // 화면 전환 전의 창과 장면 크기를 저장
        double windowWidth = stage.getWidth();
        double windowHeight = stage.getHeight();
        double sceneWidth = stage.getScene().getWidth();
        double sceneHeight = stage.getScene().getHeight();

        // 저장된 점수 목록을 불러온 뒤, 점수 기준으로 내림차순 정렬
        List<ScoreRecord> scores = new ScoreLoader().loadScores();
        scores.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed());

        // 상위 3개만 추출해서 표에 표시
        List<ScoreRecord> top3 = scores.stream().limit(3).toList();

        // JavaFX 표를 생성
        TableView<ScoreRecord> table = new TableView<>();

        // 순위 컬럼 생성 및 값 연결
        TableColumn<ScoreRecord, Integer> rankColumn = new TableColumn<>("순위");
        rankColumn.setCellValueFactory(cellData -> {
            int index = top3.indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(index + 1).asObject();
        });

        // 이름 컬럼 생성 및 ScoreRecord의 name 필드와 연결
        TableColumn<ScoreRecord, String> nameColumn = new TableColumn<>("이름");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // 점수 컬럼 생성 및 ScoreRecord의 score 필드와 연결
        TableColumn<ScoreRecord, Integer> scoreColumn = new TableColumn<>("점수");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // 표에 3개의 컬럼 추가
        table.getColumns().addAll(rankColumn, nameColumn, scoreColumn); // 세 컬럼을 표에 추가

        // top3 데이터를 표에 넣기
        table.setItems(FXCollections.observableArrayList(top3));

        // 제목 라벨 생성 및 스타일 지정
        Label title = new Label("Top 3 Scores");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // 뒤로가기 버튼 생성
        Button backButton = new Button("뒤로가기");
        backButton.setPrefSize(180, 50);
        backButton.setOnAction(e -> {
            new StartScreen().start(stage);
        });

        // 제목과 표를 세로로 배치하는 루트 컨테이너
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(title, table, backButton);

        // 전체 화면을 감싸는 BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        root.setCenter(content);

        // 스코어 보드 화면 타이틀, 화면 전환
        //화면 사이즈는 기존의 크기를 유지
        stage.setTitle("Top 3 Scores");
        stage.setScene(new Scene(root, sceneWidth, sceneHeight)); // 기존 장면 크기로 새 화면 생성
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.show();
    }
}
