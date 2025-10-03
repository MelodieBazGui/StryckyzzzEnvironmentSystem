package engine;

import math.Vec3;

/**
 * Represents a collision contact between two rigid bodies.
 *
 * Stores:
 * - IDs of colliding bodies
 * - contact point in world space
 * - contact normal (unit vector pointing from A → B)
 * - penetration depth (overlap distance)
 * - friction parameters (static & dynamic)
 * - optional tangent directions for friction impulses
 * @author EmeJay
 */
public final class Contact {
    public final int a;              // body A id
    public final int b;              // body B id
    public final Vec3 point;         // world-space contact point
    public final Vec3 normal;        // world-space collision normal (unit vector)
    public final float penetration;  // overlap distance

    // Friction coefficients
    public final float staticFriction;
    public final float dynamicFriction;

    // Cached tangent directions (orthogonal to normal), computed lazily
    private Vec3 tangentU;
    private Vec3 tangentV;

    public Contact(int a, int b, Vec3 point, Vec3 normal, float penetration) {
        this(a, b, point, normal, penetration, 0.5f, 0.3f); // defaults
    }

    public Contact(int a, int b, Vec3 point, Vec3 normal, float penetration,
                   float staticFriction, float dynamicFriction) {
        this.a = a;
        this.b = b;
        this.point = point.cpy();
        this.normal = normal.cpy().normalize();
        this.penetration = penetration;
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
    }

    /**
     * Returns an orthonormal basis {tangentU, tangentV} perpendicular to the contact normal.
     * These are used to apply friction impulses.
     */
    public Vec3 getTangentU() {
        if (tangentU == null) {
			computeTangents();
		}
        return tangentU;
    }

    public Vec3 getTangentV() {
        if (tangentV == null) {
			computeTangents();
		}
        return tangentV;
    }

    private void computeTangents() {
        // Pick an arbitrary vector that is not parallel to normal
        Vec3 ref = Math.abs(normal.getX()) > 0.707f ? new Vec3(0,1,0) : new Vec3(1,0,0);
        tangentU = normal.cross(ref).normalize();
        tangentV = normal.cross(tangentU).normalize();
    }

    @Override
    public String toString() {
        return String.format(
            "Contact[a=%d, b=%d, point=%s, normal=%s, penetration=%.4f, μs=%.2f, μd=%.2f]",
            a, b, point, normal, penetration, staticFriction, dynamicFriction
        );
    }
}
