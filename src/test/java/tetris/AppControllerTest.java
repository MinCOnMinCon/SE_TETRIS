package tetris;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AppControllerTest {
    @Test
    void requiresStartScreen() {
        assertThrows(NullPointerException.class, () -> new AppController(null));
    }
}
