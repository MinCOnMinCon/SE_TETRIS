package tetris.gameboard;
import javafx.scene.paint.Color;







public class GameBoard {
    public static final int boardRow = 20;
    public static final int boardCol = 10;
    public static final int boardRowPad = 1;
    public static final int boardColPad = 1;
    private final Color padColor = Color.GRAY;
    
    private BoardElement [][] board;
    
    public GameBoard(){
        InitGameBoard();
    }

    public BoardElement[][] getBoard(){
        return board;
    }

    public void InitGameBoard(){
        board = new BoardElement[boardRow + boardRowPad*2][boardCol + boardColPad*2];
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                if(row < boardRowPad || row > boardRow || col < boardColPad || col > boardCol){
                    board[row][col] = new BoardElement(padColor, false);
                }
                else{
                    board[row][col] = new BoardElement();
                }
            
            }
        }
 
    }
    
    public void PrintBoard(){ // 보드 배열 값 확인용
        for(int row = 0; row < board.length; row++){
            System.out.print("[");

            for(int col = 0; col < board[row].length; col++){
                BoardElement element = board[row][col];
                System.out.print("[" + element.isBlock() + ", "
                        + getColorName(element.getElementColor()) + "]");

                if(col < board[row].length - 1){
                    System.out.print(", ");
                }
            }

            System.out.println("]");
        }
    }

    private String getColorName(Color color){ // PrintBoard에서 색상을 문자열로 출려하기 위한 함수
        if(Color.GRAY.equals(color)) return "GRAY";
        if(Color.WHITE.equals(color)) return "WHITE";
        if(Color.RED.equals(color)) return "RED";
        if(Color.ORANGE.equals(color)) return "ORANGE";
        if(Color.YELLOW.equals(color)) return "YELLOW";
        if(Color.GREEN.equals(color)) return "GREEN";
        if(Color.BLUE.equals(color)) return "BLUE";
        if(Color.PURPLE.equals(color)) return "PURPLE";
        if(Color.CYAN.equals(color)) return "CYAN";
        if(Color.BLACK.equals(color)) return "BLACK";
        return "UNKNOWN_COLOR";
    }


}
