package mathTest;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import bodies.SphereShape;
import math.Quat;
import math.Vec3;
import math.absurd.CollisionDetector;
import math.algorithm.EPA;

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
