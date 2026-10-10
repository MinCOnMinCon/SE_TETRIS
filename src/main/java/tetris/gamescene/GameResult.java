package tetris.gamescene;

import tetris.settings.GameSettings;

// TODO: 일반·아이템 모드 구현 후 결과에 게임 모드 필드를 추가한다.
public record GameResult(
        long score,
        GameSettings.Difficulty difficulty) {}
