package bodies;

import math.Mat3;
import math.Quat;
import math.Vec3;
import math.algorithm.AABB;

//Shape.java
/**
 * @author EmeJay
 */
public abstract class Shape {
	
	 /**
	  * Support function for GJK:
	  * Returns the furthest point on the shape in the given direction.
	  *
	  * @param dir Direction in world space
	  * @param rot Orientation of the shape
	  * @param pos Position of the shape in world space
	  * @return Support point in world space
	  */
	abstract Vec3 support(Vec3 dir, Quat rot, Vec3 pos);
	
	 /**
	  * Compute the world-space axis-aligned bounding box for broadphase.
	  */
	abstract AABB computeAABB(Quat orientation, Vec3 position);
	
	 /**
	  * Compute inertia tensor (body-space).
	  * @param mass mass of the body
	  * @return inertia tensor in local space
	  */
	abstract Mat3 computeInertia(float mass);

	abstract Vec3 getPosition();
	
	abstract float getInvMass();

}
