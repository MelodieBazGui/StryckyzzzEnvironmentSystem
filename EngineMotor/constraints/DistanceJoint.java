package constraints;

import bodies.Shape;
import math.Vec3;

/**
 * High-performance distance joint constraint between two rigid bodies.
 * Uses in-place Vec3 operations (no heap allocations, full getter-based access).
 */
public final class DistanceJoint implements Constraint {

    private int id = -1;

    private final Shape bodyA;
    private final Shape bodyB;
    private final Vec3 localAnchorA;
    private final Vec3 localAnchorB;
    private final float restLength;

    // Reusable temporary vectors to avoid per-step allocations
    private final Vec3 delta = new Vec3();
    private final Vec3 correction = new Vec3();

    public DistanceJoint(Shape a, Vec3 localA,
                         Shape b, Vec3 localB) {
        this.bodyA = a;
        this.bodyB = b;
        this.localAnchorA = localA.cpy();
        this.localAnchorB = localB.cpy();

        // Precompute rest length (initial distance between anchor points)
        Vec3 temp = new Vec3();
        temp.set(worldAnchorB()).sub(worldAnchorA());
        this.restLength = temp.len();
    }

    @Override
    public int getId() { return id; }

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public Shape getBodyA() { return bodyA; }

    @Override
    public Shape getBodyB() { return bodyB; }

    /**
     * Computes the world-space anchor for body A.
     */
    private Vec3 worldAnchorA() {
        Vec3 posA = bodyA.getPosition();
        return new Vec3(
            posA.getX() + localAnchorA.getX(),
            posA.getY() + localAnchorA.getY(),
            posA.getZ() + localAnchorA.getZ()
        );
    }

    /**
     * Computes the world-space anchor for body B.
     */
    private Vec3 worldAnchorB() {
        Vec3 posB = bodyB.getPosition();
        return new Vec3(
            posB.getX() + localAnchorB.getX(),
            posB.getY() + localAnchorB.getY(),
            posB.getZ() + localAnchorB.getZ()
        );
    }

    @Override
    public void solve(float dt) {
        // Compute displacement vector between anchors (delta = B - A)
        delta.set(worldAnchorB()).sub(worldAnchorA());
        float distance = delta.len();

        // Avoid divide-by-zero or coincident anchor points
        if (distance < 1e-6f) return;

        // Compute inverse masses
        float invMassA = bodyA.getInvMass();
        float invMassB = bodyB.getInvMass();
        float invMassSum = invMassA + invMassB;
        if (invMassSum == 0f) return;

        // Compute normalized direction from A to B
        Vec3 n = delta.cpy().scl(1f / distance);

        // Compute constraint error
        float C = distance - restLength;
        if (Math.abs(C) < 1e-6f) return;

        // Apply positional corrections proportionally to inverse mass
        float correctionA =  C * (invMassA / invMassSum);
        float correctionB = -C * (invMassB / invMassSum);

        if (invMassA > 0f) {
            bodyA.getPosition().add(Vec3.scl(n, correctionA)); // A moves toward B
        }
        if (invMassB > 0f) {
            bodyB.getPosition().add(Vec3.scl(n, correctionB)); // B moves toward A
        }
    }


    @Override
    public Vec3[] getAnchorPoints() {
        return new Vec3[] { worldAnchorA(), worldAnchorB() };
    }
}
