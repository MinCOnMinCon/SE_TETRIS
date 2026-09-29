package tetris.gamescene;


import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import tetris.gamescene.board.*;

public class SceneRenderer  {
    private RenderConfig boardRenderData = new RenderConfig();
    private Scene gameScene;
    private Canvas boardCanvas;
    private BorderPane sceneLayout;

    

    public void createBoardCanvas(BoardElement[][] board){ // 보드 캔버스를 처음 생성하고 그린다.
        double blockSide = boardRenderData.blockSide;
        boardCanvas = new Canvas(board[0].length * blockSide, board.length * blockSide);

        updateBoardCanvas(board);
    }

    public void updateBoardCanvas(BoardElement[][] board){ // 기존 보드 캔버스를 업데이트한다.
        double blockSide = boardRenderData.blockSide;
        GraphicsContext graphicsContext = boardCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
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

    public void createScene(SceneRenderState state){ // 게임 씬을 생성

        sceneLayout = new BorderPane();
        gameScene = new Scene(sceneLayout, boardRenderData.gameSceneWidth,
                boardRenderData.gameSceneHeight, Color.BLACK);
        createBoardCanvas(state.board());
    }
    public void UpdateScene(SceneRenderState state){ // 게임 씬 업데이트
        
        updateBoardCanvas(state.board());
        
    }

    
}

// create함수는 빈 캔버스, 빈 씬 등을 최초로 생성만 함.
// 내용물은 Update 함수로 채워야 함.