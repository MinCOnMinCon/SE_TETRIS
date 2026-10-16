package tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.application.Application;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void isAJavaFxApplicationEntryPoint() {
        assertEquals(Application.class, Main.class.getSuperclass());
    }
}
