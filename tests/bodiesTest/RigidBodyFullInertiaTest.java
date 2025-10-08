package bodiesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bodies.BoxShape;
import bodies.RigidBodyFullInertia;
import bodies.Shape;
import math.Mat3;
import math.Quat;
import math.Vec3;

class RigidBodyFullInertiaTest {

    private static final float EPS = 1e-6f;

    private Shape box;

    @BeforeEach
    void setUp() {
        box = new BoxShape(1f,2f,3f,1f);
    }

    @AfterEach
    void tearDown() {
        box = null;
    }

    @Test
    void testDynamicBodyConstruction() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(1f, 2f, 3f), Quat.identity(), 2f);

        assertEquals(0.5f, body.getInvMass(), EPS, "Inverse mass should be 1/mass");
        assertEquals(new Vec3(1f, 2f, 3f), body.getPosition(), "Position should be initialized");
        assertEquals(Quat.identity(), body.getOrientation(), "Orientation should be initialized");

        assertNotNull(body.getInertiaBody());
        assertNotNull(body.getInertiaBodyInv());
        assertNotNull(body.getInertiaWorldInv());

        body = null;
    }

    @Test
    void testStaticBodyConstruction() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 0f);

        assertEquals(0f, body.getInvMass(), EPS, "Static body should have invMass = 0");
        assertNotNull(body.getInertiaBody(), "Static body should still have inertia matrices");
        assertNotNull(body.getInertiaBodyInv());
        assertNotNull(body.getInertiaWorldInv());

        // Should not move when integrated
        Vec3 posBefore = body.getPosition();
        body.integrate(1f, new Vec3(0f, -9.81f, 0f));
        assertEquals(posBefore, body.getPosition(), "Static body should not move");

        body = null;
    }

    @Test
    void testApplyImpulseLinear() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 1f);

        Vec3 impulse = new Vec3(1f, 0f, 0f);
        Vec3 rel = new Vec3(0f, 0f, 0f);

        body.applyImpulse(impulse, rel);

        Vec3 velocity = body.getVelocity();
        assertEquals(1f, velocity.getX(), EPS);
        assertEquals(0f, velocity.getY(), EPS);
        assertEquals(0f, velocity.getZ(), EPS);

        body = null;
    }

    @Test
    void testApplyImpulseAngular() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 1f);

        Vec3 impulse = new Vec3(0f, 1f, 0f);
        Vec3 rel = new Vec3(1f, 0f, 0f); // offset → should produce torque

        body.applyImpulse(impulse, rel);

        Vec3 omega = body.getOmega();
        assertNotEquals(new Vec3(0f, 0f, 0f), omega, "Angular velocity should change");

        body = null;
    }

    @Test
    void testIntegrateWithGravity() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(0f, 10f, 0f), Quat.identity(), 1f);

        body.integrate(1f, new Vec3(0f, -9.81f, 0f));

        Vec3 pos = body.getPosition();
        assertTrue(pos.getY() < 10f, "Body should fall under gravity");

        body = null;
    }

    @Test
    void testUpdateInertiaWorld() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 1f);

        Mat3 inertiaWorldBefore = body.getInertiaWorldInv();
        body.updateInertiaWorld();
        Mat3 inertiaWorldAfter = body.getInertiaWorldInv();

        assertEquals(inertiaWorldBefore, inertiaWorldAfter, "Update should produce consistent inertia world matrix");

        body = null;
    }

    @Test
    void testInertiaBodyAndInverseConsistency() {
        float mass = 2f;
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), mass);

        Mat3 inertiaBody = body.getInertiaBody();
        Mat3 inertiaBodyInv = body.getInertiaBodyInv();

        // Multiplying inertia by its inverse should give (approximately) identity
        Mat3 product = inertiaBody.mul(inertiaBodyInv);

        assertEquals(Mat3.identity().get(0,0), product.get(0,0), EPS);
        assertEquals(Mat3.identity().get(1,1), product.get(1,1), EPS);
        assertEquals(Mat3.identity().get(2,2), product.get(2,2), EPS);

        body = null;
    }

    @Test
    void testInertiaWorldMatchesRotation() {
        float mass = 1f;
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), mass);

        // Capture original world inertia
        Mat3 inertiaWorldBefore = body.getInertiaWorldInv();

        // Rotate the body 90° around Z
        body.getOrientation().setFromAxisAngle(new Vec3(0f,0f,1f), (float)Math.toRadians(90));
        body.updateInertiaWorld();

        Mat3 inertiaWorldAfter = body.getInertiaWorldInv();

        assertNotEquals(inertiaWorldBefore, inertiaWorldAfter,
            "World inertia should change when orientation changes");

        body = null;
    }

    @Test
    void testStaticBodyHasIdentityInertia() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 0f);

        assertEquals(Mat3.identity(), body.getInertiaBody(), "Static body inertia should default to identity");
        assertEquals(Mat3.identity(), body.getInertiaBodyInv(), "Static body inertia inverse should default to identity");
        assertEquals(Mat3.identity(), body.getInertiaWorldInv(), "Static body world inertia should default to identity");

        body = null;
    }

}
