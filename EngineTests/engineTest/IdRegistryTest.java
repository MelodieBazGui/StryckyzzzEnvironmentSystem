package engineTest;

import org.junit.jupiter.api.Test;

import engine.IdRegistry;

import static org.junit.jupiter.api.Assertions.*;

class IdRegistryTest {
    @Test
    void registerAndUnregister() {
        IdRegistry<String> reg = new IdRegistry<>();

        int id1 = reg.register("A");
        int id2 = reg.register("B");

        assertEquals("A", reg.get(id1));
        assertEquals("B", reg.get(id2));
        assertTrue(reg.isActive(id1));
        assertEquals(2, reg.size());

        reg.unregister(id1);
        assertNull(reg.get(id1));
        assertFalse(reg.isActive(id1));

        int id3 = reg.register("C"); // should recycle id1
        assertEquals(id1, id3);
        assertEquals("C", reg.get(id3));
    }
}
