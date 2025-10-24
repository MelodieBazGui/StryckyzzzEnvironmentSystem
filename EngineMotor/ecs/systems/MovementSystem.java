package ecs.systems;

import ecs.*;
import ecs.components.*;
import ecs.utils.ParallelECSExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parallelized movement system.
 * Updates entity transforms based on velocity using dt.
 * Designed to be thread-safe and allocation-free.
 */
public final class MovementSystem extends SystemBase {

    private ParallelECSExecutor executor;
    private ExecutorService pool;

    public void initialize(ECSManager ecs) {
        // Initialize thread pool once
        pool = Executors.newWorkStealingPool(); // better parallelism than cached pool
        executor = new ParallelECSExecutor(pool);
    }

    @Override
    public void update(ECSManager ecs, float dt) {
        // Gather all entities with required components
        List<Entity> entities = ecs.getEntitiesWith(TransformComponent.class, VelocityComponent.class);
        if (entities.isEmpty()) return;

        executor.forEachParallel(entities, 64, entity -> {
            TransformComponent t = entity.getComponent(TransformComponent.class);
            VelocityComponent v = entity.getComponent(VelocityComponent.class);

            // No need to synchronize; each transform is unique to the entity
            // Perform in-place position update (no allocations)
            t.position.add(v.velocity);
        });
    }

    public void shutdown() {
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
            executor = null;
        }
    }
}
