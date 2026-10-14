package tetris.gameoverscene;

import tetris.gamescene.GameResult;
import tetris.scoreboard.*;


import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView; // 표 전체



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

    private VBox scoreBoard;        // scoreBoard
    private Button backButton;
    private TableView<ScoreRecord> oldScore;
    private HBox newScore;
    private Label myRank; 
    private Label myName;
    private Label myScore;

    // 1. 생성자: 외부에서 넘겨준 데이터를 받아서 화면을 세팅함



    // ****TODO: 스코어보드 table 받아서 현재 (이름, 점수) 추가 후 표시 구현****
    // 스코어보드 팝업 추가 (뒤로가기 버튼도)



    public GameOverRoot(GameResult result, ScoreBoardScreen oldScoreBoard) {
        int finalScore =  result.score();
        
        if (oldScoreBoard==null)
            oldScoreBoard = new ScoreBoardScreen(null);

        // 1. 노드 생성 및 스타일 설정 (디자인)
        setNodes(finalScore);

        // 2. 이벤트(동작) 연결 (로직)
        bindEvents(finalScore, oldScoreBoard);

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



        // 5. 스코어 보드 추가 (VBox)
        scoreBoard = new VBox(20);
        scoreBoard.setAlignment(Pos.CENTER);
        scoreBoard.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); 
        scoreBoard.setVisible(false); // 처음에는 투명하게 숨겨둠

        oldScore = new TableView<ScoreRecord>();

        
        // 2. 내 등수표 생성
        newScore = new HBox();
        newScore.setAlignment(Pos.CENTER);
        newScore.setStyle("-fx-background-color: #333333; -fx-padding: 10px;"); // 위쪽 표와 구분되도록 약간의 배경색/여백 추가
        myRank = new Label(); 
        myName = new Label();
        myScore = new Label();
        myRank.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        myName.setStyle("-fx-text-fill: white;");
        myScore.setStyle("-fx-text-fill: white;");


        // 🌟 핵심 팁: 위쪽 TableView의 컬럼 너비와 동일하게 강제 고정!

        // 화면 비율 맞는지 확인

        // (표의 컬럼 너비를 50, 100, 100으로 설정했다고 가정)


        myRank.setPrefWidth(50);
        myName.setPrefWidth(100);
        myScore.setPrefWidth(100);
        // 중앙 정렬 (선택 사항)
        myRank.setAlignment(Pos.CENTER);
        myName.setAlignment(Pos.CENTER);
        myScore.setAlignment(Pos.CENTER);
        // HBox에 조립
        newScore.getChildren().addAll(myRank, myName, myScore);

        // 3. 뒤로가기 버튼 생성
        backButton = new Button("뒤로가기");

        scoreBoard.getChildren().addAll(oldScore, newScore, backButton);
    }

    private void bindEvents(int finalScore, ScoreBoardScreen oldScoreBoard) {
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

            confirmMessage.setText("[" + playerName + "] 님으로 등록하시겠습니까?");
            confirmPopup.setVisible(true);
            baseScreen.setDisable(true);
        });

        // 팝업 [등록] 버튼 클릭 시 -> 실제 저장, 스코어 보드 로드 로직
        yesButton.setOnAction(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty())
                playerName = "익명";
            else playerName = nameField.getText();

            ScoreStorage scoreStorage = new ScoreStorage();
            scoreStorage.saveScore(playerName, finalScore);
            System.out.println("플레이어: " + playerName + " / 획득 점수: " + finalScore + " 저장 완료");


            // 1. 이전 스코어 보드 로드 (상위 n등)
            oldScore = oldScoreBoard.makeSB();

            confirmPopup.setVisible(false);
            nameField.setDisable(true); // 중복 등록 방지


            // 스코어보드
            // 1. 전체 점수 리스트(scores)에서 내 순위 찾기
            int myRankCount = 1;
            List<ScoreRecord> scores = new ScoreLoader().loadScores();
            for (ScoreRecord tmp : scores) {
                if (tmp.getName().equals(nameField.getText()) && tmp.getScore() == finalScore) {
                    break; // 내 기록을 찾으면 카운트 멈춤
                }
                myRankCount++;
            }
            myRank.setText(myRankCount + "위");
            myName.setText(playerName);
            myScore.setText(finalScore + "점");

            
            scoreBoard.setVisible(true);
            System.out.println(myRank.getText() + myName.getText() + myScore.getText());

            
        });
        // 팝업 [다시 입력] 버튼 클릭 시 -> 팝업 닫고 입력창 복귀
        noButton.setOnAction(e -> {
            confirmPopup.setVisible(false);
            baseScreen.setDisable(false);
            nameField.requestFocus();
        });

        // 스코어 보드 [뒤로가기] 버튼 클릭 시 -> 스코어 보드 닫고 게임 오버창 복귀
        backButton.setOnAction(e -> {
            scoreBoard.setVisible(false);
            baseScreen.setDisable(false);
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

        // 점수 저장
        baseVbox.getChildren().addAll(nameField);
        

        // 3. 시작화면 버튼 추가 (Button)
        baseVbox.getChildren().addAll(startMenuButton);
        baseScreen.setCenter(baseVbox);


        // 4. 입력 이름 확인 창 추가 (VBox)
        confirmPopup.getChildren().addAll(confirmMessage, confirmButtonBox);

        // 최종 조립: StackPane에 바탕화면과 팝업을 겹쳐서 올림
        this.getChildren().addAll(baseScreen, confirmPopup, scoreBoard);
    }

    
}