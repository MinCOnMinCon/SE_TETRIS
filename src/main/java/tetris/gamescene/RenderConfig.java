package tetris.gamescene;
import javafx.scene.paint.Color;
public class RenderConfig{
    final double gameSceneWidth;
    final double gameSceneHeight;
    final double blockSide;
    final Color sceneColor;

    public RenderConfig(){
        gameSceneWidth = 800;
        gameSceneHeight = 1000;
        blockSide = 40;
        sceneColor = Color.BLACK;
    }
}

/*
해당 데이터 클래스는 SceneRenderer에서 화면을 렌더링하기 위한
기본적인 설정 정보를 나타낸다. 씬 크기가 얼마나 될지, 게임 보드, 점수판, 다음 나올 블럭등의 배치가
어떻게 되고 크기는 어떻게 될지 그런 정보를 나타낸다.
*/