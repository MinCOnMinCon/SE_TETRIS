package tetris.block.data;

import java.util.ArrayList;
import javafx.scene.paint.Color;



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
    private ColorType colorType;
    private int colorMode; // 0: 일반, 1: 적녹색맹, 2: 청황색맹
    private Color color; // 현재 색상
    private int x;
    private int y;
    private boolean item;
    private ItemType itemType = ItemType.NORMAL;
    private int[] itemPos; //x, y
    private PatternType patternType = PatternType.NONE;
    private int[][] currentShapeIndex;

    public BlockData(boolean[][] shape, ColorType colorType, int colorMode, int x, int y, boolean item, ItemType itemType, int[] itemPos) {
        this.shape = shape;
        this.colorType = colorType;
        this.colorMode = colorMode;
        this.x = x;
        this.y = y;
        this.item = item;
        this.itemType = itemType;
        this.itemPos = itemPos;
        this.currentShapeIndex = currentShapeIndex(shape);
    }
    public BlockData(boolean[][] shape, ColorType colorType, int colorMode) {
        this(shape, colorType, colorMode, 3, 0, false, ItemType.NORMAL, null); // 보드 상단 중앙 부근에서 생성되도록 초기 위치 설정 (보드가 10칸이므로)
    }
    public BlockData(boolean[][] shape, int x, int y) {
        this(shape, ColorType.GRAY, 0, x, y, false, ItemType.NORMAL, null);
    }


    private int[][] currentShapeIndex(boolean[][] shape) {
        ArrayList<int[]> resultList = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j]) {
                    resultList.add(new int[]{i, j});
                }
            }
        }
        return resultList.toArray(new int[0][2]);
    }


    public boolean[][] GetShape() {
        return this.shape;
    }
    public void SetShape(boolean[][] shape) {
        this.shape = shape;
        this.currentShapeIndex = currentShapeIndex(shape);
    }
    public ColorType GetColorType() {
        return this.colorType;
    }
    public int GetColorMode() {
        return this.colorMode;
    }
    public Color GetColor() {
        if (this.color == null) {
            this.color = ColorTypeConverter.ConvertEnumToColor(this.colorType, this.colorMode);
        }
        return this.color;
    }
    public int GetX() { return this.x; }
    public void SetX(int x) { this.x = x; }
    public int GetY() { return this.y; }
    public void SetY(int y) { this.y = y; }
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
    public PatternType GetPatternType() {
        return this.patternType;
    }
    public void SetPatternType(PatternType patternType) {
        this.patternType = patternType;
    }
    public int[][] GetCurrentShapeIndex() {
        return this.currentShapeIndex;
    }
}
