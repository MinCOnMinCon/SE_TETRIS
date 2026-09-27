package tetris.gameboard;

import javafx.scene.paint.Color;

public class BoardElement{

    private Color elementColor;
    private boolean isBlock;


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

    public void setBlock(boolean block){
        isBlock = block;
    }
}