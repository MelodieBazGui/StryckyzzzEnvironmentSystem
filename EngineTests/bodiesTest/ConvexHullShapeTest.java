package bodiesTest;

import bodies.*;
import math.*;
import math.algorithm.AABB;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConvexHullShapeTest {

    @Test
    void testSupport() {
        ConvexHullShape shape = new ConvexHullShape(List.of(
                new Vec3(-1, 0, 0),
                new Vec3( 1, 0, 0),
                new Vec3( 0, 1, 0)
        ));

        Vec3 dir = new Vec3(1, 0, 0);

        // Use identity rotation and origin position
        Vec3 support = shape.support(dir, Quat.identity(), new Vec3(0,0,0));

        assertEquals(1f, support.getX(), 1e-6);
        assertEquals(0f, support.getY(), 1e-6);
        assertEquals(0f, support.getZ(), 1e-6);
    }

    @Test
    void testComputeAABB() {
        ConvexHullShape shape = new ConvexHullShape(List.of(
                new Vec3(-1,-1,-1),
                new Vec3( 1, 1, 1)
        ));

        AABB box = shape.computeAABB(Quat.identity(), new Vec3(0,0,0));

        assertEquals(-1f, box.getMin().getX(), 1e-6);
        assertEquals(-1f, box.getMin().getY(), 1e-6);
        assertEquals(-1f, box.getMin().getZ(), 1e-6);

        assertEquals( 1f, box.getMax().getX(), 1e-6);
        assertEquals( 1f, box.getMax().getY(), 1e-6);
        assertEquals( 1f, box.getMax().getZ(), 1e-6);
    }
}
