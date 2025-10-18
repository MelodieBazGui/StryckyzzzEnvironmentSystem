package ecs;

import ecs.components.*;
import ecs.systems.PhysicsSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhysicsSystemTest {

    private ECSManager ecs;
    private PhysicsSystem physics;

    @BeforeEach
    void setup() {
        ecs = new ECSManager();
        physics = new PhysicsSystem();
    }

    @Test
    void testAppliesVelocityAndGravity() {
        Entity entity = ecs.createEntity();
        TransformComponent tf = new TransformComponent();
        RigidbodyComponent rb = new RigidbodyComponent();

        rb.velocity.set(1, 0, 0);
        rb.acceleration.set(0, 0, 0);
        rb.useGravity = true;
        ecs.getComponentManager().addComponent(entity.getId(), tf);
        ecs.getComponentManager().addComponent(entity.getId(), rb);

        physics.update(ecs, 1.0f); // 1 second frame

        assertTrue(tf.position.getY() < 0, "Gravity should decrease Y position");
        assertTrue(tf.position.getX() > 0, "Velocity should increase X position");
    }

    @Test
    void testNoTransformNoUpdate() {
        Entity entity = ecs.createEntity();
        RigidbodyComponent rb = new RigidbodyComponent();
        ecs.getComponentManager().addComponent(entity.getId(), rb);

        rb.velocity.set(1, 2, 3);
        physics.update(ecs, 1.0f);
        assertEquals(1, rb.velocity.getX());
    }
}

