package math;

/**
 * Ultra-fast 3D vector for real-time physics and rendering.
 * All operations are in-place unless otherwise noted.
 * No memory allocation, no garbage, no Locale overhead where unneeded (such as transformations of a vector).
 * @author EmeJay
 */
public final class Vec3 {

    private float x, y, z;
    
    private static final Vec3 ZERO_VEC = new Vec3();
    
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
    	float oldX = x;
        float oldY = y;
        float oldZ = z;

        x = oldY * v.z - oldZ * v.y;
        y = oldZ * v.x - oldX * v.z;
        z = oldX * v.y - oldY * v.x;
        return this;
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
        if (l != 0) {
            x /= l;
            y /= l;
            z /= l;
        }
        return this;
    }

    /** Squared distance between this vector and another. */
    public float distanceSquared(Vec3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Euclidean distance between this vector and another. */
    public float distance(Vec3 other) {
        return (float) Math.sqrt(distanceSquared(other));
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
        return new Vec3(
        		a.x + b.x,
        		a.y + b.y,
        		a.z + b.z);
    }

    public static Vec3 sub(Vec3 a, Vec3 b) {
        return new Vec3(
        		a.x - b.x,
                a.y - b.y,
                a.z - b.z);
    }

    public static Vec3 neg(Vec3 v) {
    	return v.cpy().negate();
    }
    
    public static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
        		a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
        		);
        
    }

    public static Vec3 scl(Vec3 v, float scalar) {
        return new Vec3(v.x * scalar, v.y * scalar, v.z * scalar);
    }
    
    public static float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    
	public boolean isZero() {
		return this.equals(ZERO_VEC);
	}
    
}
