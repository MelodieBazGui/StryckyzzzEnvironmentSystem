package ecs.systems;

import engine.IdRegistry;

/**
 * Manages all entity IDs in the ECS.
 * Provides creation, destruction, and alive status tracking.
 */
public class EntityManager {
    private final IdRegistry<Integer> registry = new IdRegistry<>();

    /** Create a new entity and return its ID */
    public int createEntity() {
        int id = registry.register(0);
        return id;
    }

    /** Destroy an entity by ID */
    public void destroyEntity(int id) {
        registry.unregister(id);
    }

    /** Check whether an entity ID is alive */
    public boolean isAlive(int id) {
        return registry.isActive(id);
    }

    /** Return the number of active entities */
    public int count() {
        return registry.size();
    }
}
