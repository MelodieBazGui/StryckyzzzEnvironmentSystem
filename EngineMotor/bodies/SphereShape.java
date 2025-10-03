package bodies;// SphereShape.java

import math.Mat3;
import math.Quat;
import math.Vec3;
import math.algorithm.AABB;

public final class SphereShape implements Shape {
	private final Vec3 center;
    private final float radius;

    // Constructor using a Vec3
    public SphereShape(Vec3 vec) {
        this.center = vec.cpy();   // keep the vector as center
        this.radius = vec.len();   // use its magnitude as radius
    }

    public Vec3 getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }

    public SphereShape(float radius) {
        this.center = new Vec3();
		this.radius = radius;
    }

    @Override
    public Vec3 support(Vec3 dir, Quat rot, Vec3 pos) {
        if (dir.len2() == 0f) {
            return pos.cpy(); // degenerate case
        }
        Vec3 d = dir.cpy().normalize();   // safe copy + normalize
        return pos.add(d.scl(radius));
    }

    @Override
    public AABB computeAABB(Quat orientation, Vec3 position) {
        Vec3 r = new Vec3(radius, radius, radius);
        return new AABB(Vec3.sub(position, r), Vec3.add(position, r));
    }

    @Override
    public Mat3 computeInertia(float mass) {
        float i = 0.4f * mass * radius * radius; // (2/5) m r^2
        return Mat3.diag(i, i, i);
    }
}
