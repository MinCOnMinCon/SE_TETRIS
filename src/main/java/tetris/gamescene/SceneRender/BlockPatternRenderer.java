package tetris.gamescene.scenerender;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import tetris.block.data.PatternType;

// 칸의 색은 유지하고 그 위에 패턴만 그린다. 표시할 모드와 위치는 SceneRenderer가 결정한다.
public class BlockPatternRenderer {
    public void DrawPattern(GraphicsContext graphicsContext, PatternType patternType,
            double x, double y, double side, Color blockColor) {
        if (side <= 0 || patternType == null || patternType == PatternType.NONE) return;

        graphicsContext.save();
        try {
            // 칸 안쪽만 그려서 인접한 칸이나 테두리를 침범하지 않는다.
            graphicsContext.beginPath();
            graphicsContext.rect(x + side * 0.1, y + side * 0.1, side * 0.8, side * 0.8);
            graphicsContext.clip();
            graphicsContext.beginPath();

            double brightness = 0.299 * blockColor.getRed()
                    + 0.587 * blockColor.getGreen() + 0.114 * blockColor.getBlue();
            Color contrastColor = brightness >= 0.5 ? Color.BLACK : Color.WHITE;
            // 불투명한 최종 색을 미리 계산해 겹치는 부분도 같은 진하기를 유지한다.
            Color patternColor = blockColor.interpolate(contrastColor, 0.7);
            graphicsContext.setStroke(patternColor);
            graphicsContext.setFill(patternColor);
            graphicsContext.setGlobalAlpha(1.0);
            // 확대 변환 없이 화면 픽셀 단위로 그린다.
            graphicsContext.setLineWidth(Math.max(1, Math.round(side * 0.06)));

            switch (patternType) {
                case VERTICAL_STRIPES:
                    DrawStripes(graphicsContext, x, y, side, true, false);
                    break;
                case HORIZONTAL_STRIPES:
                    DrawStripes(graphicsContext, x, y, side, false, true);
                    break;
                case GRID:
                    DrawStripes(graphicsContext, x, y, side, true, true);
                    break;
                case DIAGONAL_UP:
                case DIAGONAL_DOWN:
                    for (int i = -3; i <= 3; i++) {
                        double offset = i * 0.3;
                        if (patternType == PatternType.DIAGONAL_UP) {
                            graphicsContext.strokeLine(x + offset * side, y + side,
                                    x + (offset + 1) * side, y);
                        } else {
                            graphicsContext.strokeLine(x + offset * side, y,
                                    x + (offset + 1) * side, y + side);
                        }
                    }
                    break;
                case CHECKERBOARD:
                    for (int row = 0; row < 4; row++) {
                        for (int col = 0; col < 4; col++) {
                            if ((row + col) % 2 == 0) {
                                // 경계를 정수 픽셀로 맞춰 칸마다 흐려지는 정도가 달라지지 않게 한다.
                                double left = Math.round(x + (0.1 + col * 0.2) * side);
                                double top = Math.round(y + (0.1 + row * 0.2) * side);
                                double right = Math.round(x + (0.1 + (col + 1) * 0.2) * side);
                                double bottom = Math.round(y + (0.1 + (row + 1) * 0.2) * side);
                                graphicsContext.fillRect(left, top, right - left, bottom - top);
                            }
                        }
                    }
                    break;
                case DOTS:
                    for (int row = 0; row < 3; row++) {
                        for (int col = 0; col < 3; col++) {
                            graphicsContext.fillOval(x + (0.2 + col * 0.25) * side,
                                    y + (0.2 + row * 0.25) * side, side * 0.1, side * 0.1);
                        }
                    }
                    break;
                default:
                    break;
            }
        } finally {
            graphicsContext.restore();
        }
    }

    private void DrawStripes(GraphicsContext graphicsContext, double x, double y, double side,
            boolean vertical, boolean horizontal) {
        // 홀수 두께의 선은 반 픽셀, 짝수 두께는 정수 픽셀에 중심을 맞춘다.
        double pixelOffset = ((int) graphicsContext.getLineWidth() % 2 == 0) ? 0 : 0.5;
        for (int i = 1; i <= 3; i++) {
            double position = i * 0.25;
            if (vertical) {
                double lineX = Math.floor(x + position * side) + pixelOffset;
                graphicsContext.strokeLine(lineX, y + side * 0.1, lineX, y + side * 0.9);
            }
            if (horizontal) {
                double lineY = Math.floor(y + position * side) + pixelOffset;
                graphicsContext.strokeLine(x + side * 0.1, lineY, x + side * 0.9, lineY);
            }
        }
    }
}
