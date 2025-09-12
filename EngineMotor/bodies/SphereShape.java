package bodies;// SphereShape.java

import math.*;

public final class SphereShape implements Shape {
    private final float radius;
    public SphereShape(float radius){ this.radius = radius; }
    public float getRadius(){ return radius; }
    
	@Override
	public Vec3 support(Vec3 dir){
        Vec3 d = dir.cpy();
        d.normalize();
        return Vec3.scl(d, radius);
    }
	@Override
	public AABB computeAABB(Quat orientation, Vec3 position){
        return AABB.fromSphere(position, radius);
    }
}

