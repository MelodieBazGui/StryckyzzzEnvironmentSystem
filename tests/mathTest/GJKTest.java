package mathTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bodies.BoxShape;
import bodies.Shape;
import bodies.SphereShape;
import math.Quat;
import math.Vec3;
import math.algorithm.GJK;

public class GJKTest {

    private static final Quat ID = new Quat(0, 0, 0, 1);
	private Shape b1;
	private Shape b2;

    @BeforeEach
    public void beforeEach() {
    	b1 = new BoxShape(1f,1f,1f,1f);
    	b2 = new BoxShape(new Vec3(2f,1f,1f),1f);
    }
    
    @AfterEach
    public void afterEach() {
    	b1 = null;
    	b2 = null;
    }
    
    @Test
    public void testSphereSeparated() {
        SphereShape s1 = new SphereShape(1f);
        SphereShape s2 = new SphereShape(1f);

        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(3,0,0); // centers 3 apart, radii 1+1=2, so no overlap

        GJK.Result res = GJK.intersect(s1, s2, ID, p1, ID, p2);
        assertFalse(res.intersect, "Spheres separated by >2 should not intersect");
    }

    @Test
    public void testSphereTouching() {
        SphereShape s1 = new SphereShape(1f);
        SphereShape s2 = new SphereShape(1f);

        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(2,0,0); // exactly touching at x=1

        GJK.Result res = GJK.intersect(s1, s2, ID, p1, ID, p2);
        assertTrue(res.intersect, "Spheres touching should intersect (touch counts as collision)");
    }

    @Test
    public void testSphereOverlapping() {
        Shape s1 = new SphereShape(1f);
        Shape s2 = new SphereShape(1f);

        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(1,0,0); // centers 1 apart, overlap

        GJK.Result res = GJK.intersect(s1, s2, ID, p1, ID, p2);
        assertTrue(res.intersect, "Spheres overlapping should intersect");
    }

    @Test
    public void testBoxSeparated() {

        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(5,0,0);

        GJK.Result res = GJK.intersect(b1, b2, ID, p1, ID, p2);
        assertFalse(res.intersect, "Boxes far apart should not intersect");
    }

    @Test
    public void testBoxOverlap() {
        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(1.5f,0,0); // overlap (boxes extend to ±1 and [0.5..2.5])

        GJK.Result res = GJK.intersect(b1, b2, ID, p1, ID, p2);
        assertTrue(res.intersect, "Boxes overlapping should intersect");
    }

    @Test
    public void testBoxTouching() {

        Vec3 p1 = new Vec3(0,0,0);
        Vec3 p2 = new Vec3(2,0,0); // touching along face

        GJK.Result res = GJK.intersect(b1, b2, ID, p1, ID, p2);
        assertTrue(res.intersect, "Boxes touching along face should intersect");
    }
}
