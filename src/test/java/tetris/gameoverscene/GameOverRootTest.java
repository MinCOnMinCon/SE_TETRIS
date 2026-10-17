package tetris.gameoverscene;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameOverRootTest {
    @Test
    void requiresGameResult() {
        assertThrows(NullPointerException.class, () -> new GameOverRoot(null));
    }
}
