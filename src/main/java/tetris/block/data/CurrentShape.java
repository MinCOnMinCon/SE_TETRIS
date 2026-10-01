package tetris.block.data;

import java.util.ArrayList;

public class CurrentShape {
    public static int[][] currentShapeIndex(boolean[][] shape) {
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
}
