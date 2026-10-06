package tetris.gamescene;
import javafx.scene.paint.Color;
import tetris.settings.GameSettings; // 저장된 게임 해상도와 색상 모드
public class RenderConfig{
    public double gameSceneWidth = 800; // 게임 씬 전체의 너비(px)
    public double gameSceneHeight = 1000; // 게임 씬 전체의 높이(px)
    public double blockSide = 22; // 게임 보드 한 칸의 가로·세로 크기(px)
    

    public double scenePadding = 16; // 씬 가장자리와 내부 콘텐츠 사이의 상하좌우 여백(px)
    public double sidePanelGap = 16; // 중앙 보드와 좌우 VBox 사이의 가로 간격(px)
    public double canvasVGap = 16; // 각 VBox 안에 배치된 캔버스 사이의 세로 간격(px)

    public double panelTitleHPadding = 12; // 패널 제목의 좌우 여백(px)
    public double blockPreviewPadding = 12; // 블럭 미리보기 영역 내부의 상하좌우 여백(px)
    public double panelBottomPadding = 12; // 큐·홀딩·점수판 내용 영역의 하단 여백(px)
    public double scoreHPadding = 12; // 점수 숫자의 좌우 여백(px)
    public double panelTitleHeight = 40; // NEXT·HOLD·SCORE 제목에 할당하는 상단 영역의 높이(px)

    public double previewBlockSide = 24; // 큐·홀딩 미리보기 한 칸의 기본 크기(px), 공간이 부족하면 축소
    public double previewSlotHeight = 112; // 다음 블럭 하나를 표시하는 슬롯의 높이 및 홀딩 기본 영역 높이(px)
    public double previewSlotGap = 16; // 큐 캔버스 안에서 블럭 미리보기 슬롯 사이의 세로 간격(px)
    

    public int blockQueueDisplayCount;
    public double blockQueueCanvasWidth = 128; // 다음 블럭 큐 캔버스의 너비(px)
    // 다음 블럭 큐 캔버스의 높이(px): 제목 + 하단 여백 + 슬롯 개수만큼의 높이 + 슬롯 사이 간격
    public double blockQueueCanvasHeight = panelTitleHeight + panelBottomPadding
            + blockQueueDisplayCount * previewSlotHeight
            + (blockQueueDisplayCount - 1) * previewSlotGap;

    public double blockHoldingCanvasWidth = 128; // 홀딩 캔버스의 너비(px)
    public double blockHoldingCanvasHeight = panelTitleHeight + previewSlotHeight + panelBottomPadding; // 홀딩 캔버스의 높이(px): 제목 + 미리보기 영역 + 하단 여백

    public double scoreCanvasWidth = 128; // 점수판 캔버스의 너비(px)
    public double scoreCanvasHeight = 144; // 점수판 캔버스의 높이(px)
    public double scoreFontSize = 20; // 점수 숫자의 글자 크기

    public double titleFontSize = 18; // NEXT·HOLD·SCORE 제목의 글자 크기
   
    public int colorMode = 0; // 큐·홀딩 블럭의 색상 모드: 0 기본, 1 적록 색각 모드, 2 청황 색각 모드 (조정 필요)
    public Color sceneColor = Color.BLACK; // 게임 씬과 BorderPane의 배경색
    public Color panelColor = Color.rgb(24, 24, 24); // 큐·홀딩·점수판 캔버스의 배경색
    public Color textColor = Color.WHITE; // 패널 제목과 점수 숫자의 색
    public Color borderColor = Color.WHITE; // 보드 칸·미리보기 블럭·패널 테두리의 색
    

    public RenderConfig(){
        this(3);
    }
    public RenderConfig(int queueDisplayCount){
        blockQueueDisplayCount = queueDisplayCount;
    }

    // 저장된 사용자 설정을 렌더링 설정에 반영
    public RenderConfig(GameSettings settings) {
        this();
        gameSceneWidth = settings.screenWidth(); // 저장된 화면 너비 적용
        gameSceneHeight = settings.screenHeight(); // 저장된 화면 높이 적용
        colorMode = settings.colorMode(); // 블록 미리보기에 사용할 색상 모드 적용
    }
}

/*
해당 데이터 클래스는 SceneRenderer에서 화면을 렌더링하기 위한
기본적인 설정 정보를 나타낸다. 씬 크기가 얼마나 될지, 게임 보드, 점수판, 다음 나올 블럭등의 배치가
어떻게 되고 크기는 어떻게 될지 그런 정보를 나타낸다.
*/
