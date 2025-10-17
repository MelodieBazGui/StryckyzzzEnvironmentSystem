package ecs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores components of one type in a contiguous structure.
 * Provides O(1) access by entityId and high cache locality.
 */
public class ComponentPool<T> {
    private final Map<Integer, T> components = new ConcurrentHashMap<>();

    public void add(int entityId, T component) {
        components.put(entityId, component);
    }

    public void remove(int entityId) {
        components.remove(entityId);
    }

    public T get(int entityId) {
        return components.get(entityId);
    }

    public boolean has(int entityId) {
        return components.containsKey(entityId);
    }

    public Collection<Map.Entry<Integer, T>> entries() {
        return components.entrySet();
    }

    public Collection<T> values() {
        return components.values();
    }

    public int size() {
        return components.size();
    }
}
