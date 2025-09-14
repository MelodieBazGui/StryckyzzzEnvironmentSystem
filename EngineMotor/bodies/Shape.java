package bodies;

import math.*;

//Shape.java
public interface Shape {
 /**
  * Support function for GJK:
  * Returns the furthest point on the shape in the given direction.
  *
  * @param dir Direction in world space
  * @param rot Orientation of the shape
  * @param pos Position of the shape in world space
  * @return Support point in world space
  */
 Vec3 support(Vec3 dir, Quat rot, Vec3 pos);

 /**
  * Compute the world-space axis-aligned bounding box for broadphase.
  */
 AABB computeAABB(Quat orientation, Vec3 position);

 /**
  * Compute inertia tensor (body-space).
  * @param mass mass of the body
  * @return inertia tensor in local space
  */
 Mat3 computeInertia(float mass);
}
