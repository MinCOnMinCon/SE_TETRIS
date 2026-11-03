package tetris.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

class GameSettingsTest {
    @Test
    void snapshotKeepsResolutionDifficultyColorAndBindings() {
        KeyBindingSettings bindings = new KeyBindingSettings();
        GameSettings settings = new GameSettings(
                "1280x720", 1280, 720, 2, bindings.getAllBindings(),
                GameSettings.Difficulty.EASY, GameSettings.GameMode.NORMAL);
        bindings.setKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT, KeyCode.A);

        assertEquals("1280x720", settings.resolutionPreset());
        assertEquals(1280, settings.screenWidth());
        assertEquals(720, settings.screenHeight());
        assertEquals(2, settings.colorMode());
        assertEquals(GameSettings.Difficulty.EASY, settings.difficulty());
        assertEquals(KeyCode.LEFT,
                settings.keyBindings().get(KeyBindingSettings.ActionType.MOVE_LEFT).get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> settings.keyBindings().put(KeyBindingSettings.ActionType.PAUSE, java.util.List.of(KeyCode.P)));
    }

    @Test
    void withDifficultyKeepsTheOtherSavedSettings() {
        GameSettings settings = new GameSettings(
                "960x540", 960, 540, 1, new KeyBindingSettings().getAllBindings(),
                GameSettings.Difficulty.NORMAL, GameSettings.GameMode.ITEM);

        GameSettings changed = settings.withDifficulty(GameSettings.Difficulty.HARD);

        assertEquals(GameSettings.Difficulty.HARD, changed.difficulty());
        assertEquals("960x540", changed.resolutionPreset());
        assertEquals(960, changed.screenWidth());
        assertEquals(540, changed.screenHeight());
        assertEquals(1, changed.colorMode());
        assertEquals(GameSettings.GameMode.ITEM, changed.gameMode());
    }

    @Test
    void nullGameModeBecomesNormal() {
        GameSettings settings = new GameSettings(
                "960x540", 960, 540, 0, new KeyBindingSettings().getAllBindings(),
                GameSettings.Difficulty.NORMAL, null);

        assertEquals(GameSettings.GameMode.NORMAL, settings.gameMode());
    }
}
