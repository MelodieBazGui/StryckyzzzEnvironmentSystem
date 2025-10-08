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
	
	private float invMass;
	private Vec3 position;

	/**
	  * Support function for GJK:
	  * Returns the furthest point on the shape in the given direction.
	  *
	  * @param dir Direction in world space
	  * @param rot Orientation of the shape
	  * @param pos Position of the shape in world space
	  * @return Support point in world space
	  */
	public abstract Vec3 support(Vec3 dir, Quat rot, Vec3 pos);
	
	 /**
	  * Compute the world-space axis-aligned bounding box for broadphase.
	  */
	public abstract AABB computeAABB(Quat orientation, Vec3 position);
	
	 /**
	  * Compute inertia tensor (body-space).
	  * @param mass mass of the body
	  * @return inertia tensor in local space
	  */
	public abstract Mat3 computeInertia(float mass);
	
	public float getInvMass() {
		return invMass;
	};

	public Vec3 getPosition() {
		return position;
	}

	public void setInvMass(float invMass) {
		this.invMass = invMass;
	}

	public void setPosition(Vec3 position) {
		this.position = position;
	}


}
