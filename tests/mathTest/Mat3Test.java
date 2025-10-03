package mathTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import math.Mat3;
import math.Quat;
import math.Vec3;

class Mat3Test {

    @Test
    void testIdentity() {
        Mat3 I = Mat3.identity();
        Vec3 v = new Vec3(1, 2, 3);
        assertEquals(v, I.mul(v), "Identity matrix should not change vector");
    }

    @Test
    void testFromRows() {
        Vec3 r0 = new Vec3(1, 2, 3);
        Vec3 r1 = new Vec3(4, 5, 6);
        Vec3 r2 = new Vec3(7, 8, 9);
        Mat3 M = Mat3.fromRows(r0, r1, r2);
        assertEquals(1, M.get(0,0));
        assertEquals(5, M.get(1,1));
        assertEquals(9, M.get(2,2));
    }

    @Test
    void testMatrixMultiplication() {
        Mat3 A = Mat3.rotationZ((float) Math.PI / 2); // 90° about Z
        Vec3 x = new Vec3(1, 0, 0);
        Vec3 result = A.mul(x);
        assertEquals(0f, result.getX(), 1e-6);
        assertEquals(1f, result.getY(), 1e-6);
        assertEquals(0f, result.getZ(), 1e-6);
    }

    @Test
    void testTranspose() {
        Mat3 M = new Mat3(new float[]{
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        });
        Mat3 Mt = M.transpose();
        assertEquals(1, Mt.get(0,0));
        assertEquals(4, Mt.get(0,1));
        assertEquals(7, Mt.get(0,2));
        assertEquals(2, Mt.get(1,0));
    }

    @Test
    void testDeterminant() {
        Mat3 I = Mat3.identity();
        assertEquals(1f, I.determinant(), 1e-6);

        Mat3 zero = new Mat3(new float[]{
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        });
        assertEquals(0f, zero.determinant(), 1e-6);
    }

    @Test
    void testInverse() {
        Mat3 I = Mat3.identity();
        Mat3 invI = I.inverse();
        assertEquals(I.toString(), invI.toString());

        Mat3 R = Mat3.rotationX((float) Math.PI / 3);
        Mat3 Rinv = R.inverse();
        Mat3 shouldBeI = R.mul(Rinv);

        Vec3 v = new Vec3(1,2,3);
        Vec3 v2 = shouldBeI.mul(v);
        assertEquals(v.getX(), v2.getX(), 1e-6);
        assertEquals(v.getY(), v2.getY(), 1e-6);
        assertEquals(v.getZ(), v2.getZ(), 1e-6);
    }

    @Test
    void testRotationY() {
        Mat3 Ry = Mat3.rotationY((float)Math.PI/2); // 90° around Y
        Vec3 z = new Vec3(0, 0, 1);
        Vec3 result = Ry.mul(z);
        assertEquals(1f, result.getX(), 1e-6);
        assertEquals(0f, result.getY(), 1e-6);
        assertEquals(0f, result.getZ(), 1e-6);
    }

    @Test
    void testFromQuatMatchesRotation() {
        Quat q = Quat.fromAxisAngle(new Vec3(0,0,1), (float)Math.PI/2);
        Mat3 Rq = Mat3.fromQuat(q);

        Vec3 v = new Vec3(1,0,0);
        Vec3 result = Rq.mul(v);
        assertEquals(0f, result.getX(), 1e-6);
        assertEquals(1f, result.getY(), 1e-6);
        assertEquals(0f, result.getZ(), 1e-6);
    }
}
