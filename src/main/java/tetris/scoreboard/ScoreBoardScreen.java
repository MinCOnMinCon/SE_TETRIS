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
import tetris.settings.GameSettings;

// 점수판 화면을 생성하고 표시하는 클래스
public class ScoreBoardScreen {
    private BorderPane root;
    private TableView<ScoreRecord> table;
    private Button backButton;

    public ScoreBoardScreen(Runnable onBack) {
        makeSB();
        this.backButton.setOnAction(e -> onBack.run());
    }

    // 점수 표, 제목, 뒤로가기 버튼을 만들어 화면에 배치한다.
    public TableView<ScoreRecord> makeSB() {
        table = createScoreTableView();
        Label title = createTitleLabel();
        backButton = createBackButton();
        root = createRoot(title, table, backButton);
        return table;
    }

    private Label createTitleLabel() {
        Label title = new Label("Top 10 Scores");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        return title;
    }

    private Button createBackButton() {
        Button button = new Button("뒤로가기");
        button.setPrefSize(180, 50);
        button.setStyle("-fx-font-size: 16px;");
        return button;
    }

    private BorderPane createRoot(Label title, TableView<ScoreRecord> scoreTable, Button button) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(title, scoreTable, button);

        BorderPane screen = new BorderPane();
        screen.setStyle("-fx-background-color: black;");
        screen.setCenter(content);
        return screen;
    }

    public TableView<ScoreRecord> createScoreTableView() {
        TableView<ScoreRecord> scoreTable = createScoreTable();
        scoreTable.setItems(FXCollections.observableArrayList(loadTopScores()));
        return scoreTable;
    }

    private List<ScoreRecord> loadTopScores() {
        return new ScoreLoader().loadScores().stream().limit(10).toList();
    }

    private TableView<ScoreRecord> createScoreTable() {
        TableView<ScoreRecord> scoreTable = new TableView<>();
        scoreTable.setStyle("-fx-font-size: 15px;");
        scoreTable.getColumns().addAll(
                createRankColumn(scoreTable),
                createNameColumn(),
                createScoreColumn(),
                createDifficultyColumn(),
                createGameModeColumn());
        return scoreTable;
    }

    private TableColumn<ScoreRecord, Integer> createRankColumn(TableView<ScoreRecord> scoreTable) {
        TableColumn<ScoreRecord, Integer> rankColumn = new TableColumn<>("순위");
        rankColumn.setCellValueFactory(cellData -> {
            int index = scoreTable.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(index + 1).asObject();
        });
        return rankColumn;
    }

    private TableColumn<ScoreRecord, String> createNameColumn() {
        TableColumn<ScoreRecord, String> nameColumn = new TableColumn<>("이름");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        return nameColumn;
    }

    private TableColumn<ScoreRecord, Integer> createScoreColumn() {
        TableColumn<ScoreRecord, Integer> scoreColumn = new TableColumn<>("점수");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        return scoreColumn;
    }

    private TableColumn<ScoreRecord, GameSettings.Difficulty> createDifficultyColumn() {
        TableColumn<ScoreRecord, GameSettings.Difficulty> difficultyColumn = new TableColumn<>("난이도");
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        return difficultyColumn;
    }

    private TableColumn<ScoreRecord, GameSettings.GameMode> createGameModeColumn() {
        TableColumn<ScoreRecord, GameSettings.GameMode> gameModeColumn = new TableColumn<>("모드");
        gameModeColumn.setCellValueFactory(new PropertyValueFactory<>("gameMode"));
        return gameModeColumn;
    }

    public void refreshScores() {
        table.setItems(FXCollections.observableArrayList(loadTopScores()));
    }

    public BorderPane showSB() {
        return root;
    }

    public void focusBackButton() {
        backButton.requestFocus();
    }

    public TableView<ScoreRecord> getTable() {
        return table;
    }
}