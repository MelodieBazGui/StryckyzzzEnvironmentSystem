package engineTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.IdGenerator;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @BeforeEach
    void reset() {
        IdGenerator.reset();
    }

    @Test
    void testUniqueIds() {
        int a = IdGenerator.nextId();
        int b = IdGenerator.nextId();
        assertNotEquals(a, b);
    }

    @Test
    void testResetResetsCounter() {
        int id1 = IdGenerator.nextId();
        IdGenerator.reset();
        int id2 = IdGenerator.nextId();
        assertEquals(0, id2);
    }
}

