package bodies;

import math.*;

//Shape.java
public interface Shape {
 // support function for GJK: returns point on shape farthest in direction d
 Vec3 support(Vec3 dir);

 // AABB for broadphase
 AABB computeAABB(Quat orientation, Vec3 position);
}
