package mathTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import math.Vec3;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the high-performance Vec3 class.
 * Covers arithmetic, normalization, dot/cross products, and fast math.
 */
public class Vec3Test {

    private Vec3 a;
    private Vec3 b;

    @BeforeEach
    void setUp() {
        a = new Vec3(1f, 2f, 3f);
        b = new Vec3(4f, 5f, 6f);
    }

    // --------------------------------------------------
    // Basic operations
    // --------------------------------------------------

    @Test
    void testAddInPlace() {
        a.add(b);
        assertEquals(new Vec3(5f, 7f, 9f), a);
    }

    @Test
    void testSubInPlace() {
        a.sub(b);
        assertEquals(new Vec3(-3f, -3f, -3f), a);
    }

    @Test
    void testScale() {
        a.scl(2f);
        assertEquals(new Vec3(2f, 4f, 6f), a);
    }

    @Test
    void testNegate() {
        a.negate();
        assertEquals(new Vec3(-1f, -2f, -3f), a);
    }

    @Test
    void testZero() {
        a.zero();
        assertEquals(new Vec3(0f, 0f, 0f), a);
    }

    // --------------------------------------------------
    // Math helpers
    // --------------------------------------------------

    @Test
    void testDotProduct() {
        float dot = a.dot(b);
        // (1*4 + 2*5 + 3*6) = 32
        assertEquals(32f, dot, 1e-6f);
    }

    @Test
    void testCrossProduct() {
        a.cross(b); // mutates a
        // cross(1,2,3)x(4,5,6) = (-3,6,-3)
        assertEquals(new Vec3(-3f, 6f, -3f), a);
    }

    @Test
    void testLengthAndLengthSquared() {
        assertEquals(14f, a.len2(), 1e-6f);
        assertEquals((float) Math.sqrt(14f), a.len(), 1e-6f);
    }

    // --------------------------------------------------
    // Normalization
    // --------------------------------------------------

    @Test
    void testNormalizeStandard() {
        a.normalize();
        float len = a.len();
        assertEquals(1f, len, 1e-5f);
    }

    @Test
    void testNormalizeFast() {
        Vec3 v = new Vec3(5f, 0f, 0f);
        v.normalizeFast();
        // Should be roughly unit length (fast approx)
        assertEquals(1f, v.len(), 1e-2f);
    }

    @Test
    void testNormalizeZeroVector() {
        Vec3 v = new Vec3();
        v.normalize();
        assertEquals(new Vec3(0f, 0f, 0f), v);
    }

    // --------------------------------------------------
    // Static no-allocation methods (they return new Vec3)
    // --------------------------------------------------

    @Test
    void testStaticAddNoAlloc() {
        Vec3 out = Vec3.add(a, b);
        assertEquals(new Vec3(5f, 7f, 9f), out);
    }

    @Test
    void testStaticSubNoAlloc() {
        Vec3 out = Vec3.sub(b, a);
        assertEquals(new Vec3(3f, 3f, 3f), out);
    }

    @Test
    void testStaticSclNoAlloc() {
        Vec3 out = Vec3.scl(a, 2f);
        assertEquals(new Vec3(2f, 4f, 6f), out);
    }

    @Test
    void testStaticCrossNoAlloc() {
        Vec3 out = Vec3.cross(a, b);
        assertEquals(new Vec3(-3f, 6f, -3f), out);
    }

    @Test
    void testStaticDotNoAlloc() {
        assertEquals(32f, Vec3.dot(a, b), 1e-6f);
    }

    // --------------------------------------------------
    // Equality, copy, and hashCode
    // --------------------------------------------------

    @Test
    void testCopy() {
        Vec3 copy = a.cpy();
        assertEquals(a, copy);
        assertNotSame(a, copy);
    }

    @Test
    void testEqualsAndHashCode() {
        Vec3 v1 = new Vec3(1f, 2f, 3f);
        Vec3 v2 = new Vec3(1f, 2f, 3f);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void testNotEquals() {
        Vec3 v1 = new Vec3(1f, 2f, 3f);
        Vec3 v2 = new Vec3(1.1f, 2f, 3f);
        assertNotEquals(v1, v2);
    }

    // --------------------------------------------------
    // String representation
    // --------------------------------------------------

    @Test
    void testToStringFormat() {
        Vec3 v = new Vec3(1.23456f, -2.34567f, 3.45678f);
        String result = v.toString();
        assertTrue(result.startsWith("("));
        assertTrue(result.endsWith(")"));
        assertTrue(result.contains(","));
        // Rough check of values (string contains substring of printed value)
        assertTrue(result.contains("1.234") || result.contains("1.23456"));
    }
}
