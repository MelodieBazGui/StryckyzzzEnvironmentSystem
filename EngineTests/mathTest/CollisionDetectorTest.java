package mathTest;

import math.*;
import bodies.SphereShape;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollisionDetectorTest {

    @Test
    void testCollisionDetected() {
        SphereShape A = new SphereShape(1f);
        SphereShape B = new SphereShape(1f);

        Quat q = new Quat();
        Vec3 pA = new Vec3(0,0,0);
        Vec3 pB = new Vec3(1.5f,0,0);

        EPA.PenetrationInfo info = CollisionDetector.detect(A, B, q, pA, q, pB);
        assertNotNull(info);
        //assertTrue(info.getDepth() > 0f);
    }

    @Test
    void testNoCollision() {
        SphereShape A = new SphereShape(1f);
        SphereShape B = new SphereShape(1f);

        Quat q = new Quat();
        Vec3 pA = new Vec3(0,0,0);
        Vec3 pB = new Vec3(3f,0,0);

        EPA.PenetrationInfo info = CollisionDetector.detect(A, B, q, pA, q, pB);
        assertNull(info);
    }
}
