package constraintsTest;

import math.*;
import math.algorithm.AABB;
import bodies.Shape;
import constraints.DistanceJoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DistanceJoint class using compliant mock RigidBodyFullInertia and Shape interfaces.
 */
public class DistanceJointTest {

    static class MockBody extends Shape {
        private final Vec3 position;
        private final float invMass;

        public MockBody(Vec3 position, float invMass) {
            this.position = position;
            this.invMass = invMass;
        }

        // Shape implementation (dummy)
        public Vec3 support(Vec3 dir, Quat rot, Vec3 pos) {
            Vec3 d = dir.cpy().normalize();
            return new Vec3(
                    pos.getX() + d.getX(),
                    pos.getY() + d.getY(),
                    pos.getZ() + d.getZ()
            );
        }

        public AABB computeAABB(Quat orientation, Vec3 position) {
            Vec3 half = new Vec3(0.5f, 0.5f, 0.5f);
            return new AABB(Vec3.sub(position, half), Vec3.add(position, half));
        }

        public Mat3 computeInertia(float mass) {
            // Simple diagonal inertia tensor for test (unit cube)
            float diag = (1f / 6f) * mass;
            return Mat3.diag(diag, diag, diag);
        }


		@Override
		public Vec3 getPosition() {
			return this.position;
		}


		@Override
		public float getInvMass() {
			return this.invMass;
		}
    }

    private MockBody bodyA;
    private MockBody bodyB;
    private DistanceJoint joint;

    @BeforeEach
    void setup() {
        bodyA = new MockBody(new Vec3(0, 0, 0), 1f);
        bodyB = new MockBody(new Vec3(2, 0, 0), 1f);
        joint = new DistanceJoint(bodyA, new Vec3(0, 0, 0), bodyB, new Vec3(0, 0, 0));
    }

    @Test
    void testRestLengthIsComputedCorrectly() {
        Vec3[] anchors = joint.getAnchorPoints();
        float computed = Vec3.sub(anchors[1], anchors[0]).len();
        assertEquals(2f, computed, 1e-6f);
    }

    @Test
    void testGetAndSetId() {
        joint.setId(123);
        assertEquals(123, joint.getId());
    }

    @Test
    void testSolveReducesDistanceError() {
        bodyB.getPosition().set(3f, 0, 0);
        float before = Vec3.sub(bodyB.getPosition(), bodyA.getPosition()).len();
        joint.solve(1f / 60f);
        float after = Vec3.sub(bodyB.getPosition(), bodyA.getPosition()).len();
        assertTrue(after < before, "Distance should shrink toward rest length");
    }

    @Test
    void testNoCorrectionWhenCoincident() {
        bodyA.getPosition().set(0, 0, 0);
        bodyB.getPosition().set(0, 0, 0);

        Vec3 beforeA = bodyA.getPosition().cpy();
        Vec3 beforeB = bodyB.getPosition().cpy();

        joint.solve(1f / 60f);

        assertEquals(beforeA, bodyA.getPosition());
        assertEquals(beforeB, bodyB.getPosition());
    }

    @Test
    void testBodiesMoveOppositeDirections() {
        bodyA.getPosition().set(0, 0, 0);
        bodyB.getPosition().set(3, 0, 0);

        Vec3 beforeA = bodyA.getPosition().cpy();
        Vec3 beforeB = bodyB.getPosition().cpy();

        joint.solve(1f / 60f);

        assertTrue(bodyA.getPosition().getX() > beforeA.getX(), "A should move +X");
        assertTrue(bodyB.getPosition().getX() < beforeB.getX(), "B should move -X");
    }

    @Test
    void testNoCorrectionWithInfiniteMass() {
        bodyA = new MockBody(new Vec3(0, 0, 0), 0f);
        bodyB = new MockBody(new Vec3(3, 0, 0), 0f);
        joint = new DistanceJoint(bodyA, new Vec3(0, 0, 0), bodyB, new Vec3(0, 0, 0));

        Vec3 beforeA = bodyA.getPosition().cpy();
        Vec3 beforeB = bodyB.getPosition().cpy();

        joint.solve(1f / 60f);

        assertEquals(beforeA, bodyA.getPosition());
        assertEquals(beforeB, bodyB.getPosition());
    }

    @Test
    void testAnchorPointsReturned() {
        Vec3[] anchors = joint.getAnchorPoints();
        assertNotNull(anchors);
        assertEquals(2, anchors.length);
        assertNotNull(anchors[0]);
        assertNotNull(anchors[1]);
    }
}
