package tetris.versusmode;

import java.util.Objects;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import tetris.settings.GameSettings;
import tetris.settings.GameSettings.GameMode;

public class VersusRenderer {
    private final BorderPane root = new BorderPane(); // 두 플레이어의 게임 컨트롤러 화면이 들어갈 큰 화면

    public VersusRenderer(Parent player1Root, Parent player2Root, GameSettings settings) {
        Objects.requireNonNull(player1Root);
        Objects.requireNonNull(player2Root);
        Objects.requireNonNull(settings);
        if (player1Root == player2Root) {
            throw new IllegalArgumentException("플레이어별로 서로 다른 화면이 필요합니다.");
        }

        double width = settings.screenWidth();
        double height = settings.screenHeight();
        root.setMinSize(width, height);
        root.setPrefSize(width, height);
        root.setMaxSize(width, height);

        double gameHeight = height;
        if (settings.gameMode() == GameMode.TIMEOUT) {
            double timerHeight = height / 8;
            CreateTimerPane(width, timerHeight);
            gameHeight -= timerHeight;
        }

        StackPane left = CreatePlayerPane(player1Root, width / 2, gameHeight);
        StackPane right = CreatePlayerPane(player2Root, width / 2, gameHeight);
        root.setCenter(new HBox(left, right));
    }

    private void CreateTimerPane(double width, double height) {
        Label timerLabel = new Label("남은 시간 --:--");
        StackPane timerPane = new StackPane(timerLabel);
        timerPane.setMinSize(width, height);
        timerPane.setPrefSize(width, height);
        timerPane.setMaxSize(width, height);
        root.setTop(timerPane);
    }

    private StackPane CreatePlayerPane(Parent screen, double width, double height) {
        StackPane pane = new StackPane(new Group(screen));
        pane.setMinSize(width, height);
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);

        screen.applyCss();
        screen.autosize();
        screen.layout();
        ResizeToFit(screen, width, height);

        return pane;
    }

    private void ResizeToFit(Parent screen, double width, double height) {
        Bounds bounds = screen.getLayoutBounds();
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0
                || width <= 0 || height <= 0) {
            return;
        }

        double scale = Math.min(1.0, Math.min(
                width / bounds.getWidth(),
                height / bounds.getHeight()));
        screen.setScaleX(scale);
        screen.setScaleY(scale);
    }

    public Parent GetRoot() {
        return root;
    }
}
