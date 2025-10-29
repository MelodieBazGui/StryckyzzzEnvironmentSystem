package ecs.systems;

import ecs.Entity;
import engine.IdRegistry;
import java.util.*;

/**
 * Manages all entity instances and their IDs.
 * Provides creation, removal, and alive-state checks.
 */
public class EntityManager {
    private final IdRegistry<Entity> registry = new IdRegistry<>();

    /** Register an entity by its own ID. */
    public void addEntity(Entity e) {
        registry.register(e.getId(), e);
    }

    /** Remove an entity by object. */
    public void removeEntity(Entity e) {
        registry.unregister(e);
    }

    /** Check if an entity is alive (by object reference). */
    public boolean isAlive(Entity e) {
        return registry.contains(e);
    }

    /** Retrieve an entity by ID. */
    public Entity getEntity(int id) {
        return registry.get(id);
    }

    /** Retrieve all active entities. */
    public Collection<Entity> getAllEntities() {
        return new ArrayList<>(registry.values());
    }

    /** Number of active entities. */
    public int count() {
        return registry.size();
    }

    /** Clear all registered entities. */
    public void clear() {
        registry.clear();
    }
}
