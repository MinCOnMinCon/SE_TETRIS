package tetris.gameoverscene;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;



public class VersusOverRoot {
    private BorderPane baseScreen;  // 기본 화면
    private Label titleLabel;       // GAME OVER
    // score
    private HBox scoreBox;
    private VBox hostScoreBox;
    private Label hostName;
    private Label hostScore;
    private VBox clientScoreBox;
    private Label clientName;
    private Label clientScore;

    private Button startMenuButton; // 시작화면 호출

    // 1. 생성자: 외부에서 넘겨준 데이터를 받아서 화면을 세팅함

    public VersusOverRoot(VersusResult result, Runnable onBack) {
        
        // 1. 노드 생성 및 스타일 설정 (디자인)
        setNodes(result);

        // 2. 이벤트(동작) 연결 (로직)
        bindEvents(onBack);

        // 3. 화면 배치 및 조립 (레이아웃)
        setScene();
    }
 
    private void setNodes(VersusResult result) {
        baseScreen = new BorderPane();
        baseScreen.setStyle("-fx-background-color: black;");
        
        // 1. 텍스트 추가 (Label)
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: red;");
        switch (result.LocalWinLose()) {
            case 0:
                titleLabel.setText("GAME OVER");
                break;
            case 1:
                titleLabel.setText("WIN");
                titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: yellow;");
                break;
            case 2:
                titleLabel.setText("LOSE");
                break;
            default:
                break;
        }

        // 2. 점수 추가 (HBox, VBox)
        scoreBox = new HBox(40);

        hostScoreBox = new VBox();
        hostName = new Label(result.hostName());
        hostScore = new Label(result.hostScore() + "점");

        clientScoreBox = new VBox();
        clientName = new Label(result.clientName());
        clientScore = new Label(result.clientScore() + "점");

        hostName.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        hostScore.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        clientName.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        clientScore.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        // 3. 시작화면 버튼 추가 (Button)
        switch (result.LocalWinLose()) {
            case 0:
                startMenuButton = new Button("시작화면");
                break;
            default:
                startMenuButton = new Button("로비화면");
                break;
        }
        startMenuButton.setPrefSize(180, 50); // 시작 화면에서 쓰셨던 버튼 크기와 통일
        startMenuButton.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black; -fx-font-size: 16px;");
    }

        private void bindEvents(Runnable onBack) {
        // 버튼 클릭 시 루트 시작화면으로 변경 이벤트
        startMenuButton.setOnAction(e -> onBack.run());
    }

    private void setScene() {
        VBox baseVbox = new VBox(20); // 부품 사이 간격 20
        baseVbox.setAlignment(Pos.CENTER);

        // 1. 텍스트 추가 (Label)
        baseVbox.getChildren().add(titleLabel);

        // 2. 점수 추가 (HBox, VBox)
        hostScoreBox.getChildren().addAll(hostName, hostScore);
        clientScoreBox.getChildren().addAll(clientName, clientScore);
        hostScoreBox.setAlignment(Pos.CENTER);
        clientScoreBox.setAlignment(Pos.CENTER);

        scoreBox.getChildren().addAll(hostScoreBox, clientScoreBox);
        scoreBox.setAlignment(Pos.CENTER);

        baseVbox.getChildren().add(scoreBox);
        

        // 3. 시작화면 버튼 추가 (Button)
        baseVbox.getChildren().addAll(startMenuButton);
        baseScreen.setCenter(baseVbox);
    }

    public Parent getRoot() {
        return baseScreen;
    }

    public void focusBackButton() {
        startMenuButton.requestFocus();
    }

    
}