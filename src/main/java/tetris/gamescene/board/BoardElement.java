package tetris.gamescene.board;

import javafx.scene.paint.Color;
import tetris.block.data.PatternType;

public class BoardElement{

    private Color elementColor;
    private boolean isBlock;
    private PatternType patternType = PatternType.NONE;


    public BoardElement(Color color, boolean block){
        this.elementColor = color;
        this.isBlock = block;
    }
    public BoardElement(){
        this(Color.BLACK, false);
    }


    
    public Color getElementColor(){
        return elementColor;
    }

    public void setElementColor(Color elementColor){
        this.elementColor = elementColor;
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
