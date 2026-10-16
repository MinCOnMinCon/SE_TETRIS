package tetris;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StartScreenTest {
    @Test
    void exposesStageAndSceneProvidedAtConstruction() {
        StartScreen screen = new StartScreen(null, null);

        assertNull(screen.getStage());
        assertNull(screen.getScene());
    }
}
