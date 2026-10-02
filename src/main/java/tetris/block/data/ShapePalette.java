package tetris.block.data;

/*
    SapePalette 클래스는 블럭의 모양 정보를 저장
    각 블럭의 모양을 행렬로 저장
*/

public class ShapePalette {
    
    public boolean[][] GetShapeI() {
        return new boolean[][] { {true, true, true, true} };
    }

    public boolean[][] GetShapeJ() {
        return new boolean[][] {
            {true, false, false},
            {true, true, true}
        };
    }

    public boolean[][] GetShapeL() {
        return new boolean[][] {
            {false, false, true},
            {true, true, true}
        };
    }

    public boolean[][] GetShapeO() {
        return new boolean[][] {
            {true, true},
            {true, true}
        };
    }

    public boolean[][] GetShapeS() {
        return new boolean[][] {
            {false, true, true},
            {true, true, false}
        };
    }

    public boolean[][] GetShapeT() {
        return new boolean[][] {
            {false, true, false},
            {true, true, true}
        };
    }

    public boolean[][] GetShapeZ() {
        return new boolean[][] {
            {true, true, false},
            {false, true, true}
        };
    }
}