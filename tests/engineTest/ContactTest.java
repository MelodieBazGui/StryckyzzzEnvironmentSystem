package engineTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.Contact;
import math.Vec3;

class ContactTest {

    private Contact contact;
    private Vec3 point;
    private Vec3 normal;

    @BeforeEach
    void setUp() {
        point = new Vec3(1, 2, 3);
        normal = new Vec3(0, 0, 1);
        contact = new Contact(1, 2, point, normal, 0.05f, 0.6f, 0.4f);
    }

    @AfterEach
    void tearDown() {
        contact = null;
        point = null;
        normal = null;
    }

    @Test
    void testIdsStoredCorrectly() {
        assertEquals(1, contact.a);
        assertEquals(2, contact.b);
    }

    @Test
    void testPointAndNormalStored() {
        assertEquals(point.getX(), contact.point.getX(), 1e-6);
        assertEquals(point.getY(), contact.point.getY(), 1e-6);
        assertEquals(point.getZ(), contact.point.getZ(), 1e-6);

        // Normal should be normalized
        assertEquals(1.0f, contact.normal.len(), 1e-6);
        assertEquals(0f, contact.normal.getX(), 1e-6);
        assertEquals(0f, contact.normal.getY(), 1e-6);
        assertEquals(1f, contact.normal.getZ(), 1e-6);
    }

    @Test
    void testPenetrationAndFriction() {
        assertEquals(0.05f, contact.penetration, 1e-6);
        assertEquals(0.6f, contact.staticFriction, 1e-6);
        assertEquals(0.4f, contact.dynamicFriction, 1e-6);
    }

    @Test
    void testTangentsAreOrthogonal() {
        contact.computeTangents();
        Vec3 u = contact.getTangentU();
        Vec3 v = contact.getTangentV();
        
        // Length should be 1
        assertEquals(1f, u.len(), 1e-6);
        assertEquals(1f, v.len(), 1e-6);

        
        // u · normal = 0
        assertEquals(0f, u.dot(contact.normal), 1e-6);

        // v · normal = 0
        assertEquals(0f, v.dot(contact.normal), 1e-6);

        // u · v = 0
        assertEquals(0f, u.dot(v), 1e-6);
    }

    @Test
    void testToStringContainsData() {
        String s = contact.toString();
        assertTrue(s.contains("a=1"));
        assertTrue(s.contains("b=2"));
        assertTrue(s.contains("penetration"));
        assertTrue(s.contains("μs"));
        assertTrue(s.contains("μd"));
    }
}

