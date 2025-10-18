package engineTest;

import org.junit.jupiter.api.Test;

import engine.IdRegistry;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

class IdRegistryTest {

    @Test
    void testRegisterAndRetrieve() {
        IdRegistry<String> registry = new IdRegistry<>();
        int id = registry.register("EntityA");
        assertEquals("EntityA", registry.get(id));
        assertTrue(registry.isActive(id));
    }

    @Test
    void testUnregisterAndReuseId() {
        IdRegistry<String> registry = new IdRegistry<>();
        int id1 = registry.register("A");
        registry.unregister(id1);
        int id2 = registry.register("B");
        assertEquals("B", registry.get(id2));
        assertTrue(id2 == id1, "Should reuse freed ID");
    }

    @Test
    void testActiveIds() {
        IdRegistry<String> registry = new IdRegistry<>();
        int id1 = registry.register("A");
        int id2 = registry.register("B");
        Set<Integer> ids = registry.activeIds();
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }
}
