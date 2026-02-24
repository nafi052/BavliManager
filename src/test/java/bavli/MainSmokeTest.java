package bavli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainSmokeTest {

    @Test
    void mainClassIsLoadable() throws ClassNotFoundException {
        Class<?> mainClass = Class.forName("bavli.Main");
        assertNotNull(mainClass);
    }
}
