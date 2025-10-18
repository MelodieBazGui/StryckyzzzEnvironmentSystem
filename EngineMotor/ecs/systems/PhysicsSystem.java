package ecs.systems;

import ecs.*;
import ecs.components.*;
import math.Vec3;
import java.util.Collection;
import java.util.Map;

/**
 * Physics system:
 * Updates all entities that have both TransformComponent and RigidbodyComponent.
 * Fully typed, no use of 'var' for older compiler compatibility.
 */
public class PhysicsSystem extends SystemBase {

    @Override
    public void update(ECSManager ecs, float deltaTime) {
        ComponentManager cm = ecs.getComponentManager();

        // Get component pools
        Collection<Map.Entry<Integer, TransformComponent>> transforms =
                cm.entriesForType(TransformComponent.class);
        Collection<Map.Entry<Integer, RigidbodyComponent>> rigidbodies =
                cm.entriesForType(RigidbodyComponent.class);

        // To speed up lookups, we’ll check transform presence directly
        for (Map.Entry<Integer, RigidbodyComponent> entry : rigidbodies) {
            int entityId = entry.getKey();
            RigidbodyComponent rb = entry.getValue();

            TransformComponent tf = cm.getComponent(entityId, TransformComponent.class);
            if (tf == null) continue; // Only update entities with both components

            // --- Physics Integration ---
            // Update velocity: v += a * dt
            Vec3 deltaVel = Vec3.scl(rb.acceleration, deltaTime);
            rb.velocity.add(deltaVel);

            // Update position: p += v * dt
            Vec3 deltaPos = Vec3.scl(rb.velocity, deltaTime);
            tf.position.add(deltaPos);

            // Apply gravity if enabled
            if (rb.useGravity) {
                tf.position.add(new Vec3(0f,-9.81f * deltaTime, 0f));
            }

            // Apply drag (exponential decay)
            rb.velocity.scl(1.0f - rb.drag * deltaTime);
        }
    }
}
