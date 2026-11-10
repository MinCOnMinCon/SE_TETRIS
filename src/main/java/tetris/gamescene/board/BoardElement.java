package tetris.gamescene.board;

import java.io.Serializable;

import javafx.scene.paint.Color;
import tetris.block.data.ColorType;
import tetris.block.data.PatternType;
import tetris.block.data.ColorTypeConverter;

public class BoardElement implements Serializable{
    // 클래스 버전 관리 (권장). 통신하는 양쪽의 클래스 형태가 같다는 것을 보증합니다.
    private static final long serialVersionUID = 1L;
    // transient로 데이터 전송 시 NULL값으로 변경
    private transient Color elementColor;
    private ColorType colorType;
    private boolean isBlock;
    private transient int colorMode;
    private transient PatternType patternType = PatternType.NONE;


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
