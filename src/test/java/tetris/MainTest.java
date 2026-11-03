package tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void isAJavaFxApplicationEntryPoint() {
        assertEquals(Application.class, Main.class.getSuperclass());
    }

    @Test
    void mainIsThePublicStaticEntryPoint() throws Exception {
        Method mainMethod = Main.class.getMethod("main", String[].class);
        Method startMethod = Main.class.getMethod("start", Stage.class);

        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        assertEquals(void.class, mainMethod.getReturnType());
        assertEquals(void.class, startMethod.getReturnType());
    }
}
