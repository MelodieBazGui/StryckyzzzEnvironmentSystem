package stryckyzzzGameElements;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

import parametres.MouvementParametres;

public class MouvementParametresTest {

    private final Path path = Paths.get("data/parametres/mouvement.json");

    @BeforeEach
    void setup() throws IOException {
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("Should have correct default WASD layout")
    void testDefaultLayout() {
        MouvementParametres m = new MouvementParametres();
        assertEquals("WASD", m.getLayout());
        assertEquals('W', m.getKeyForward());
        assertEquals('S', m.getKeyBackward());
        assertEquals('A', m.getKeyLeft());
        assertEquals('D', m.getKeyRight());
    }

    @Test
    @DisplayName("Should correctly apply ZQSD layout")
    void testZQSDLayout() {
        MouvementParametres m = new MouvementParametres();
        m.applyLayout("ZQSD");
        assertEquals("ZQSD", m.getLayout());
        assertEquals('Z', m.getKeyForward());
        assertEquals('S', m.getKeyBackward());
        assertEquals('Q', m.getKeyLeft());
        assertEquals('D', m.getKeyRight());
    }

    @Test
    @DisplayName("Should save and reload correctly")
    void testSaveAndLoad() {
        MouvementParametres m1 = new MouvementParametres();
        m1.applyLayout("ZQSD");
        m1.save();

        MouvementParametres m2 = new MouvementParametres();
        m2.load();

        assertEquals("ZQSD", m2.getLayout());
        assertEquals('Z', m2.getKeyForward());
    }

    @Test
    @DisplayName("Should auto-create JSON file on load if missing")
    void testAutoCreateOnLoad() {
        MouvementParametres m = new MouvementParametres();
        assertFalse(Files.exists(path));
        m.load();
        assertTrue(Files.exists(path), "Should auto-create mouvement.json file");
    }
}
