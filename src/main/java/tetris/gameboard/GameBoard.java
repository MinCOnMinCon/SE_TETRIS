package tetris.gameboard;
import java.util.Arrays;







public class GameBoard {
    public static final int boardRow = 20;
    public static final int boardCol = 10;
    public static final int boardRowPad = 1;
    public static final int boardColPad = 1;

    private BoardElement [][] board;
    


    public void InitGameBoard(){
        board = new BoardElement[boardRow + boardRowPad][boardCol + boardColPad];
        for(BoardElement[] row: board){
            for(BoardElement element : row){
                element = new BoardElement();
            }
        }
    }

    

    

}
