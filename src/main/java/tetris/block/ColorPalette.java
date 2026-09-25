package src.main.java.tetris.block;

import java.awt.Color;

/*
    ColorPalette 클래스는 블럭의 색상 정보를 색맹 모드에 따라 저장
    ColorPalette(Color, Color, Color) 각 색맹 모드에 따른 색상 정보를 설정
    GetColor(int) 색맹 모드에 따른 색상 정보를 반환
*/

public class ColorPalette {
    private Color normalColor;
    private Color redGreenBlindColor;
    private Color blueYellowBlindColor;

    public ColorPalette(Color normal, Color redGreen, Color blueYellow) {
        this.normalColor = normal;
        this.redGreenBlindColor = redGreen;
        this.blueYellowBlindColor = blueYellow;
    }

    public Color GetColor(int colorMode) {
        if (colorMode == 1) {
            return this.redGreenBlindColor;
        } else if (colorMode == 2) {
            return this.blueYellowBlindColor;
        }
        return this.normalColor;
    }
}