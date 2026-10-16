package tetris.gamescene;

import tetris.settings.GameSettings;

public record GameResult(
        int score,
                GameSettings.Difficulty difficulty,
                GameSettings.GameMode gameMode) {

        public GameResult(int score, GameSettings.Difficulty difficulty) {
                this(score, difficulty, GameSettings.GameMode.NORMAL);
        }
}
