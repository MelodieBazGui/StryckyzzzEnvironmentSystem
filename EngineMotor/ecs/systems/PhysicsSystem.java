package ecs.systems;

import math.Vec3;
import java.util.Map;

import ecs.*;
import ecs.components.*;

/**
 * Physics system: updates all entities with Transform and Rigidbody components.
 * Explicit types — no use of 'var' for compatibility.
 */
public class PhysicsSystem extends SystemBase {

    @Override
    public void update(ECSManager ecs, float deltaTime) {
        ComponentManager cm = ecs.getComponentManager();

        Map<Integer, TransformComponent> transforms = cm.entriesForType(TransformComponent.class);
        Map<Integer, RigidbodyComponent> rigidbodies = cm.entriesForType(RigidbodyComponent.class);

        for (Map.Entry<Integer, RigidbodyComponent> entry : rigidbodies.entrySet()) {
            int entity = entry.getKey();
            RigidbodyComponent rb = entry.getValue();
            TransformComponent tf = transforms.get(entity);

            if (tf == null) continue;

            rb.velocity.add(Vec3.scl(rb.acceleration, deltaTime));

            tf.position.add(Vec3.scl(rb.velocity, deltaTime));

            if (rb.useGravity) {
                tf.position.add(new Vec3(0, -9.81f * deltaTime, 0));
            }

            rb.velocity.scl(1.0f - rb.drag * deltaTime);
        }
    }
}
