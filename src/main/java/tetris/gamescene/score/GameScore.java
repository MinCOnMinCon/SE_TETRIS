package tetris.gamescene.score;

import java.util.Objects;
import tetris.gamescene.GameProgress;

public class GameScore{
    
    
    private final int basicDownScore = 10;
    private final int downScoreIncrese = 5;
    private final GameProgress progress;
    private final int[] lineClearScores = {100, 300, 500, 800};

    private int gameScore;
    
    
    public GameScore(GameProgress progress){
        this.progress = Objects.requireNonNull(progress);
        gameScore = 0;
    }

    public int GetGameScore(){
        return gameScore;
    }

    public void AddBlockDownScore(){
        AddGameScore(basicDownScore +  downScoreIncrese * progress.GetLevel());
    }

    public void AddLineClearScore(int clearedLines){
        if (clearedLines < 1 || clearedLines > lineClearScores.length) {
            throw new IllegalArgumentException("clearedLines must be between 1 and 4");
        }
        AddGameScore(lineClearScores[clearedLines - 1] * (progress.GetLevel() + 1));
    }

    public void AddGameScore(int score){
        if(Integer.MAX_VALUE - score < gameScore){
            gameScore = Integer.MAX_VALUE;
        }
        else{
            gameScore += score;
        }
    }


}
