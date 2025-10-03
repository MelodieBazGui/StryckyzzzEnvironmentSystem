package math;

import java.util.Locale;

/**
 * High-performance 3x3 matrix class for physics/math.
 *
 * Optimized for JVM:
 * - Uses a flat float[9] array (row-major).
 * - Methods avoid creating garbage (reuses this where possible).
 * - JIT-friendly, inlinable math ops.
 */
public final class Mat3 {

    // Row-major storage: m[row * 3 + col]
    private final float[] m = new float[9];

    // ===== Constructors =====
    public Mat3() {
        setIdentity();
    }

    public Mat3(float[] values) {
        if (values.length != 9) {
			throw new IllegalArgumentException("Mat3 requires 9 values");
		}
        System.arraycopy(values, 0, m, 0, 9);
    }

    public Mat3(Mat3 other) {
        System.arraycopy(other.m, 0, m, 0, 9);
    }

    // ===== Basic Accessors =====
    public float get(int row, int col) {
        return m[row * 3 + col];
    }

    public void set(int row, int col, float value) {
        m[row * 3 + col] = value;
    }

    public float[] raw() {
        return m;
    }

    // ===== Factory Methods =====
    public static Mat3 identity() {
        return new Mat3().setIdentity();
    }

    public static Mat3 diag(float xx, float yy, float zz) {
        return new Mat3 (new float[] {
        		xx,0,0,
        		0,yy,0,
        		0,0,zz
        });
    }

    public Mat3 setIdentity() {
        m[0] = 1; m[1] = 0; m[2] = 0;
        m[3] = 0; m[4] = 1; m[5] = 0;
        m[6] = 0; m[7] = 0; m[8] = 1;
        return this;
    }

    public static Mat3 fromRows(Vec3 r0, Vec3 r1, Vec3 r2) {
        return new Mat3(new float[]{
                r0.getX(), r0.getY(), r0.getZ(),
                r1.getX(), r1.getY(), r1.getZ(),
                r2.getX(), r2.getY(), r2.getZ()
        });
    }

    // ===== Operations =====
    public Mat3 mul(Mat3 o) {
        float[] a = this.m, b = o.m;
        float[] r = new float[9];

        // unrolled loop for speed (row-major)
        r[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
        r[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
        r[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

        r[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
        r[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
        r[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

        r[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
        r[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
        r[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

        return new Mat3(r);
    }

    public Vec3 mul(Vec3 v) {
        return new Vec3(
                m[0] * v.getX() + m[1] * v.getY() + m[2] * v.getZ(),
                m[3] * v.getX() + m[4] * v.getY() + m[5] * v.getZ(),
                m[6] * v.getX() + m[7] * v.getY() + m[8] * v.getZ()
        );
    }

    public Mat3 transpose() {
        return new Mat3(new float[]{
                m[0], m[3], m[6],
                m[1], m[4], m[7],
                m[2], m[5], m[8]
        });
    }

    public float determinant() {
        return m[0] * (m[4] * m[8] - m[5] * m[7])
             - m[1] * (m[3] * m[8] - m[5] * m[6])
             + m[2] * (m[3] * m[7] - m[4] * m[6]);
    }

    public Mat3 inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-8f) {
			throw new ArithmeticException("Singular matrix");
		}

        float invDet = 1f / det;
        float[] r = new float[9];

        r[0] = (m[4] * m[8] - m[5] * m[7]) * invDet;
        r[1] = (m[2] * m[7] - m[1] * m[8]) * invDet;
        r[2] = (m[1] * m[5] - m[2] * m[4]) * invDet;

        r[3] = (m[5] * m[6] - m[3] * m[8]) * invDet;
        r[4] = (m[0] * m[8] - m[2] * m[6]) * invDet;
        r[5] = (m[2] * m[3] - m[0] * m[5]) * invDet;

        r[6] = (m[3] * m[7] - m[4] * m[6]) * invDet;
        r[7] = (m[1] * m[6] - m[0] * m[7]) * invDet;
        r[8] = (m[0] * m[4] - m[1] * m[3]) * invDet;

        return new Mat3(r);
    }

    // ===== Convenience: rotation matrices =====
    public static Mat3 rotationX(float radians) {
        float c = (float) Math.cos(radians);
        float s = (float) Math.sin(radians);
        return new Mat3(new float[]{
                1, 0, 0,
                0, c, -s,
                0, s, c
        });
    }

    public static Mat3 rotationY(float radians) {
        float c = (float) Math.cos(radians);
        float s = (float) Math.sin(radians);
        return new Mat3(new float[]{
                c, 0, s,
                0, 1, 0,
                -s, 0, c
        });
    }

    public static Mat3 rotationZ(float radians) {
        float c = (float) Math.cos(radians);
        float s = (float) Math.sin(radians);
        return new Mat3(new float[]{
                c, -s, 0,
                s, c, 0,
                0, 0, 1
        });
    }

    public static Mat3 fromQuat(Quat q) {
        float x = q.getX();
        float y = q.getY();
        float z = q.getZ();
        float w = q.getW();

        float x2 = x + x;
        float y2 = y + y;
        float z2 = z + z;

        float xx = x * x2;
        float yy = y * y2;
        float zz = z * z2;
        float xy = x * y2;
        float xz = x * z2;
        float yz = y * z2;
        float wx = w * x2;
        float wy = w * y2;
        float wz = w * z2;

        return new Mat3(new float[]{
            1f - (yy + zz),  xy - wz,          xz + wy,
            xy + wz,         1f - (xx + zz),   yz - wx,
            xz - wy,         yz + wx,          1f - (xx + yy)
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof Mat3)) {
			return false;
		}
        Mat3 m = (Mat3) o;
        float eps = 1e-6f;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(this.get(i,j) - m.get(i,j)) > eps) {
					return false;
				}
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
            "[[%.3f, %.3f, %.3f]\n [% .3f, %.3f, %.3f]\n [% .3f, %.3f, %.3f]]",
            m[0], m[1], m[2],
            m[3], m[4], m[5],
            m[6], m[7], m[8]
        );
    }
}