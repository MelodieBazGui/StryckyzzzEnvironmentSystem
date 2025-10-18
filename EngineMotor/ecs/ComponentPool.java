package ecs;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores components of one type in a contiguous structure.
 * Provides O(1) access by entityId and high cache locality.
 */
@SuppressWarnings("hiding")
public class ComponentPool<Component> {
    private final Map<Integer, Component> components = new ConcurrentHashMap<>();

    public void add(int entityId, Component component) {
        components.put(entityId, component);
    }

    public void remove(int entityId) {
        components.remove(entityId);
    }

    public Component get(int entityId) {
        return components.get(entityId);
    }

    public boolean has(int entityId) {
        return components.containsKey(entityId);
    }

    public Collection<Entry<Integer, Component>> entries() {
        return components.entrySet();
    }

    public Collection<Component> values() {
        return components.values();
    }

    public int size() {
        return components.size();
    }
}
