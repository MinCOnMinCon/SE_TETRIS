package tetris.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SettingsControllerTest {
    @Test
    void colorModeNamesAndIndicesMatchGameModes() {
        assertEquals(0, GameSettings.ColorBlindMode.NORMAL.getColorMode());
        assertEquals(1, GameSettings.ColorBlindMode.RED_GREEN.getColorMode());
        assertEquals(2, GameSettings.ColorBlindMode.BLUE_YELLOW.getColorMode());
        assertEquals("일반 모드", GameSettings.ColorBlindMode.NORMAL.toString());
    }
}
