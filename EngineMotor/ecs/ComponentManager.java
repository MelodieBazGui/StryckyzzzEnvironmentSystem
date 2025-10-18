package ecs;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all component pools by type and provides access to entity-component data.
 * @author EmeJay
 */
public class ComponentManager {
    
    /** Maps each component type to its dedicated pool */
    private final Map<Class<? extends Component>, ComponentPool<? extends Component>> pools = new ConcurrentHashMap<>();

    /**
     * Retrieve an existing pool for the given component type, or create it if missing.
     */
    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentPool<T> getOrCreatePool(Class<T> type) {
        return (ComponentPool<T>) pools.computeIfAbsent(type, k -> new ComponentPool<>());
    }

    /**
     * Adds a component instance to the entity’s pool.
     */
    @SuppressWarnings("unchecked")
	public <T extends Component> void addComponent(int entityId, T component) {
        getOrCreatePool((Class<T>) component.getClass()).add(entityId, component);
    }

    /**
     * Removes a component from the entity.
     */
    public <T extends Component> void removeComponent(int entityId, Class<T> type) {
        ComponentPool<T> pool = getOrCreatePool(type);
        pool.remove(entityId);
    }

    /**
     * Retrieves a specific component from an entity.
     */
    public <T extends Component> T getComponent(int entityId, Class<T> type) {
        ComponentPool<T> pool = getOrCreatePool(type);
        return pool.get(entityId);
    }

    /**
     * Checks if an entity has a component of the given type.
     */
    public <T extends Component> boolean hasComponent(int entityId, Class<T> type) {
        ComponentPool<T> pool = getOrCreatePool(type);
        return pool.has(entityId);
    }

    /**
     * Returns all entity-component pairs for a specific component type.
     */
    public <T extends Component> Collection<Entry<Integer, T>> entriesForType(Class<T> type) {
        ComponentPool<T> pool = getOrCreatePool(type);
        return pool != null ? pool.entries() : Collections.emptyList();
    }

    /**
     * Clears all pools (used when resetting the ECS).
     */
    public void clear() {
        pools.clear();
    }
}
