package tetris.gamescene.board;

import javafx.scene.paint.Color;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;
import tetris.block.data.ColorTypeConverter;

public class BoardElement{

    private Color elementColor;
    private ColorType colorType;
    private boolean isBlock;
    private int colorMode;
    private PatternType patternType = PatternType.NONE;


    public BoardElement(ColorType colorType, boolean block, int colorMode){
        this.colorType = colorType;
        this.isBlock = block;
        this.colorMode = colorMode;
    }
    public BoardElement(int colorMode){
        this(ColorType.NONE, false, colorMode);
    }


    
    public ColorType getElementColorType(){
        return colorType;
    }
    public void setElementColorType(ColorType colorType){
        if (this.colorType!=colorType)
            this.elementColor = null;
        this.colorType = colorType;
    }

    public Color getElementColor(){
        if (elementColor == null) {
            elementColor = ColorTypeConverter.ConvertEnumToColor(colorType, colorMode);
        }
        return elementColor;
    }
    public void setElementColor(Color elementColor){
        this.elementColor = elementColor;
    }

    public int getElementColorMode() {
        return colorMode;
    }

    public boolean isBlock(){
        return isBlock;
    }

    public PatternType getPatternType(){
        return patternType;
    }

    public void setPatternType(PatternType patternType){
        this.patternType = patternType;
    }

    public void setBlock(boolean block){
        isBlock = block;
    }
}
