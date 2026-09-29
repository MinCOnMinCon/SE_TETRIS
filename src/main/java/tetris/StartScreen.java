package tetris;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tetris.scoreboard.ScoreBoardWindow;
import tetris.settings.SettingsScreen;

public class StartScreen extends Application {
    private Button[] buttons;//버튼 배열 생성
    private int selectedIndex = 0;//선택된 버튼의 인덱스 초기화

    private void updateSelection() {//선택된 버튼의 스타일 업데이트 방식 선언
        for (int i = 0; i < buttons.length; i++) {//버튼 배열의 길이만큼 반복
            if (i == selectedIndex) {//선택된 버튼이면 푸른색으로 변경
                buttons[i].setStyle("-fx-background-color: #4da3ff; -fx-text-fill: white;");
            } else {//선택되지 않은 버튼이면 회색으로 변경
                buttons[i].setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black;");
            }
        }
    }

    @Override
    public void start(Stage stage) {//시작 화면을 구성하는 메서드
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
        startButton.setOnAction(e -> {
            System.out.println("게임 시작 버튼 누름");
        });

        //스코어 보드 버튼 클릭 시 동작
        rankingButton.setOnAction(e -> {
            new ScoreBoardWindow().show(stage);//스코어 보드 창 띄우기
        });

        //설정 버튼 클릭 시 동작
        settingButton.setOnAction(e -> {
            new SettingsScreen().show(stage);//설정창 화면 전환
        });

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

        Scene scene = new Scene(root, 400, 600);//화면 크기 설정
        scene.getRoot().requestFocus();//버튼으로 빠지는 focus를 scene 루트에 욺김
        scene.setOnKeyPressed(e -> {
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
        stage.setScene(scene);//scene으로 초기 화면 설정
        stage.setResizable(true);//창 크기 조절 가능
        stage.show();//창 띄우기
    }
}