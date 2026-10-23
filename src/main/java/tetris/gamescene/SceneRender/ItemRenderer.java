package tetris.gamescene.scenerender;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock.ItemType;

// 블록의 아이템 정보를 해석하고, 해당 칸 위에 기호를 그린다.
public class ItemRenderer {
    // 블록 내부 좌표 [col, row]가 아이템 기호를 그리는 칸인지 확인한다.
    public boolean IsItemCell(BlockData block, int col, int row) {
        int[] itemPos = block.GetItemPos();
        return block.IsItem()
                && itemPos != null && itemPos[0] == col && itemPos[1] == row;
    }

    // originX, originY는 shape의 왼쪽 위 화면 좌표다.
    public void DrawBlockItem(GraphicsContext graphicsContext, BlockData block,
            double originX, double originY, double side) {
        if (block == null || !block.IsItem() || block.GetItemType() == ItemType.DEL_4VLINE) return;

        int[] itemPos = block.GetItemPos();
        if (itemPos == null) return;
        int itemX = itemPos[0];
        int itemY = itemPos[1];

        boolean[][] shape = block.GetShape();
        if (itemY < 0 || itemY >= shape.length || itemX < 0
                || itemX >= shape[itemY].length || !shape[itemY][itemX]) return;

        // 블록 색과 기호 색이 비슷하면 기호가 묻히므로, 배경인 블록의 밝기를 계산한다.
        Color color = block.GetCurrentColor();
        // 눈이 느끼는 밝기에 맞춰 RGB에 가중치를 적용한다(초록의 영향이 가장 크다).
        double brightness = 0.299 * color.getRed()
                + 0.587 * color.getGreen() + 0.114 * color.getBlue();
        // 밝은 블록에는 검정, 어두운 블록에는 흰색 기호를 그려 대비를 확보한다.
        Color symbolColor = brightness >= 0.5 ? Color.BLACK : Color.WHITE;
        DrawItem(graphicsContext, block.GetItemType(),
                originX + itemX * side, originY + itemY * side, side, symbolColor);
    }

    // x, y: 그릴 칸의 왼쪽 위 화면 좌표, side: 한 칸의 크기(px)
    private void DrawItem(GraphicsContext graphicsContext, ItemType itemType,
            double x, double y, double side, Color symbolColor) {
        if (side <= 0 || itemType == null
                || itemType == ItemType.NORMAL || itemType == ItemType.DEL_4VLINE) {
            return;
        }

        graphicsContext.save();
        try {
            // 0~1 좌표로 그려 보드와 미리보기에서 같은 비율을 유지한다.
            graphicsContext.translate(x, y);
            graphicsContext.scale(side, side);
            graphicsContext.setStroke(symbolColor);
            graphicsContext.setFill(symbolColor);
            graphicsContext.setLineWidth(0.065);
            graphicsContext.setLineCap(StrokeLineCap.ROUND);

            switch (itemType) {
                case DEL_HLINE:
                    DrawArrow(graphicsContext, 0.38, 0.5, 0.14, 0.5);
                    DrawArrow(graphicsContext, 0.62, 0.5, 0.86, 0.5);
                    DrawCenterCircle(graphicsContext);
                    break;
                case DEL_VLINE:
                    DrawArrow(graphicsContext, 0.5, 0.38, 0.5, 0.14);
                    DrawArrow(graphicsContext, 0.5, 0.62, 0.5, 0.86);
                    DrawCenterCircle(graphicsContext);
                    break;
                case DEL_AEMPTY:
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4;
                        double dx = Math.cos(angle);
                        double dy = Math.sin(angle);
                        DrawArrow(graphicsContext,
                                0.5 + dx * 0.12, 0.5 + dy * 0.12,
                                0.5 + dx * 0.37, 0.5 + dy * 0.37);
                    }
                    break;
                case DEL_VEMPTY:
                    graphicsContext.setFont(Font.font("System", FontWeight.BOLD, 0.75));
                    graphicsContext.setTextAlign(TextAlignment.CENTER);
                    graphicsContext.setTextBaseline(VPos.CENTER);
                    graphicsContext.fillText("P", 0.5, 0.5, 0.8);
                    break;
                default:
                    break;
            }
        } finally {
            // 선과 글꼴 설정이 호출한 렌더러에 영향을 주지 않도록 복원한다.
            graphicsContext.restore();
        }
    }

    private void DrawCenterCircle(GraphicsContext graphicsContext) {
        graphicsContext.strokeOval(0.38, 0.38, 0.24, 0.24);
    }

    private void DrawArrow(GraphicsContext graphicsContext,
            double startX, double startY, double endX, double endY) {
        graphicsContext.strokeLine(startX, startY, endX, endY);
        double angle = Math.atan2(endY - startY, endX - startX);
        double headLength = 0.11;
        double headAngle = Math.PI / 5;
        graphicsContext.strokeLine(endX, endY,
                endX - headLength * Math.cos(angle - headAngle),
                endY - headLength * Math.sin(angle - headAngle));
        graphicsContext.strokeLine(endX, endY,
                endX - headLength * Math.cos(angle + headAngle),
                endY - headLength * Math.sin(angle + headAngle));
    }
}
