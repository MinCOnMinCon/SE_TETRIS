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
                        return Color.web("#56B4E9");
                    case 2:
                        return Color.web("#67E6E4");
                    default:
                        return Color.CYAN;
                }
            case BlockJ:
                switch (colorMode) {
                    case 1:
                        return Color.web("#0072B2");
                    case 2:
                        return Color.web("#117733");
                    default:
                        return Color.BLUE;
                }
            case BlockL:
                switch (colorMode) {
                    case 1:
                        return Color.web("#E69F00");
                    case 2:
                        return Color.web("#FF99B9");
                    default:
                        return Color.ORANGE;
                }
            case BlockO:
                switch (colorMode) {
                    case 1:
                        return Color.web("#F0E442");
                    case 2:
                        return Color.web("#FAFAFA");
                    default:
                        return Color.YELLOW;
                }
            case BlockS:
                switch (colorMode) {
                    case 1:
                        return Color.web("#009E73");
                    case 2:
                        return Color.web("#90B830");
                    default:
                        return Color.GREEN;
                }
            case BlockT:
                switch (colorMode) {
                    case 1:
                        return Color.web("#F5F5F5");
                    case 2:
                        return Color.web("#9D4092");
                    default:
                        return Color.PURPLE;
                }
            case BlockZ:
                switch (colorMode) {
                    case 1:
                        return Color.web("#DE6321");
                    case 2:
                        return Color.web("#F34158");
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
