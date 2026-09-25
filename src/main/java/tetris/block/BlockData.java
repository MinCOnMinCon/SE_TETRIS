package src.main.java.tetris.block;

import java.awt.Color;

/*
    BlockData 클래스는 테트리스 블럭의 모양과 색상 정보, 위치를 저장
    BlockData(int[][], ColorPalette) 각 블럭(blocks 파일)에서 모양과 색상 정보를 설정
    GetShape() 현 shape 로드
    SetShape(int[][]) 현 shape 변경
    GetCurrentColor(int) 현 색상 모드에 따른 색상 로드
        색 설정은 각 블럭(blocks 파일)에서 모든 모드의 색 설정
    GetX(), SetX(), GetY(), SetY() 메서드를 통해 블록의 위치를 가져오고 설정
*/

public class BlockData {
    private int[][] shape;
    private ColorPalette colorPalette;
    private int x;
    private int y;

    public BlockData(int[][] shape, ColorPalette colorPalette) {
        this.shape = shape;
        this.colorPalette = colorPalette;
        // 보드 상단 중앙 부근에서 생성되도록 초기 위치 설정 (보드가 10칸이므로)
        this.x = 3; 
        this.y = 0;
    }

    public int[][] GetShape() {
        return this.shape;
    }

    public void SetShape(int[][] shape) {
        this.shape = shape;
    }

    public Color GetCurrentColor(int colorMode) {
        return this.colorPalette.GetColor(colorMode);
    }

    public int GetX() { return this.x; }
    public void SetX(int x) { this.x = x; }
    
    public int GetY() { return this.y; }
    public void SetY(int y) { this.y = y; }
}