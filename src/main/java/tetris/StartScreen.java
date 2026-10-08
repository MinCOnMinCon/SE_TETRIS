package tetris;

import javafx.geometry.Pos; // 화면 요소의 정렬 위치
import javafx.scene.Parent; // 시작 메뉴 루트
import javafx.scene.Scene; // 화면에 표시할 장면
import javafx.scene.control.Button; // 버튼 컨트롤
import javafx.scene.control.Label; // 제목과 문구를 표시
import javafx.scene.input.KeyCode; // 키보드 키 종류
import javafx.scene.layout.BorderPane; // 화면을 상·중·하로 나누는 레이아웃
import javafx.scene.layout.VBox; // 버튼을 세로로 배치하는 레이아웃
import javafx.stage.Stage; // 실제 앱 창

// 테트리스의 시작 메뉴 화면. Scene과 Stage를 받아 메뉴 루트를 만든다.
public class StartScreen {
    private final Stage stage;
    private final Scene scene;
    private Button[] buttons;//버튼 배열 생성
    private int selectedIndex = 0;//선택된 버튼의 인덱스 초기화

    public StartScreen(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }

    private void updateSelection() {// 선택된 버튼과 일반 버튼의 색상 갱신
        for (int i = 0; i < buttons.length; i++) {//버튼 배열의 길이만큼 반복
            if (i == selectedIndex) {//선택된 버튼이면 푸른색으로 변경
                buttons[i].setStyle("-fx-background-color: #4da3ff; -fx-text-fill: white;");
            } else {//선택되지 않은 버튼이면 회색으로 변경
                buttons[i].setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black;");
            }
        }
    }

    public Parent createRoot(Runnable onGame, Runnable onScoreBoard, Runnable onSettings) {// 메뉴 루트를 만들고 화면 전환은 콜백에 맡긴다
        selectedIndex = 0;
        Label title = new Label("TETRIS");//타이틀 생성
        //타이틀 스타일 설정(폰트, 굵기, 색상)
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        //버튼 생성
        Button startButton = new Button("게임시작");
        Button rankingButton = new Button("스코어 보드");
        Button settingButton = new Button("설정");
        Button exitButton = new Button("게임 종료");

        //버튼 배열 초기화
        buttons = new Button[] {
            startButton, rankingButton, settingButton, exitButton
        };

        //버튼 크기 설정
        for (Button button : buttons) {
            button.setPrefSize(180, 50);
        }

        //게임 시작 버튼 클릭 시 동작
        startButton.setOnAction(e -> onGame.run());

        //스코어 보드 버튼 클릭 시 동작
        rankingButton.setOnAction(e -> onScoreBoard.run());

        //설정 버튼 클릭 시 동작
        settingButton.setOnAction(e -> onSettings.run());

        //게임 종료 버튼 클릭 시 동작
        exitButton.setOnAction(e -> {
            System.exit(0);
        });

        //버튼에 마우스 오버 이벤트 설정
        for (int i = 0; i < buttons.length; i++) {//모든 버튼의 배열 회전
            final int index = i;//마우스 오버 이벤트 설정을 위한 인덱스 변수 설정
            buttons[i].setOnMouseEntered(e -> {
                selectedIndex = index;//선택된 버튼이면
                updateSelection();//선택된 버튼으로 설정
            });

            buttons[i].setFocusTraversable(false);
        }

        //버튼을 수직 배치, 버튼 간격을 20으로 설정 및 중앙 정렬
        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(startButton, rankingButton, settingButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();//전체 화면 구성
        root.setStyle("-fx-background-color: black;");//배경 검은색 설정
        root.setTop(title);//타이틀 상단 배치
        BorderPane.setAlignment(title, Pos.CENTER);//타이틀 중앙 정렬
        root.setCenter(buttonBox);//버튼 중앙 배치

        scene.setOnKeyPressed(e -> { // 씬 전체에서 키보드 입력을 감지
            if (e.getCode() == KeyCode.DOWN) {//아래 방향키 입력 시
                if (selectedIndex < buttons.length - 1) {//선택된 버튼이 마지막 버튼이 아니면
                    selectedIndex++;//선택된 버튼 인덱스 증가
                    updateSelection();
                }
            } else if (e.getCode() == KeyCode.UP) {//위 방향키 입력 시
                if (selectedIndex > 0) {//선택된 버튼이 첫 번째 버튼이 아니면
                    selectedIndex--;//선택된 버튼 인덱스 감소
                    updateSelection();
                }
            } else if (e.getCode() == KeyCode.ENTER) { //엔터키 입력 시
                buttons[selectedIndex].fire();//선택된 버튼 클릭 이벤트 발생
            }
        });

        updateSelection();//초기 선택된 버튼 업데이트
        stage.setTitle("Tetris");//창 이름 설정
        return root;
    }
}
