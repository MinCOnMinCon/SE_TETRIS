package tetris.block.data;

import javafx.scene.paint.Color;
import tetris.block.data.CurrentBlock.ItemType;

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
    private boolean[][] shape;
    private ColorPalette colorPalette;
    private int colorMode; // 0: 일반, 1: 적녹색맹, 2: 청황색맹
    private int x;
    private int y;
    private boolean item;
    private ItemType itemType;
    private int[] itemPos; //x, y
    private PatternType patternType = PatternType.NONE;

    public BlockData(boolean[][] shape, ColorPalette colorPalette, int colorMode, int x, int y, boolean item, ItemType itemType, int[] itemPos) {
        this.shape = shape;
        this.colorPalette = colorPalette;
        this.colorMode = colorMode;
        this.x = x;
        this.y = y;
        this.item = item;
        this.itemType = itemType;
        this.itemPos = itemPos;
    }
    public BlockData(boolean[][] shape, ColorPalette colorPalette, int colorMode) {
        this(shape, colorPalette, colorMode, 3, 0, false, ItemType.NORMAL, null); // 보드 상단 중앙 부근에서 생성되도록 초기 위치 설정 (보드가 10칸이므로)
    }
    public BlockData(boolean[][] shape, ColorPalette colorPalette, int colorMode, int x, int y) {
        this(shape, null, 0, x, y, false, ItemType.NORMAL, null);
    }
    public BlockData(boolean[][] shape, int x, int y) {
        this(shape, null, 0, x, y, false, ItemType.NORMAL, null);
    }

    public boolean IsItem() {
        return this.item;
    }
    public void SetItem(boolean item) {
        this.item = item;
    }
    public ItemType GetItemType() {
        return this.itemType;
    }
    public void SetItemType(ItemType itemType) {
        this.itemType = itemType;
    }
    public int[] GetItemPos() {
        return this.itemPos;
    }
    public void SetItemPos(int[] itemPos) {
        this.itemPos = itemPos;
    }

    public boolean[][] GetShape() {
        return this.shape;
    }

    public PatternType GetPatternType() {
        return this.patternType;
    }

    public void SetPatternType(PatternType patternType) {
        this.patternType = patternType;
    }

    public void SetShape(boolean[][] shape) {
        this.shape = shape;
    }

    public Color GetCurrentColor() {
        return this.colorPalette.GetColor(colorMode);
    }

    public int GetX() { return this.x; }
    public void SetX(int x) { this.x = x; }
    
    public int GetY() { return this.y; }
    public void SetY(int y) { this.y = y; }
}
