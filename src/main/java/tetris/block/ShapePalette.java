package src.main.java.tetris.block;

/*
    SapePalette 클래스는 블럭의 모양 정보를 저장
    각 블럭의 모양을 행렬로 저장
*/

public class ShapePalette {
    
    public int[][] GetShapeI() {
        return new int[][] { {1, 1, 1, 1} };
    }

    public int[][] GetShapeJ() {
        return new int[][] {
            {1, 0, 0},
            {1, 1, 1}
        };
    }

    public int[][] GetShapeL() {
        return new int[][] {
            {0, 0, 1},
            {1, 1, 1}
        };
    }

    public int[][] GetShapeO() {
        return new int[][] {
            {1, 1},
            {1, 1}
        };
    }

    public int[][] GetShapeS() {
        return new int[][] {
            {0, 1, 1},
            {1, 1, 0}
        };
    }

    public int[][] GetShapeT() {
        return new int[][] {
            {0, 1, 0},
            {1, 1, 1}
        };
    }

    public int[][] GetShapeZ() {
        return new int[][] {
            {1, 1, 0},
            {0, 1, 1}
        };
    }
}