package tetris.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

class KeyBindingSettingsTest {
    @Test
    void startsWithDefaultBindingsAndAllowsChangingOneAction() {
        KeyBindingSettings bindings = new KeyBindingSettings();

        assertEquals(KeyCode.LEFT, bindings.getKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT));
        assertEquals(List.of(KeyCode.X, KeyCode.UP),
                bindings.getKeyCodes(KeyBindingSettings.ActionType.ROTATE_CLOCK));

        bindings.setKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT, KeyCode.A);
        assertEquals(KeyCode.A, bindings.getKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT));
    }

    @Test
    void copiesKeyListsAndRejectsWrongArraySize() {
        KeyBindingSettings bindings = new KeyBindingSettings();
        bindings.setKeyCodes(KeyBindingSettings.ActionType.HOLD, List.of(KeyCode.C, KeyCode.V));

        assertEquals(List.of(KeyCode.C, KeyCode.V),
                bindings.getKeyCodes(KeyBindingSettings.ActionType.HOLD));
        assertThrows(IllegalArgumentException.class, () -> new KeyBindingSettings(new KeyCode[0]));
    }

    @Test
    void resetToDefaultsRestoresChangedKeys() {
        KeyBindingSettings bindings = new KeyBindingSettings();
        bindings.setKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT, KeyCode.A);

        bindings.resetToDefaults();

        assertEquals(KeyCode.LEFT, bindings.getKeyCode(KeyBindingSettings.ActionType.MOVE_LEFT));
    }
}
