package tetris.settings;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettingsStoreTest {
    @Test
    void providesGameSettingsSnapshotWhenLoadedLazily() {
        GameSettings settings = SettingsStore.getGameSettings();

        assertNotNull(settings);
        assertTrue(settings.screenWidth() > 0);
        assertTrue(settings.screenHeight() > 0);
    }
}
