package math;

import java.util.Objects;

/**
 * Simple quaternion class (w, x, y, z) using floats.
 * Methods are GC-friendly and optimized for hot paths.
 */
public final class Quat {
    private float w, x, y, z;

    public Quat() { setIdentity(); }

    /** Construct with components (w, x, y, z). */
    public Quat(float w, float x, float y, float z) {
        this.w = w; this.x = x; this.y = y; this.z = z;
    }

    // --- Getters ---
    public float getW() { return w; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    /** Set values from another quaternion */
    public Quat set(Quat q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;
        return this;
    }

    // --- Basic factory helpers ---
    public static Quat identity() {
        return new Quat(1.0f, 0f, 0f, 0f);
    }

    /**
     * Create quaternion from axis-angle (axis may be non-normalized).
     * angle is in radians.
     */
    public static Quat fromAxisAngle(Vec3 axis, float angle) {
        Vec3 n = axis.cpy().normalize();
        float half = angle * 0.5f;
        float s = (float)Math.sin(half);
        return new Quat((float)Math.cos(half), n.getX()*s, n.getY()*s, n.getZ()*s);
    }

    /** In-place: set this quaternion from axis-angle (no allocation). */
    public Quat setFromAxisAngle(math.Vec3 axis, float angle) {
        float len = axis.len();
        if (len < 1e-8f) {
            this.w = 1f; this.x = this.y = this.z = 0f;
            return this;
        }
        float half = 0.5f * angle;
        float sinH = (float)Math.sin(half);
        float cosH = (float)Math.cos(half);
        float invLen = 1.0f / len;
        float s = sinH * invLen;
        this.w = cosH;
        this.x = axis.getX() * s;
        this.y = axis.getY() * s;
        this.z = axis.getZ() * s;
        return normalize();
    }

    /** Integrate orientation from angular velocity (semi-implicit Euler). */
    public void integrateAngular(Vec3 omega, float dt) {
        float halfDt = 0.5f * dt;
        // dq/dt = 0.5 * q * ω_quat
        Quat omegaQuat = new Quat(omega.getX() * halfDt, omega.getY() * halfDt, omega.getZ() * halfDt, 0f);
        Quat dq = this.mul(omegaQuat);
        this.x += dq.x;
        this.y += dq.y;
        this.z += dq.z;
        this.w += dq.w;
        normalize();
    }

    /**
     * Converts Quaternion to rotation matrix
     * @return rotation matrix outt'a the quaternion
     */
    public Mat3 toRotationMatrix() {
    // Precompute products
    float xx = x * x;
    float yy = y * y;
    float zz = z * z;
    float xy = x * y;
    float xz = x * z;
    float yz = y * z;
    float wx = w * x;
    float wy = w * y;
    float wz = w * z;

    // Raw matrix values
    float[] m = new float[]{
        1f - 2f * (yy + zz),  2f * (xy - wz),       2f * (xz + wy),
        2f * (xy + wz),       1f - 2f * (xx + zz),  2f * (yz - wx),
        2f * (xz - wy),       2f * (yz + wx),       1f - 2f * (xx + yy)
    };

    // Clean tiny floating-point errors
    for (int i = 0; i < m.length; i++) {
        m[i] = clean(m[i]);
    }

    return new Mat3(m);
}

// Helper to snap tiny values to 0, 1, or -1
private float clean(float v) {
    float epsilon = 1e-6f;
    if (Math.abs(v) < epsilon) return 0f;
    if (Math.abs(v - 1f) < epsilon) return 1f;
    if (Math.abs(v + 1f) < epsilon) return -1f;
    return v;
}
    
    public float len2() { return w*w + x*x + y*y + z*z; }
    public float len() { return (float)Math.sqrt(len2()); }

    /** In-place normalization; returns this. */
    public Quat normalize() {
        float l2 = len2();
        if (l2 > 1e-12f) {
            float inv = 1.0f / (float)Math.sqrt(l2);
            w *= inv; x *= inv; y *= inv; z *= inv;
        } else {
            // fallback to identity if degenerate
            setIdentity();
        }
        return this;
    }

    public void setIdentity() {
    	this.w = 1f; this.x = this.y = this.z = 0f;
    }


 // --- Hamilton product (this * other) returning a new quaternion ---
    public Quat mul(Quat o) {
        float rw = w*o.w - x*o.x - y*o.y - z*o.z;
        float rx = w*o.x + x*o.w + y*o.z - z*o.y;
        float ry = w*o.y - x*o.z + y*o.w + z*o.x;
        float rz = w*o.z + x*o.y - y*o.x + z*o.w;
        return new Quat(rw, rx, ry, rz);
    }

    public Vec3 transform(Vec3 v) {
        // Rotate vector by this quaternion
        Quat qNorm = this.normalize(); // ensure unit quaternion
        Quat p = new Quat(0, v.getX(), v.getY(), v.getZ());
        Quat res = qNorm.mul(p).mul(qNorm.conjugate());
        return new Vec3(res.x, res.y, res.z);
    }

    public Vec3 invTransform(Vec3 v) {
        // Rotate vector by the inverse quaternion
        Quat qNorm = this.normalize(); // ensure unit quaternion
        Quat inv = qNorm.conjugate();
        Quat p = new Quat(0, v.getX(), v.getY(), v.getZ());
        Quat res = inv.mul(p).mul(qNorm);
        return new Vec3(res.x, res.y, res.z);
    }

    public Quat conjugate() {
        return new Quat(w, -x, -y, -z);
    }

    public Quat inverse() {
        float len2 = len2();
        if (len2 > 1e-8f) {
            return new Quat(w / len2, -x / len2, -y / len2, -z / len2);
        }
        return identity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof Quat)) {
			return false;
		}
        Quat q = (Quat) o;
        float eps = 1e-6f;
        return Math.abs(w - q.w) < eps &&
               Math.abs(x - q.x) < eps &&
               Math.abs(y - q.y) < eps &&
               Math.abs(z - q.z) < eps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.round(w*1e6f), Math.round(x*1e6f),
                            Math.round(y*1e6f), Math.round(z*1e6f));
    }


    @Override
    public String toString() {
        return String.format("Quat[w=%.6f, x=%.6f, y=%.6f, z=%.6f]", w, x, y, z);
    }
}