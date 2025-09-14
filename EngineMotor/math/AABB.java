package math;

public final class AABB {
    private final Vec3 min;
    private final Vec3 max;

    public AABB(Vec3 min, Vec3 max){
        this.min = min.cpy();
        this.max = max.cpy();
    }

    public Vec3 getMin(){ return min.cpy(); }
    public Vec3 getMax(){ return max.cpy(); }

    public boolean overlaps(AABB o){
        return !(max.getX() < o.min.getX() || min.getX() > o.max.getX()
              || max.getY() < o.min.getY() || min.getY() > o.max.getY()
              || max.getZ() < o.min.getZ() || min.getZ() > o.max.getZ());
    }

    // create AABB for sphere (center, radius)
    public static AABB fromSphere(Vec3 center, float radius){
        Vec3 r = new Vec3(radius, radius, radius);
        return new AABB(Vec3.sub(center, r), Vec3.add(center, r));
    }

    // expand by margin in-place? returns new
    public AABB expand(float margin){
        Vec3 m = new Vec3(margin, margin, margin);
        return new AABB(Vec3.sub(min, m), Vec3.add(max, m));
    }
    
    /**
     * Checks whether a point lies inside (or on the boundary of) this AABB.
     */
    public boolean contains(Vec3 p) {
        return contains(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Faster overload, avoids creating Vec3s.
     */
    public boolean contains(float x, float y, float z) {
        return (x >= min.getX() && x <= max.getX()) &&
               (y >= min.getY() && y <= max.getY()) &&
               (z >= min.getZ() && z <= max.getZ());
    }

    @Override
    public String toString() {
        return "AABB[min=" + min + ", max=" + max + "]";
    }
}