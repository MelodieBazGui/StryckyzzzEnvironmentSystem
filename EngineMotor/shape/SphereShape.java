// SphereShape.java
public final class SphereShape implements Shape {
    private final float radius;
    public SphereShape(float radius){ this.radius = radius; }
    public float getRadius(){ return radius; }
    @Override public Vec3 support(Vec3 dir){
        Vec3 d = dir.copy();
        d.normalize();
        return Vec3.scl(d, radius);
    }
    @Override public AABB computeAABB(Quat orientation, Vec3 position){
        return AABB.fromSphere(position, radius);
    }
}
package shape;

public class SphereShape {

}
