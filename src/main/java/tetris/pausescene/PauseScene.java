package tetris.pausescene;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// 일시정지 화면을 구성하고 버튼 동작은 전달받은 콜백에 위임한다.
public class PauseScene {
    private final VBox root;
    private final VBox messageBox;
    private final TextField chatInput;
    private final String chatName;
    private final Consumer<String> onChatSend;

    public PauseScene(Runnable onResume, Runnable onExit) {
        this(onResume, onExit, null, null);
    }

    public PauseScene(Runnable onResume, Runnable onExit, String chatName, Consumer<String> onChatSend) {
        Objects.requireNonNull(onResume);
        Objects.requireNonNull(onExit);

        this.chatName = chatName;
        this.onChatSend = onChatSend;
        boolean chatEnabled = chatName != null && onChatSend != null;

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

        VBox actions = new VBox(24, title, resumeButton, exitButton);
        actions.setAlignment(Pos.CENTER);

        if (chatEnabled) {
            messageBox = new VBox(4);
            messageBox.setAlignment(Pos.TOP_LEFT);
            messageBox.setFillWidth(true);

            ScrollPane history = new ScrollPane(messageBox);
            history.setFitToWidth(true);
            history.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            history.setStyle("-fx-background: #1a1a1a; -fx-background-color: transparent;");
            VBox.setVgrow(history, Priority.ALWAYS);

            chatInput = new TextField();
            chatInput.setPromptText("메시지를 입력하세요");
            chatInput.setStyle("-fx-font-size: 14px;");
            chatInput.setOnAction(event -> submitChat());

            VBox chat = new VBox(8, history, chatInput);
            chat.setPadding(new Insets(8));
            chat.setPrefWidth(320);
            chat.setPrefHeight(280);
            chat.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #dcdcdc;");

            HBox content = new HBox(40, actions, chat);
            content.setAlignment(Pos.CENTER);
            root = new VBox(content);
            root.setAlignment(Pos.CENTER);
        } else {
            messageBox = null;
            chatInput = null;
            root = new VBox(24, title, resumeButton, exitButton);
            root.setAlignment(Pos.CENTER);
        }
        root.setStyle("-fx-background-color: black;");
    }

    public Parent GetRoot() {
        return root;
    }

    public boolean isChatEnabled() {
        return chatInput != null;
    }

    public void addIncomingMessage(String message) {
        if (messageBox == null || message == null || message.isBlank()) {
            return;
        }
        Label line = new Label(message);
        line.setWrapText(true);
        line.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        messageBox.getChildren().add(line);
    }

    public void focusChatInput() {
        if (chatInput != null) {
            chatInput.requestFocus();
        }
    }

    private void submitChat() {
        if (chatInput == null || onChatSend == null || chatName == null) {
            return;
        }
        String text = chatInput.getText() == null ? "" : chatInput.getText().trim();
        chatInput.clear();
        if (text.isEmpty()) {
            return;
        }
        String line = chatName + ": " + text;
        addIncomingMessage(line);
        onChatSend.accept(line);
    }
}
