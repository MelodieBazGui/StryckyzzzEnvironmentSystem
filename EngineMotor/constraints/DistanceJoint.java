package constraints;

import bodies.RigidBodyFullInertia;
import math.Vec3;

/**
 * Simple distance joint between two bodies at anchor points.
 */
public final class DistanceJoint implements Constraint {
    private int id = -1;

    private final RigidBodyFullInertia bodyA;
    private final RigidBodyFullInertia bodyB;
    private final Vec3 localAnchorA;
    private final Vec3 localAnchorB;
    private final float restLength;

    public DistanceJoint(RigidBodyFullInertia a, Vec3 localA,
                         RigidBodyFullInertia b, Vec3 localB) {
        this.bodyA = a;
        this.bodyB = b;
        this.localAnchorA = localA.cpy();
        this.localAnchorB = localB.cpy();
        this.restLength = Vec3.sub(worldAnchorB(), worldAnchorA()).len();
    }

    @Override
    public int getId() { return id; }

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public RigidBodyFullInertia getBodyA() { return bodyA; }

    @Override
    public RigidBodyFullInertia getBodyB() { return bodyB; }

    private Vec3 worldAnchorA() {
        return Vec3.add(bodyA.getPosition(), localAnchorA);
    }

    private Vec3 worldAnchorB() {
        return Vec3.add(bodyB.getPosition(), localAnchorB);
    }

    @Override
    public void solve(float dt) {
        Vec3 pa = worldAnchorA();
        Vec3 pb = worldAnchorB();

        Vec3 diff = Vec3.sub(pb, pa);
        float dist = diff.len();
        if (dist < 1e-6f) {
			return;
		}

        float error = dist - restLength;
        Vec3 n = diff.scl(1f / dist);

        // simple positional correction (no Baumgarte, no compliance)
        float invMassA = bodyA.getInvMass();
        float invMassB = bodyB.getInvMass();
        float invMassSum = invMassA + invMassB;
        if (invMassSum == 0f) {
			return;
		}

        Vec3 correction = Vec3.scl(n, error / invMassSum);

        // apply positional corrections
        if (invMassA > 0f) {
			bodyA.getPosition().add(Vec3.scl(correction, -invMassA));
		}
        if (invMassB > 0f) {
			bodyB.getPosition().add(Vec3.scl(correction,  invMassB));
		}
    }

    @Override
    public Vec3[] getAnchorPoints() {
        return new Vec3[] { worldAnchorA(), worldAnchorB() };
    }
}
