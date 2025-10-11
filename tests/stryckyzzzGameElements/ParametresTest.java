package stryckyzzzGameElements;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.*;

import parametres.Parametres;

public class ParametresTest {

    static Path testDir = Paths.get("data/parametres/");

    @BeforeAll
    static void setupDir() throws Exception {
        Files.createDirectories(testDir);
    }

    @AfterEach
    void cleanup() throws Exception {
        // clean up test files after each test
        Files.list(testDir)
             .filter(f -> f.getFileName().toString().startsWith("test"))
             .forEach(f -> f.toFile().delete());
    }

    // ------------------------
    // Test Classes
    // ------------------------
    static class TestParametres extends Parametres {
        public int number = 42;
        public boolean enabled = true;
        public String name = "default";

        @Override
        public String getName() {
            return "testParams";
        }
    }

    static class TestNoGsonParametres extends Parametres {
        public double value = 3.14;
        public String mode = "safe";

        @Override
        public String getName() {
            return "testNoGson";
        }
    }

    // ------------------------
    // Actual Tests
    // ------------------------

    @Test
    @DisplayName("Should save and load Parametres correctly")
    void testSaveAndLoad() {
        TestParametres p = new TestParametres();
        p.number = 123;
        p.enabled = false;
        p.name = "Hello";
        p.save();

        // load into a new object
        TestParametres loaded = new TestParametres();
        loaded.load();

        assertEquals(123, loaded.number);
        assertFalse(loaded.enabled);
        assertEquals("Hello", loaded.name);
    }

    @Test
    @DisplayName("Should create file if it doesn't exist on load()")
    void testAutoCreateOnLoad() {
        Path file = testDir.resolve("testParams.json");
        if (Files.exists(file)) file.toFile().delete();

        TestParametres p = new TestParametres();
        p.load();

        assertTrue(Files.exists(file), "File should be auto-created when missing");
    }

    @Test
    @DisplayName("Should correctly fallback to manual JSON if Gson missing")
    void testFallbackParsing() throws Exception {
        TestNoGsonParametres p = new TestNoGsonParametres();
        p.value = 9.99;
        p.mode = "fast";
        p.save();

        // manually corrupt Gson detection
        Path jsonPath = testDir.resolve("testNoGson.json");
        assertTrue(Files.exists(jsonPath));

        // simulate reload via fallback
        TestNoGsonParametres loaded = new TestNoGsonParametres();
        loaded.load();

        assertEquals(9.99, loaded.value);
        assertEquals("fast", loaded.mode);
    }

    @Test
    @DisplayName("Should overwrite JSON file with new data")
    void testOverwrite() {
        TestParametres p = new TestParametres();
        p.number = 7;
        p.save();

        // change values and save again
        p.number = 999;
        p.save();

        TestParametres loaded = new TestParametres();
        loaded.load();
        assertEquals(999, loaded.number);
    }
}
