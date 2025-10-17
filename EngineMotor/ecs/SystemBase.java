package ecs;

public abstract class SystemBase {
    /**
     * Called each frame. Should avoid modifying ECS structures directly while iterating.
     * Use thread-safe data structures or deferred operations.
     */
    public abstract void update(ECSManager ecs, float deltaTime);
}
