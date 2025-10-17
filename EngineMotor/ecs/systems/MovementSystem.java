package ecs.systems;

import java.util.concurrent.Executors;

import ecs.*;
import ecs.components.*;
import ecs.utils.ParallelECSExecutor;

public class MovementSystem extends SystemBase {
    private ParallelECSExecutor executor;

    @Override
    public void update(ECSManager ecs, float dt) {
        if (executor == null)
            executor = new ParallelECSExecutor(Executors.newCachedThreadPool());

        var entities = ecs.getAllEntities().stream()
            .filter(e -> e.hasComponent(TransformComponent.class) && e.hasComponent(VelocityComponent.class))
            .toList();

        executor.forEachParallel(entities, 64, e -> {
            var t = e.getComponent(TransformComponent.class);
            var v = e.getComponent(VelocityComponent.class);
            synchronized (t) {
                t.position.add(v.velocity.cpy().scl(dt));
            }
        });
    }
}
