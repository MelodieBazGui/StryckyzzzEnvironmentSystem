package ecs;

import ecs.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains component pools for all registered component types.
 */
public class ComponentManager {
    private final Map<Class<? extends Component>, ComponentPool<? extends Component>> pools = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private <T extends Component> ComponentPool<T> getOrCreatePool(Class<T> type) {
        return (ComponentPool<T>) pools.computeIfAbsent(type, k -> new ComponentPool<T>());
    }

    @SuppressWarnings("unchecked")
	public <T extends Component> void addComponent(int entityId, T component) {
        getOrCreatePool((Class<T>) component.getClass()).add(entityId, component);
    }

    public <T extends Component> void removeComponent(int entityId, Class<T> type) {
        var pool = getOrCreatePool(type);
        pool.remove(entityId);
    }

    public <T extends Component> T getComponent(int entityId, Class<T> type) {
        var pool = getOrCreatePool(type);
        return pool.get(entityId);
    }

    public <T extends Component> boolean hasComponent(int entityId, Class<T> type) {
        var pool = getOrCreatePool(type);
        return pool.has(entityId);
    }

    public Collection<Map.Entry<Integer, ? extends Component>> entriesForType(Class<? extends Component> type) {
        var pool = pools.get(type);
        return pool != null ? pool.entries() : Collections.emptyList();
    }
}
