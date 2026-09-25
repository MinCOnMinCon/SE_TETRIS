package tetris.gameboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BoardRenderer extends Application {
    private BoardRenderData boardRenderData = new BoardRenderData();
    private Scene gameScene;
    private Canvas boardCanvas;
    private BorderPane sceneLayout;

    @Override
    public void start(Stage primaryStage){// 최초 게임 시작 시 화면 띄우는 역할. 
        GameBoard gameBoard = new GameBoard();
        createScene();
        UpdateScene(gameBoard.getBoard());

        primaryStage.setTitle("Tetris");
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }

    public void drawBoardCanvas(BoardElement[][] board){ //테트로미노가 있는 보드 캔버스를 그린다.
        double blockSide = boardRenderData.blockSide;
        boardCanvas = new Canvas(board[0].length * blockSide, board.length * blockSide);

        GraphicsContext graphicsContext = boardCanvas.getGraphicsContext2D();
        graphicsContext.setStroke(Color.WHITE);

        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                double x = col * blockSide;
                double y = row * blockSide;

                graphicsContext.setFill(board[row][col].getElementColor());
                graphicsContext.fillRect(x, y, blockSide, blockSide);
                graphicsContext.strokeRect(x, y, blockSide, blockSide);
            }
        }
    }

    public void createScene(){ // 게임 씬을 생성
        sceneLayout = new BorderPane();
        gameScene = new Scene(sceneLayout, boardRenderData.gameSceneWidth,
                boardRenderData.gameSceneHeight, Color.BLACK);
    }
    public void UpdateScene(BoardElement[][] board){ // 게임 씬 업데이트
        drawBoardCanvas(board);
        sceneLayout.setCenter(boardCanvas);
    }

    
}
