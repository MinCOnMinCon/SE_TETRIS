package tetris.scoreboard;

import javafx.collections.FXCollections; // JavaFX의 ObservableList를 쉽게 다루기 위한 클래스
import javafx.geometry.Insets; // 컴포넌트 여백 설정
import javafx.geometry.Pos; // 정렬 위치
import javafx.scene.control.Button; // 뒤로가기 버튼
import javafx.scene.control.Label; // 텍스트 라벨
import javafx.scene.control.TableColumn; // 표의 열
import javafx.scene.control.TableView; // 표 전체
import javafx.scene.control.cell.PropertyValueFactory; // 객체 속성을 표 컬럼과 연결하는 도구
import javafx.scene.layout.BorderPane; // 전체 화면 배치
import javafx.scene.layout.VBox; // 세로로 컴포넌트를 배치하는 레이아웃
import java.util.List; // 리스트 자료형

// 점수판 화면을 생성하고 표시하는 클래스
public class ScoreBoardScreen {
    private BorderPane root;
    private TableView<ScoreRecord> table;

    //onBack.run()이 AppController의 ShowStartScreen()을 호출하도록 연결
    public void makeSB(Runnable onBack) {
        // 상위 10개만 추출해서 표에 표시
        List<ScoreRecord> top10 = new ScoreLoader().loadScores().stream().limit(10).toList();

        // JavaFX 표를 생성
        table = new TableView<>();

        // 순위 컬럼 생성 및 값 연결
        TableColumn<ScoreRecord, Integer> rankColumn = new TableColumn<>("순위");
        rankColumn.setCellValueFactory(cellData -> {
            int index = top10.indexOf(cellData.getValue());
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

        // top10 데이터를 표에 넣기
        table.setItems(FXCollections.observableArrayList(top10));

        // 제목 라벨 생성 및 스타일 지정
        Label title = new Label("Top 10 Scores");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // 뒤로가기 버튼 생성
        Button backButton = new Button("뒤로가기");

        backButton.setPrefSize(180, 50);
        backButton.setOnAction(e -> onBack.run());

        // 제목과 표를 세로로 배치하는 루트 컨테이너
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(title, table, backButton);

        // 전체 화면을 감싸는 BorderPane
        root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        root.setCenter(content);
    }

    public BorderPane showSB() {
        return root;
    }

    public TableView<ScoreRecord> getTable() {
        return table;
    }
}