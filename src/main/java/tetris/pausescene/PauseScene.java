package tetris.pausescene;

import java.util.Objects;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// 일시정지 화면을 구성하고 버튼 동작은 전달받은 콜백에 위임한다.
public class PauseScene {
    private final VBox root;

    public PauseScene(Runnable onResume, Runnable onExit) {
        Objects.requireNonNull(onResume);
        Objects.requireNonNull(onExit);

        Label title = new Label("PAUSE");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");

        Button resumeButton = new Button("게임 재개");
        Button exitButton = new Button("게임 종료");
        for (Button button : new Button[] {resumeButton, exitButton}) {
            button.setPrefSize(200, 50);
            button.setStyle("-fx-background-color: #222222; -fx-text-fill: white; -fx-font-size: 20px;");
        }
        resumeButton.setOnAction(event -> onResume.run());
        exitButton.setOnAction(event -> onExit.run());

        root = new VBox(24, title, resumeButton, exitButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");
    }

    public Parent GetRoot() {
        return root;
    }
}
