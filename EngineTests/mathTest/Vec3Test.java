package mathTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import math.*;

import static org.junit.jupiter.api.Assertions.*;

class Vec3Test {

    private Vec3 a;
    private Vec3 b;
    private Mat3 identity;

    @BeforeEach
    void setUp() {
        a = new Vec3(1f, 2f, 3f);
        b = new Vec3(4f, -5f, 6f);
        identity = Mat3.identity();
    }

    @AfterEach
    void tearDown() {
        a = null;
        b = null;
        identity = null;
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
        copy = null;
    }

    @Test
    void testStaticAdd() {
        Vec3 result = Vec3.add(a, b);
        assertEquals(5f, result.getX(), 1e-6f);
        assertEquals(-3f, result.getY(), 1e-6f);
        assertEquals(9f, result.getZ(), 1e-6f);
        result = null;
    }

    @Test
    void testDotProduct() {
        float dot = a.dot(b);
        assertEquals(12f, dot, 1e-6f); // (1*4 + 2*-5 + 3*6) = 4 - 10 + 18*
    }

    @Test
    void testCrossProduct() {
        Vec3 xAxis = new Vec3(1f, 0f, 0f);
        Vec3 yAxis = new Vec3(0f, 1f, 0f);
        Vec3 cross = xAxis.cross(yAxis);
        assertEquals(0f, cross.getX(), 1e-6f);
        assertEquals(0f, cross.getY(), 1e-6f);
        assertEquals(1f, cross.getZ(), 1e-6f);
        
        xAxis = null; 
        yAxis = null; 
        cross = null;
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
        
        v = null;
    }

    @Test
    void testIdentityMatrix() {
        assertEquals(1f, identity.get(0,0), 1e-6f);
        assertEquals(1f, identity.get(1,1), 1e-6f);
        assertEquals(1f, identity.get(2,2), 1e-6f);

        assertEquals(0f, identity.get(0,1), 1e-6f);
        assertEquals(0f, identity.get(0,2), 1e-6f);
        assertEquals(0f, identity.get(1,0), 1e-6f);
    }

    @Test
    void testMulByIdentity() {
        Mat3 m = Mat3.rotationX((float)Math.PI / 4f);
        Mat3 result = m.mul(identity);
        assertEquals(m.toString(), result.toString()); // same layout
        m = null;
        result = null;
    }

    @Test
    void testVecMul() {
        Vec3 v = new Vec3(1, 0, 0);
        Mat3 rot = Mat3.rotationY((float)Math.PI / 2f); // rotate 90° around Y
        Vec3 rotated = rot.mul(v);
        assertEquals(0f, rotated.getX(), 1e-5f);
        assertEquals(0f, rotated.getY(), 1e-5f);
        assertEquals(-1f, rotated.getZ(), 1e-5f);
        v = null;
        rot = null;
        rotated = null;
    }

    @Test
    void testNeg() {
        Vec3 v = new Vec3(1f, -2f, 3f);
        Vec3 n = v.neg();
        assertEquals(-1f, n.getX(), 1e-6);
        assertEquals( 2f, n.getY(), 1e-6);
        assertEquals(-3f, n.getZ(), 1e-6);

        // original must remain unchanged
        assertEquals(1f, v.getX(), 1e-6);
        assertEquals(-2f, v.getY(), 1e-6);
        assertEquals(3f, v.getZ(), 1e-6);
        n = null;
        v = null;
    }

    @Test
    void testNegStatic() {
    	Vec3 v = new Vec3(1f, -2f, 3f);
        Vec3 neg = Vec3.neg(v);

        assertEquals(-1f, neg.getX(), 1e-6);
        assertEquals( 2f, neg.getY(), 1e-6);
        assertEquals(-3f, neg.getZ(), 1e-6);

        // Ensure original vector unchanged
        assertEquals(1f, v.getX(), 1e-6);
        assertEquals(-2f, v.getY(), 1e-6);
        assertEquals(3f, v.getZ(), 1e-6);
        
        v = null;
        neg = null;
    }
    
    @Test
    void testTranspose() {
        Mat3 m = new Mat3(new float[]{
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        });
        Mat3 t = m.transpose();
        assertEquals(1f, t.get(0,0), 1e-6f);
        assertEquals(4f, t.get(0,1), 1e-6f);
        assertEquals(7f, t.get(0,2), 1e-6f);
        assertEquals(2f, t.get(1,0), 1e-6f);
        assertEquals(5f, t.get(1,1), 1e-6f);
    }

    @Test
    void testDeterminantAndInverse() {
        Mat3 m = new Mat3(new float[]{
            4, 7, 2,
            3, 6, 1,
            2, 5, 3
        });
        float det = m.determinant();
        assertNotEquals(0f, det);

        Mat3 inv = m.inverse();
        Mat3 product = m.mul(inv);

        // should be close to identity
        assertEquals(1f, product.get(0,0), 1e-4f);
        assertEquals(0f, product.get(0,1), 1e-4f);
        assertEquals(0f, product.get(0,2), 1e-4f);
        assertEquals(0f, product.get(1,0), 1e-4f);
        assertEquals(1f, product.get(1,1), 1e-4f);
    }

