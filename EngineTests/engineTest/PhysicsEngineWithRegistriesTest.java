package engineTest;

import bodies.*;
import engine.*;
import math.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class PhysicsEngineWithRegistriesTest {

    private static final float EPS = 1e-4f;

    private Shape box;
    private PhysicsEngineWithRegistries engine;

    @BeforeEach
    void setUp() {
        // Simple cube shape with inertia implementation available in your code
        box = new BoxShape(1f, 1f, 1f); // Replace with your actual Shape subclass
        engine = new PhysicsEngineWithRegistries(false); // test without tree first
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
            engine = null;
        }
        box = null;
    }

    @Test
    void testAddAndRemoveBody() {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(), Quat.identity(), 1f);
        int id = engine.addBody(body);
        assertNotNull(engine.getBody(id));
        assertTrue(engine.activeBodyIds().contains(id));

        engine.removeBody(id);
        assertNull(engine.getBody(id));
        assertFalse(engine.activeBodyIds().contains(id));

        body = null;
    }

    @Test
    void testGravityMovesDynamicBody() throws InterruptedException {
        RigidBodyFullInertia body = new RigidBodyFullInertia(box, new Vec3(0f, 10f, 0f), Quat.identity(), 1f);
        int id = engine.addBody(body);

        float initialY = body.getPosition().getY();
        engine.step(0.1f); // simulate 0.1s
        float newY = engine.getBody(id).getPosition().getY();

        assertTrue(newY < initialY, "Body should fall under gravity");

        body = null;
    }

    @Test
    void testStaticBodyImmovable() throws InterruptedException {
        RigidBodyFullInertia staticBody = new RigidBodyFullInertia(box, new Vec3(0f, 0f, 0f), Quat.identity(), 0f);
        int id = engine.addBody(staticBody);

        Vec3 posBefore = staticBody.getPosition();
        engine.step(0.1f);
        Vec3 posAfter = engine.getBody(id).getPosition();

        assertEquals(posBefore, posAfter, "Static body should not move");

        staticBody = null;
    }

    @Test
    void testCollisionProducesSeparation() throws InterruptedException {
        // Two bodies overlapping slightly
        RigidBodyFullInertia bodyA = new RigidBodyFullInertia(box, new Vec3(0f, 0f, 0f), Quat.identity(), 1f);
        RigidBodyFullInertia bodyB = new RigidBodyFullInertia(box, new Vec3(0f, 0.5f, 0f), Quat.identity(), 1f);

        int idA = engine.addBody(bodyA);
        int idB = engine.addBody(bodyB);

        float initialDistance = bodyB.getPosition().sub(bodyA.getPosition()).len();

        engine.step(0.05f); // small timestep for contact resolution

        float newDistance = engine.getBody(idB).getPosition()
                .sub(engine.getBody(idA).getPosition())
                .len();

        assertTrue(newDistance > initialDistance - EPS,
                "Bodies should separate after collision resolution");

        bodyA = null;
        bodyB = null;
    }

    @Test
    void testBroadphaseWithTree() throws InterruptedException {
        // Create new engine with tree enabled
        engine.shutdown();
        engine = new PhysicsEngineWithRegistries(true);

        RigidBodyFullInertia bodyA = new RigidBodyFullInertia(box, new Vec3(0f, 0f, 0f), Quat.identity(), 1f);
        RigidBodyFullInertia bodyB = new RigidBodyFullInertia(box, new Vec3(2f, 0f, 0f), Quat.identity(), 1f);

        engine.addBody(bodyA);
        engine.addBody(bodyB);

        // Should run without errors
        assertDoesNotThrow(() -> engine.step(0.1f));

        bodyA = null;
        bodyB = null;
    }
}
