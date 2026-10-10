package tetris.gameoverscene;

import tetris.gamescene.score.GameScore;
import tetris.scoreboard.*;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameOverRoot extends StackPane {
    final int MAX_LENGTH = 15;  // 최대 15글자까지만 허용
    final int RANKER = 10;      // 표시 최대 순위

    private BorderPane baseScreen;  // 기본 화면
    private Label titleLabel;       // GAME OVER
    private Label scoreLabel;       // SCORE
    private TextField nameField;    // 입력 이름
    private Button startMenuButton; // 시작화면 호출

    private VBox confirmPopup;      // 확인창
    private Label confirmMessage;   // 이름 확인
    private HBox confirmButtonBox;  // 확인 버튼 (Y/N)
    private Button yesButton;
    private Button noButton;

    // 1. 생성자: 외부에서 넘겨준 데이터를 받아서 화면을 세팅함



    // ****TODO: 점수 타입 통합 필요****



    public GameOverRoot(GameScore Score, ScoreStorage scoreStorage) {
        int finalScore = (int) Score.GetGameScore();
        // 1. 노드 생성 및 스타일 설정 (디자인)
        setNodes(finalScore);

        // 2. 이벤트(동작) 연결 (로직)
        bindEvents(finalScore, scoreStorage);

        // 3. 화면 배치 및 조립 (레이아웃)
        setScene(finalScore);
    }

    private void setNodes(int finalScore) {
        baseScreen = new BorderPane();
        baseScreen.setStyle("-fx-background-color: black;");
        
        // 1. 텍스트 추가 (Label)
        titleLabel = new Label("GAME OVER");
        titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: red;");
        scoreLabel = new Label("최종 점수: " + finalScore);
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        // 2. 텍스트 입력받기 (TextField)
        nameField = new TextField();
        nameField.setPromptText("이름을 입력하세요 (Enter)"); // 입력창에 연하게 표시되는 힌트
        nameField.setMaxWidth(200); // 입력창이 화면 끝까지 늘어나지 않게 가로 길이 제한
        nameField.setStyle("-fx-font-size: 16px;");

        // 3. 시작화면 버튼 추가 (Button)
        startMenuButton = new Button("시작화면");
        startMenuButton.setPrefSize(180, 50); // 시작 화면에서 쓰셨던 버튼 크기와 통일
        startMenuButton.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black; -fx-font-size: 16px;");


        // 4. 입력 이름 확인 창 추가 (VBox)
        confirmPopup = new VBox(20);
        confirmPopup.setAlignment(Pos.CENTER);
        // 배경을 반투명한 검은색(rgba)으로 설정해 뒷배경이 어둡게 비치도록 효과 부여
        confirmPopup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); 
        confirmPopup.setVisible(false); // 처음에는 투명하게 숨겨둠

        confirmMessage = new Label();
        confirmMessage.setStyle("-fx-font-size: 24px; -fx-text-fill: yellow; -fx-font-weight: bold;");

        yesButton = new Button("등록");
        noButton = new Button("다시 입력");
        
        // 버튼 2개를 가로로 나란히 배치하기 위해 HBox 사용
        confirmButtonBox = new HBox(20, yesButton, noButton);
        confirmButtonBox.setAlignment(Pos.CENTER);
    }

    private void bindEvents(int finalScore, ScoreStorage scoreStorage) {
        // 텍스트 입력받기 (TextField) 입력 수 제한
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue.length() > MAX_LENGTH) {
            nameField.setText(oldValue); // 15글자를 초과하면 방금 친 글자를 무시하고 이전 상태로 롤백
            }
        });
        
        // 입력한 텍스트 꺼내오기 (이벤트)
        // TextField에서 엔터(Enter)키를 누르면 setOnAction이 자동으로 실행됩니다.
        nameField.setOnAction(e -> {
            // getText() 함수로 현재 입력된 문자열을 가져옵니다.
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty())
                playerName = "익명";
            else playerName = nameField.getText();
            
            
            // ****TODO: 앞/뒤 공백 포함 이름 처리****


 

            confirmMessage.setText("[" + playerName + "] 님으로 등록하시겠습니까?");
            confirmPopup.setVisible(true);
            baseScreen.setDisable(true);
        });

        // 팝업 [등록] 버튼 클릭 시 -> 실제 저장 로직
        yesButton.setOnAction(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty())
                playerName = "익명";
            else playerName = nameField.getText();

            scoreStorage.saveScore(playerName, finalScore);
            System.out.println("플레이어: " + playerName + " / 획득 점수: " + finalScore + " 저장 완료");

            confirmPopup.setVisible(false);
            baseScreen.setDisable(false);
            nameField.setDisable(true); // 중복 등록 방지
        });
        // 팝업 [다시 입력] 버튼 클릭 시 -> 팝업 닫고 입력창 복귀
        noButton.setOnAction(e -> {
            confirmPopup.setVisible(false);
            baseScreen.setDisable(false);
            nameField.requestFocus();
        });
        
        // 버튼 클릭 시 루트 시작화면으로 변경 이벤트
        startMenuButton.setOnAction(e -> {
            // 버튼 자신이 올라가 있는 현재 Scene을 알아냅니다.
            Scene currentScene = startMenuButton.getScene();
            




            // TODO: 시작 화면 Root 불러오기
            // currentScene.setRoot(new StartMenuRoot());
            




            System.out.println("시작 화면으로 돌아갑니다!");
        });
    }

    private void setScene(int finalScore) {
        VBox baseVbox = new VBox(20); // 부품 사이 간격 20
        baseVbox.setAlignment(Pos.CENTER);

        // 1. 텍스트 추가 (Label)
        baseVbox.getChildren().addAll(titleLabel, scoreLabel);

        // 2. 상위 점수 이름 입력받기 (TextField)
        int ranking = 0;
        ScoreLoader SL = new ScoreLoader();

        

        // ****TODO: ScoreLoader NULL값 확인 필요****



        for (ScoreRecord scoreRecord : SL.loadScores()){
            int score = scoreRecord.getScore();
            ranking += (finalScore > score) ? 0 : 1;
        }
        // 상위 점수 저장
        if(ranking < RANKER) {
            baseVbox.getChildren().addAll(nameField);
        }

        // 3. 시작화면 버튼 추가 (Button)
        baseVbox.getChildren().addAll(startMenuButton);
        baseScreen.setCenter(baseVbox);


        // 4. 입력 이름 확인 창 추가 (VBox)
        confirmPopup.getChildren().addAll(confirmMessage, confirmButtonBox);

        // 최종 조립: StackPane에 바탕화면과 팝업을 겹쳐서 올림
        this.getChildren().addAll(baseScreen, confirmPopup);
    }

    // 2. 호출 함수: 데이터 전달 + 화면 전환
    public static void GetEndScene(Scene scene, GameScore finalScore, ScoreStorage scoreStorage) {
        // 데이터를 듬뿍 담아서 새로운 게임 오버 뼈대를 하나 찍어냄
        GameOverRoot gameOverRoot = new GameOverRoot(finalScore, scoreStorage);
        
        // 넘겨받은 씬(Scene)의 뼈대를 방금 만든 게임 오버 뼈대로 갈아 끼움
        scene.setRoot(gameOverRoot);
    }
}