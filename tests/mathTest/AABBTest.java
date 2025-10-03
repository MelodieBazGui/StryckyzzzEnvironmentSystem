package mathTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import math.Vec3;
import math.algorithm.AABB;

class AABBTest {

    @Test
    void testContains() {
        AABB box = new AABB(new Vec3(0, 0, 0), new Vec3(1, 1, 1));
        assertTrue(box.contains(new Vec3(0.5f, 0.5f, 0.5f)));
        assertFalse(box.contains(new Vec3(2f, 0f, 0f)));
    }

    @Test
    void testExpand() {
        AABB box = new AABB(new Vec3(0, 0, 0), new Vec3(1, 1, 1));
        AABB expanded = box.expand(1f);
        assertEquals(-1f, expanded.getMin().getX(), 1e-6);
        assertEquals( 2f, expanded.getMax().getX(), 1e-6);
    }

    @Test
    void testContainsNoVector() {
        AABB box = new AABB(new Vec3(0,0,0), new Vec3(10,10,10));

        assertTrue(box.contains(new Vec3(5,5,5)));    // inside
        assertTrue(box.contains(new Vec3(0,0,0)));    // on min corner
        assertTrue(box.contains(new Vec3(10,10,10))); // on max corner

        assertFalse(box.contains(new Vec3(-1,5,5)));  // outside X
        assertFalse(box.contains(new Vec3(5,11,5)));  // outside Y
        assertFalse(box.contains(new Vec3(5,5,12)));  // outside Z
    }

    @Test
    void testOverlap() {
        AABB a = new AABB(new Vec3(0,0,0), new Vec3(1,1,1));
        AABB b = new AABB(new Vec3(0.5f,0.5f,0.5f), new Vec3(1.5f,1.5f,1.5f));
        AABB c = new AABB(new Vec3(2,2,2), new Vec3(3,3,3));
        assertTrue(a.overlaps(b));
        assertFalse(a.overlaps(c));
    }
}