    @Test
    void testRotationX() {
        Mat3 rot = Mat3.rotationX((float)Math.PI / 2f);
        Vec3 v = new Vec3(0, 1, 0);
        Vec3 rotated = rot.mul(v);
        // Y-up rotated 90° around X -> Z-up
        assertEquals(0f, rotated.getX(), 1e-5f);
        assertEquals(0f, rotated.getY(), 1e-5f);
        assertEquals(1f, rotated.getZ(), 1e-5f);
    }

    @Test
    void testRotationY() {
        Mat3 rot = Mat3.rotationY((float)Math.PI / 2f);
        Vec3 v = new Vec3(0, 0, 1);
        Vec3 rotated = rot.mul(v);
        // Z-forward rotated 90° around Y -> X-forward
        assertEquals(1f, rotated.getX(), 1e-5f);
        assertEquals(0f, rotated.getY(), 1e-5f);
        assertEquals(0f, rotated.getZ(), 1e-5f);
    }

    @Test
    void testRotationZ() {
        Mat3 rot = Mat3.rotationZ((float)Math.PI / 2f);
        Vec3 v = new Vec3(1, 0, 0);
        Vec3 rotated = rot.mul(v);
        // X-forward rotated 90° around Z -> Y-forward
        assertEquals(0f, rotated.getX(), 1e-5f);
        assertEquals(1f, rotated.getY(), 1e-5f);
        assertEquals(0f, rotated.getZ(), 1e-5f);
    }

    @Test
    void testToRotationMatrixFromQuat() {
        Quat q = Quat.fromAxisAngle(new Vec3(0,1,0), (float)Math.PI / 2f);
        Mat3 rot = Mat3.fromQuat(q);
        Vec3 v = new Vec3(1, 0, 0);
        Vec3 rotated = rot.mul(v);
        // X-forward rotated 90° around Y -> Z-back
        assertEquals(0f, rotated.getX(), 1e-5f);
        assertEquals(0f, rotated.getY(), 1e-5f);
        assertEquals(-1f, rotated.getZ(), 1e-5f);
    }

    @Test
    void testFromRows() {
        Vec3 r0 = new Vec3(1,0,0);
        Vec3 r1 = new Vec3(0,1,0);
        Vec3 r2 = new Vec3(0,0,1);
        Mat3 m = Mat3.fromRows(r0, r1, r2);
        assertEquals(1f, m.get(0,0), 1e-6f);
        assertEquals(1f, m.get(1,1), 1e-6f);
        assertEquals(1f, m.get(2,2), 1e-6f);
    }
    
    @Test
    void testEqualsAndHashCodeEqualObjects() {
        Vec3 v1 = new Vec3(1f, 2f, 3f);
        Vec3 v2 = new Vec3(1.0000001f, 2.0000001f, 3.0000001f); // within epsilon

        assertEquals(v1, v2, "Vectors with nearly identical components should be equal");
        assertEquals(v1.hashCode(), v2.hashCode(), "Equal vectors must have same hashCode");
    }

    @Test
    void testEqualsAndHashCodeDifferentObjects() {
        Vec3 v1 = new Vec3(1f, 2f, 3f);
        Vec3 v2 = new Vec3(4f, 5f, 6f);

        assertNotEquals(v1, v2, "Different vectors should not be equal");
        assertNotEquals(v1.hashCode(), v2.hashCode(), "Likely different hashCodes");
    }

    @Test
    void testEqualsSelf() {
        assertEquals(a, a, "Vector should be equal to itself");
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(a, null, "Vector should not be equal to null");
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals(a, "(1,2,3)", "Vector should not be equal to an unrelated type");
    }


    @Test
    void testToStringFormat() {
        Mat3 m = Mat3.identity();
        String s = m.toString();

        assertNotNull(s);
        assertTrue(s.startsWith("[["), "Matrix string should start with '[['");
        assertTrue(s.endsWith("]]"), "Matrix string should end with ']]'");

        // Split rows based on closing + opening brackets
        String[] rows = s.split("\\], \\[");
        assertEquals(3, rows.length, "Matrix string should have 3 rows");

        // Each row should have 3 values
        for (String row : rows) {
            String cleaned = row.replace("[", "").replace("]", "");
            String[] values = cleaned.split(",");
            assertEquals(3, values.length, "Each row should have 3 columns");
        }
    }

    
    @Test
    void testToStringMetricFormat() {
        String s = a.toString();
        assertTrue(s.matches("\\(1[,.]000, 2[,.]000, 3[,.]000\\)"),
                   "toString should format components with 3 decimals");
    }
}
