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
                GameSettings.Difficulty.EASY);
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
}
