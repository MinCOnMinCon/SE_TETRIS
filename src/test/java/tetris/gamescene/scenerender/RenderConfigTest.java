package tetris.gamescene.scenerender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javafx.scene.paint.Color;

import tetris.settings.GameSettings;

class RenderConfigTest {
    // 기대값은 RenderConfig에서 계산하지 않고 해상도별 숫자로 명시한다.
    // 큐는 실제 게임과 동일하게 3개를 표시한다.
    @ParameterizedTest(name = "{0}x{1} 해상도의 렌더링 값")
    @CsvSource({
        "1280, 720,  22, 16, 12, 40, 24, 112, 128, 420, 164, 144, 20, 18",
        "1920, 1080, 33, 24, 18, 60, 36, 168, 192, 630, 246, 216, 30, 27",
        "2560, 1440, 44, 32, 24, 80, 48, 224, 256, 840, 328, 288, 40, 36"
    })
    void matchesExpectedValuesForEachResolution(
            int width, int height, double blockSide, double gap, double padding,
            double titleHeight, double previewSide, double slotHeight, double panelWidth,
            double queueHeight, double holdingHeight, double scoreHeight,
            double scoreFontSize, double titleFontSize) {
        GameSettings settings = new GameSettings(
                width + "x" + height, width, height, 2,
                java.util.Map.of(), GameSettings.Difficulty.NORMAL);
        RenderConfig config = new RenderConfig(settings, 3);

        assertAll("화면과 설정",
                () -> assertEquals(width, config.gameSceneWidth, "gameSceneWidth"),
                () -> assertEquals(height, config.gameSceneHeight, "gameSceneHeight"),
                () -> assertEquals(3, config.blockQueueDisplayCount, "blockQueueDisplayCount"),
                () -> assertEquals(2, config.colorMode, "colorMode"));

        assertAll("블록, 간격과 여백",
                () -> assertEquals(blockSide, config.blockSide, "blockSide"),
                () -> assertEquals(gap, config.scenePadding, "scenePadding"),
                () -> assertEquals(gap, config.sidePanelGap, "sidePanelGap"),
                () -> assertEquals(gap, config.canvasVGap, "canvasVGap"),
                () -> assertEquals(gap, config.previewSlotGap, "previewSlotGap"),
                () -> assertEquals(padding, config.panelTitleHPadding, "panelTitleHPadding"),
                () -> assertEquals(padding, config.blockPreviewPadding, "blockPreviewPadding"),
                () -> assertEquals(padding, config.panelBottomPadding, "panelBottomPadding"),
                () -> assertEquals(padding, config.scoreHPadding, "scoreHPadding"));

        assertAll("패널 크기와 글자",
                () -> assertEquals(titleHeight, config.panelTitleHeight, "panelTitleHeight"),
                () -> assertEquals(previewSide, config.previewBlockSide, "previewBlockSide"),
                () -> assertEquals(slotHeight, config.previewSlotHeight, "previewSlotHeight"),
                () -> assertEquals(panelWidth, config.blockQueueCanvasWidth, "blockQueueCanvasWidth"),
                () -> assertEquals(queueHeight, config.blockQueueCanvasHeight, "blockQueueCanvasHeight"),
                () -> assertEquals(panelWidth, config.blockHoldingCanvasWidth, "blockHoldingCanvasWidth"),
                () -> assertEquals(holdingHeight, config.blockHoldingCanvasHeight, "blockHoldingCanvasHeight"),
                () -> assertEquals(panelWidth, config.scoreCanvasWidth, "scoreCanvasWidth"),
                () -> assertEquals(scoreHeight, config.scoreCanvasHeight, "scoreCanvasHeight"),
                () -> assertEquals(scoreFontSize, config.scoreFontSize, "scoreFontSize"),
                () -> assertEquals(titleFontSize, config.titleFontSize, "titleFontSize"));

        assertAll("배율과 관계없이 유지되는 색상",
                () -> assertEquals(Color.BLACK, config.sceneColor, "sceneColor"),
                () -> assertEquals(Color.rgb(24, 24, 24), config.panelColor, "panelColor"),
                () -> assertEquals(Color.WHITE, config.textColor, "textColor"),
                () -> assertEquals(Color.WHITE, config.borderColor, "borderColor"));
    }

    @Test
    void calculatesQueueCanvasHeightFromSlotCount() {
        GameSettings settings = new GameSettings(
                "1280x720", 1280, 720, 0, java.util.Map.of(), GameSettings.Difficulty.NORMAL);
        RenderConfig config1 = new RenderConfig(settings, 2);
        

        assertEquals(2, config1.blockQueueDisplayCount);
       
    }
}
