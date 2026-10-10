package tetris.gamescene.board;
import javafx.scene.paint.Color;
import java.util.Arrays;







public class GameBoard {
    public static final int boardRow = 20;
    public static final int boardCol = 10;
    public static final int maxClearedLines = 4;
    
    
    private BoardElement [][] board;
    private final int[] clearedRows = new int[maxClearedLines];
    
    public GameBoard(){
        InitGameBoard();
    }

    public BoardElement[][] GetBoard(){
        return board;
    }

    // 이동 함수와 공유하는 배열. -1은 삭제된 행이 없는 칸을 뜻한다.
    public int[] GetClearedRows(){
        return clearedRows;
    }

    public void InitGameBoard(){
        Arrays.fill(clearedRows, -1);
        board = new BoardElement[boardRow][boardCol];
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                
                board[row][col] = new BoardElement();
                
            
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
