package tetris.gamescene;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import tetris.settings.GameSettings;

class GameControllerTest {
    @Test
    void requiresPauseCallback() {
        assertThrows(NullPointerException.class,
                () -> new GameController(null, new GameSettings(
                        "1280x720", 1280, 720, 0,
                        GameSettings.getInputSettingData().getAllBindings(),
                        GameSettings.Difficulty.NORMAL)));
    }
}
