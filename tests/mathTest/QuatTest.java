package mathTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import math.Mat3;
import math.Quat;
import math.Vec3;

class QuatTest {

    private Quat q;
    private Vec3 axis;

    @BeforeEach
    void setUp() {
        axis = new Vec3(0, 1, 0); // Y-axis*
        q = new Quat(0f, 0f, 0f, 1f);
        q = Quat.fromAxisAngle(axis, (float) Math.toRadians(90)); // 90° around Y
    }

    @AfterEach
    void tearDown() {
        q = null;
        axis = null;
    }

    @Test
    void testIdentity() {
        Quat id = Quat.identity();
        assertEquals(0f, id.getX(), 1e-6);
        assertEquals(0f, id.getY(), 1e-6);
        assertEquals(0f, id.getZ(), 1e-6);
        assertEquals(1f, id.getW(), 1e-6);
    }

    @Test
    void testFromAxisAngleNormalization() {
        // Axis is normalized internally, quaternion should have unit length
        assertEquals(1f, q.len(), 1e-6);
    }

    @Test
    void testConjugate() {
        Quat conj = q.conjugate();
        assertEquals(-q.getX(), conj.getX(), 1e-6);
        assertEquals(-q.getY(), conj.getY(), 1e-6);
        assertEquals(-q.getZ(), conj.getZ(), 1e-6);
        assertEquals(q.getW(), conj.getW(), 1e-6);
    }

    @Test
    void testMul() {
        Quat id = Quat.identity();
        Quat result = q.mul(id);
        // Multiplying by identity should leave quaternion unchanged
        assertEquals(q.getX(), result.getX(), 1e-6);
        assertEquals(q.getY(), result.getY(), 1e-6);
        assertEquals(q.getZ(), result.getZ(), 1e-6);
        assertEquals(q.getW(), result.getW(), 1e-6);
    }

    @Test
    void testTransformVector() {
        Vec3 v = new Vec3(1, 0, 0); // along X
        Vec3 rotated = q.transform(v); // rotate 90° around Y
        assertEquals(0f, rotated.getX(), 1e-5);
        assertEquals(0f, rotated.getY(), 1e-5);
        assertEquals(-1f, rotated.getZ(), 1e-5);
    }

    @Test
    void testInvTransform() {
        Quat q = Quat.fromAxisAngle(new Vec3(0, 1, 0), (float)Math.PI / 2f); // 90° Y-rotation
        Vec3 v = new Vec3(1, 0, 0);

        // Forward rotation should map X→ -Z (right-handed rule)
        Vec3 rotated = q.transform(v);
        assertEquals(0f, rotated.getX(), 1e-6);
        assertEquals(0f, rotated.getY(), 1e-6);
        assertEquals(-1f, rotated.getZ(), 1e-6);

        // Inverse rotation should bring it back to X
        Vec3 unrotated = q.invTransform(rotated);
        assertEquals(1f, unrotated.getX(), 1e-6);
        assertEquals(0f, unrotated.getY(), 1e-6);
        assertEquals(0f, unrotated.getZ(), 1e-6);
    }

    @Test
    void testToRotationMatrixIdentity() {
    	q.normalize();
    	Mat3 m = q.toRotationMatrix();

    	// Diagonal should be ~1
    	for (int i = 0; i < 3; i++) {
    	    assertEquals(1f, m.get(i, i), 1e-5f);
    	}

    	// Off-diagonal should be ~0
    	for (int i = 0; i < 3; i++) {
    	    for (int j = 0; j < 3; j++) {
    	        if (i != j) assertEquals(0f, m.get(i, j), 1e-5f);
    	    }
    	}
    }

    @Test
    void testToRotationMatrixFromAxisAngle() {
        // 90° rotation around Y
        Quat rot = Quat.fromAxisAngle(new Vec3(0,1,0), (float)Math.PI / 2f);
        Mat3 m = rot.toRotationMatrix();

        Vec3 x = new Vec3(1,0,0);
        Vec3 rotated = m.mul(x);

        // X-axis rotated around Y should become Z-axis
        assertEquals(0f, rotated.getX(), 1e-5f);
        assertEquals(0f, rotated.getY(), 1e-5f);
        assertEquals(-1f, rotated.getZ(), 1e-5f);
    }

    @Test
    void testIntegrateAngular() {
        Vec3 omega = new Vec3(0f, 1f, 0f); // rotate around Y
        float dt = (float)Math.PI / 2f;    // 90° over one step

        q.integrateAngular(omega, dt);
        Mat3 m = q.toRotationMatrix();

        Vec3 x = new Vec3(1,0,0);
        Vec3 rotated = m.mul(x);

        // After integration, X-axis should rotate to Z-axis
        assertEquals(0f, rotated.getX(), 1e-4f);
        assertEquals(0f, rotated.getY(), 1e-4f);
        assertEquals(-1f, rotated.getZ(), 1e-4f);
    }

    @Test
    void testNormalize() {
        Quat unnormalized = new Quat(2, 0, 0, 0);
        Quat norm = unnormalized.normalize();
        assertEquals(1f, norm.len(), 1e-6);
    }

    @Test
    void testLenAndLen2() {
        Quat q2 = new Quat(1, 2, 3, 4);
        float len2 = 1*1 + 2*2 + 3*3 + 4*4;
        assertEquals(len2, q2.len2(), 1e-6);
        assertEquals((float)Math.sqrt(len2), q2.len(), 1e-6);
    }
}

