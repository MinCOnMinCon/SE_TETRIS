package tetris.block.data;
import javafx.scene.paint.Color;

public class ColorTypeConverter {
    public static Color ConvertEnumToColor(ColorType color, int colorMode) {
        switch (color) {
            case NONE:
                return Color.BLACK;
            case BlockI:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(135, 206, 235);
                    case 2:
                        return Color.BLUE;
                    default:
                        return Color.CYAN;
                }
            case BlockJ:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(0, 0, 128);
                    case 2:
                        return Color.rgb(128, 0, 128);
                    default:
                        return Color.BLUE;
                }
            case BlockL:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(255, 140, 0);
                    case 2:
                        return Color.rgb(255, 69, 0);
                    default:
                        return Color.ORANGE;
                }
            case BlockO:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(255, 215, 0);
                    case 2:
                        return Color.rgb(255, 250, 205);
                    default:
                        return Color.YELLOW;
                }
            case BlockS:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(0, 255, 255);
                    case 2:
                        return Color.rgb(60, 179, 113);
                    default:
                        return Color.GREEN;
                }
            case BlockT:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(148, 0, 211);
                    case 2:
                        return Color.rgb(218, 112, 214);
                    default:
                        return Color.PURPLE;
                }
            case BlockZ:
                switch (colorMode) {
                    case 1:
                        return Color.rgb(139, 0, 0);
                    case 2:
                        return Color.rgb(139, 0, 0);
                    default:
                        return Color.RED;
                }
            case GRAY:
                return Color.GRAY;

            default:
                return Color.BLACK;
        }
    }
}
