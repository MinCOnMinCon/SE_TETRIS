package tetris.versusmode;

import java.util.Objects;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tetris.settings.GameSettings;

public class VersusRenderer {
    private final BorderPane root = new BorderPane(); // 두 플레이어의 게임 컨트롤러 화면이 들어갈 큰 화면
    private final Label timerLabel = new Label();
    private final StackPane timerPane = new StackPane(timerLabel);
    private final Label connectionStatusLabel = new Label();

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
        root.setStyle("-fx-background-color: black;");

        double timerHeight = height / 8;
        CreateTimerPane(width, timerHeight);
        double gameHeight = height - timerHeight;

        StackPane left = CreatePlayerPane(player1Root, width / 2, gameHeight);
        StackPane right = CreatePlayerPane(player2Root, width / 2, gameHeight);
        root.setCenter(new HBox(left, right));
    }

    private void CreateTimerPane(double width, double height) {
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setFont(Font.font(height * 0.35));
        timerPane.setMinSize(width, height);
        timerPane.setPrefSize(width, height);
        timerPane.setMaxSize(width, height);
        root.setTop(timerPane);
    }

    public void CreateConnectionStatusPane() {
        if (connectionStatusLabel.getParent() != null) {
            return;
        }

        double fontSize = timerPane.getPrefHeight() * 0.22;
        Font font = Font.font(fontSize);
        Label caption = new Label("연결 상태 :");
        caption.setFont(font);
        caption.setTextFill(Color.WHITE);
        connectionStatusLabel.setFont(font);
        connectionStatusLabel.setMinWidth(fontSize * 2.5);

        HBox statusPane = new HBox(6, caption, connectionStatusLabel);
        statusPane.setAlignment(Pos.CENTER_LEFT);
        statusPane.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        StackPane.setAlignment(statusPane, Pos.CENTER_RIGHT);
        StackPane.setMargin(statusPane, new Insets(0, fontSize, 0, 0));
        timerPane.getChildren().add(statusPane);
    }

    public void RenderConnectionStatus(Boolean delayed) {
        if(delayed != null) return;
        connectionStatusLabel.setText(delayed ? "지연" : "쾌적");
        connectionStatusLabel.setTextFill(delayed ? Color.RED : Color.LIMEGREEN);
    }

    public void RenderTime(double remainingTimeMs) {
        if (remainingTimeMs < 0) {
            throw new IllegalArgumentException("남은 시간은 0 이상의 유한한 값이어야 합니다.");
        }
        long totalSeconds = (long) Math.ceil(remainingTimeMs / 1000.0);
        timerLabel.setText(String.format("남은 시간 %02d:%02d",
                totalSeconds / 60, totalSeconds % 60));
    }

    public void RenderFinish() {
        timerLabel.setFont(Font.font("System", FontWeight.BOLD,
                root.getPrefHeight() / 8 * 0.6));
        timerLabel.setText("FINISH");
    }

    private StackPane CreatePlayerPane(Parent screen, double width, double height) {
        StackPane pane = new StackPane(new Group(screen));
        pane.setMinSize(width, height);
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        pane.setStyle("-fx-background-color: black;");

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

        double scale = Math.min(
                width / bounds.getWidth(),
                height / bounds.getHeight());
        screen.setScaleX(scale);
        screen.setScaleY(scale);
    }

    public Parent GetRoot() {
        return root;
    }
}
