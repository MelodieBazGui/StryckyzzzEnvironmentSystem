package mathTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import math.Vec3;

import static org.junit.jupiter.api.Assertions.*;

class Vec3Test {

    private Vec3 a;
    private Vec3 b;

    @BeforeEach
    void setUp() {
        a = new Vec3(1f, 2f, 3f);
        b = new Vec3(4f, -5f, 6f);
    }

    @AfterEach
    void tearDown() {
        a = null;
        b = null;
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(1f, a.getX(), 1e-6f);
        assertEquals(2f, a.getY(), 1e-6f);
        assertEquals(3f, a.getZ(), 1e-6f);
    }

    @Test
    void testAddInPlace() {
        a.add(new Vec3(4f, -2f, 1f));
        assertEquals(5f, a.getX(), 1e-6f);
        assertEquals(0f, a.getY(), 1e-6f);
        assertEquals(4f, a.getZ(), 1e-6f);
    }

    @Test
    void testSubInPlace() {
        a.sub(new Vec3(2f, 3f, 1f));
        assertEquals(-1f, a.getX(), 1e-6f);
        assertEquals(-1f, a.getY(), 1e-6f);
        assertEquals(2f, a.getZ(), 1e-6f);
    }

    @Test
    void testScaleInPlace() {
        a.scl(2f);
        assertEquals(2f, a.getX(), 1e-6f);
        assertEquals(4f, a.getY(), 1e-6f);
        assertEquals(6f, a.getZ(), 1e-6f);
    }

    @Test
    void testCopy() {
        Vec3 copy = a.cpy();
        assertNotSame(a, copy);
        assertEquals(a.getX(), copy.getX(), 1e-6f);
        assertEquals(a.getY(), copy.getY(), 1e-6f);
        assertEquals(a.getZ(), copy.getZ(), 1e-6f);
    }

    @Test
    void testStaticAdd() {
        Vec3 result = Vec3.add(a, b);
        assertEquals(5f, result.getX(), 1e-6f);
        assertEquals(-3f, result.getY(), 1e-6f);
        assertEquals(9f, result.getZ(), 1e-6f);
    }

    @Test
    void testDotProduct() {
        float dot = a.dot(b);
        assertEquals(12f, dot, 1e-6f); // (1*4 + 2*-5 + 3*6) = 4 - 10 + 18
    }

    @Test
    void testCrossProduct() {
        Vec3 xAxis = new Vec3(1f, 0f, 0f);
        Vec3 yAxis = new Vec3(0f, 1f, 0f);
        Vec3 cross = xAxis.cross(yAxis);
        assertEquals(0f, cross.getX(), 1e-6f);
        assertEquals(0f, cross.getY(), 1e-6f);
        assertEquals(1f, cross.getZ(), 1e-6f);
    }

    @Test
    void testLengthAndNormalization() {
        Vec3 v = new Vec3(3f, 4f, 0f);
        assertEquals(25f, v.len2(), 1e-6f);
        assertEquals(5f, v.len(), 1e-6f);

        v.normalize();
        assertEquals(1f, v.len(), 1e-6f);
        assertEquals(0.6f, v.getX(), 1e-6f);
        assertEquals(0.8f, v.getY(), 1e-6f);
    }

    @Test
    void testToStringMetricFormat() {
        String s = a.toString();
        assertEquals("(1,000, 2,000, 3,000)", s);
    }
}
