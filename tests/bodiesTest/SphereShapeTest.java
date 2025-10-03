package bodiesTest;

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bodies.SphereShape;
import math.Quat;
import math.Vec3;
import math.algorithm.AABB;

public class SphereShapeTest {

    private SphereShape sphereFromVec;
    private SphereShape sphereFromRadius;
    private Quat identity;

    @BeforeEach
    void setUp() {
        sphereFromVec = new SphereShape(new Vec3(3f, 4f, 0f)); // radius = 5
        sphereFromRadius = new SphereShape(2.5f);
        identity = Quat.identity();
    }

    @AfterEach
    void tearDown() {
        sphereFromVec = null;
        sphereFromRadius = null;
        identity = null;
    }

    @Test
    void testConstructorFromVec3() {
        assertEquals(5f, sphereFromVec.getRadius(), 1e-6f);
        assertEquals(new Vec3(3f, 4f, 0f), sphereFromVec.getCenter());
    }

    @Test
    void testConstructorFromRadius() {
        assertEquals(2.5f, sphereFromRadius.getRadius(), 1e-6f);
        assertEquals(new Vec3(0f, 0f, 0f), sphereFromRadius.getCenter());
    }

    @Test
    void testSupportInDirection() {
        SphereShape sphere = new SphereShape(2f);
        Vec3 dir = new Vec3(1f, 0f, 0f);
        Vec3 pos = new Vec3(0f, 0f, 0f);

        Vec3 support = sphere.support(dir, identity, pos);

        assertEquals(new Vec3(2f, 0f, 0f), support);
        sphere = null; dir = null; pos = null; support = null;
    }

    @Test
    void testSupportWithOffset() {
        SphereShape sphere = new SphereShape(1f);
        Vec3 dir = new Vec3(0f, 1f, 0f);
        Vec3 pos = new Vec3(5f, 5f, 5f);

        Vec3 support = sphere.support(dir, identity, pos);

        assertEquals(new Vec3(5f, 6f, 5f), support);
        sphere = null; dir = null; pos = null; support = null;
    }

    @Test
    void testSupportDegenerateDirection() {
        SphereShape sphere = new SphereShape(1f);
        Vec3 dir = new Vec3(0f, 0f, 0f);
        Vec3 pos = new Vec3(2f, 3f, 4f);

        Vec3 support = sphere.support(dir, identity, pos);

        assertEquals(pos, support);
        assertNotSame(pos, support); // should be a copy
        sphere = null; dir = null; pos = null; support = null;
    }

    @Test
    void testComputeAABB() {
        SphereShape sphere = new SphereShape(2f);
        Vec3 pos = new Vec3(1f, 2f, 3f);

        AABB aabb = sphere.computeAABB(identity, pos);

        assertEquals(new Vec3(-1f, 0f, 1f), aabb.getMin());
        assertEquals(new Vec3(3f, 4f, 5f), aabb.getMax());
        sphere = null; pos = null; aabb = null;
    }
}
