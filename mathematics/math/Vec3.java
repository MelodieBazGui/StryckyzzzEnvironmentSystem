package math;

/**
 * Ultra-fast 3D vector for real-time physics and rendering.
 * All operations are in-place unless otherwise noted.
 * No memory allocation, no garbage, no Locale overhead.
 * @author EmeJay
 */
public final class Vec3 {

    private float x, y, z;

    // -------------------------
    // Constructors
    // -------------------------
    public Vec3() {
        this(0f, 0f, 0f);
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	// -------------------------
    // Basic Operations (in-place)
    // -------------------------
    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 set(Vec3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vec3 add(Vec3 v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vec3 sub(Vec3 v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public Vec3 scl(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Vec3 negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3 zero() {
        x = y = z = 0f;
        return this;
    }

    // -------------------------
    // Math helpers
    // -------------------------
    public float dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vec3 cross(Vec3 v) {
        float cx = y * v.z - z * v.y;
        float cy = z * v.x - x * v.z;
        float cz = x * v.y - y * v.x;
        return set(cx, cy, cz);
    }

    public float len2() {
        return x * x + y * y + z * z;
    }

    public float len() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /** Normalizes this vector using a fast inverse sqrt approximation. 
     * Beware as this results in a 1e-2f delta (around that for small numbers)
     * 
     * */
    public Vec3 normalizeFast() {
        float len2 = len2();
        if (len2 < 1e-12f) return this;
        float invLen = fastInvSqrt(len2);
        x *= invLen;
        y *= invLen;
        z *= invLen;
        return this;
    }

    /** Standard normalization (slower but more precise). */
    public Vec3 normalize() {
        float l = len();
        if (l > 1e-8f) {
            float inv = 1f / l;
            x *= inv;
            y *= inv;
            z *= inv;
        }
        return this;
    }

    // -------------------------
    // Utility methods
    // -------------------------
    public Vec3 cpy() {
        return new Vec3(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3)) return false;
        Vec3 v = (Vec3) o;
        return Math.abs(x - v.x) < 1e-6f &&
               Math.abs(y - v.y) < 1e-6f &&
               Math.abs(z - v.z) < 1e-6f;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + Float.floatToIntBits(x);
        h = 31 * h + Float.floatToIntBits(y);
        h = 31 * h + Float.floatToIntBits(z);
        return h;
    }

    @Override
    public String toString() {
        // Fast, allocation-free — no Locale
        return "(" + x + ", " + y + ", " + z + ")";
    }

    // -------------------------
    // Fast Math Core
    // -------------------------

    /**
     * Quake III fast inverse square root.
     * Approximate 1/sqrt(x) using bit-level hack.
     * ~4x faster than Math.sqrt() with minor precision loss.
     */
    private static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= (1.5f - xhalf * x * x); // One iteration of Newton’s method
        return x;
    }

    // -------------------------
    // Static Utility (no allocs)
    // -------------------------
    public static Vec3 add(Vec3 a, Vec3 b) {
    	Vec3 out = new Vec3();
        out.x = a.x + b.x;
        out.y = a.y + b.y;
        out.z = a.z + b.z;
        return out;
    }

    public static Vec3 sub(Vec3 a, Vec3 b) {
    	Vec3 out = new Vec3();
        out.x = a.x - b.x;
        out.y = a.y - b.y;
        out.z = a.z - b.z;
        return out;
    }

    public static Vec3 scl(Vec3 a, float s) {
    	Vec3 out = new Vec3();
        out.x = a.x * s;
        out.y = a.y * s;
        out.z = a.z * s;
        return out;
    }

    public static Vec3 cross(Vec3 a, Vec3 b) {
    	Vec3 out = new Vec3();
        out.x = a.y * b.z - a.z * b.y;
        out.y = a.z * b.x - a.x * b.z;
        out.z = a.x * b.y - a.y * b.x;
        return out;
    }

    public static float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
}
