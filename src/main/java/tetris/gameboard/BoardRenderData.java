package tetris.gameboard;
import javafx.scene.paint.Color;
public class BoardRenderData{
    final double gameSceneWidth;
    final double gameSceneHeight;
    final double blockSide;
    final Color sceneColor;

    public BoardRenderData(){
        gameSceneWidth = 800;
        gameSceneHeight = 1000;
        blockSide = 40;
        sceneColor = Color.BLACK;
    }
}