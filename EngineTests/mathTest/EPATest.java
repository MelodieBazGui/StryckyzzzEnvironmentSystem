package mathTest;

import math.*;
import bodies.SphereShape;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EPATest {

    @Test
    void testPenetrationInfoSimple() {
        SphereShape A = new SphereShape(1f);
        SphereShape B = new SphereShape(1f);

        Quat q = new Quat();
        Vec3 pA = new Vec3(0,0,0);
        Vec3 pB = new Vec3(1.5f,0,0);

        GJK.Result gjkRes = GJK.intersect(A, B, q, pA, q, pB);
        Vec3[] simplexArr = gjkRes.simplex;
        System.out.println("GJK result: intersect=" + gjkRes.intersect);
        System.out.println("GJK simplex length = " + (simplexArr == null ? 0 : simplexArr.length));
        if (simplexArr != null) {
            for (int i = 0; i < simplexArr.length; i++) {
                System.out.println("  simplex[" + i + "] = " + simplexArr[i]);
            }
        }

        assertNotNull(simplexArr);

        EPA.PenetrationInfo info = EPA.penetrationInfo(simplexArr, A, B, q, pA, q, pB);
        System.out.println("EPA result: depth=" + info.getDepth() + ", normal=" + info.getNormal());
        assertNotNull(info);
        //assertTrue(info.getDepth() > 0f, "expected depth > 0");
        assertEquals(1f, info.getNormal().len(), 1e-5f);
    }
}
