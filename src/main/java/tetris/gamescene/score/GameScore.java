package tetris.gamescene.score;

public class GameScore{
    
    
    private final int[] blockDownScoreArr = {10, 20, 30, 40, 50};

    private long gameScore;
    
    
    public GameScore(){
        gameScore = 0;
    }

    public long GetGameScore(){
        return gameScore;
    }

    public void GetBlockDownScore(int fallSpeedLevel){
        SetGameScore(blockDownScoreArr[fallSpeedLevel]);
    }

    public void SetGameScore(long score){
        if(Long.MAX_VALUE - score < gameScore){
            gameScore = Long.MAX_VALUE;
        }
        else{
            gameScore += score;
        }
    }


}