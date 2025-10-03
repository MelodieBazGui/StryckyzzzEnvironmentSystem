package engine;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Generic registry that maps integer IDs to objects, and recycles IDs when objects are removed.
 * Useful for bodies, shapes, constraints, etc.
 */
public final class IdRegistry<T> {
    private final Map<Integer, T> objects = new HashMap<>();
    private final Queue<Integer> freeIds = new ArrayDeque<>();
    private int nextId = 1; // start IDs at 1 for clarity

    /** Allocate a new ID and register object */
    public synchronized int register(T obj) {
        int id;
        if (!freeIds.isEmpty()) {
            id = freeIds.poll();
        } else {
            id = nextId++;
        }
        objects.put(id, obj);
        return id;
    }

    /** Remove an object by ID, recycle its slot */
    public synchronized void unregister(int id) {
        if (objects.containsKey(id)) {
            objects.remove(id);
            freeIds.add(id);
        }
    }

    /** Get an object by ID (null if not found) */
    public synchronized T get(int id) {
        return objects.get(id);
    }

    /** Check if ID is active */
    public synchronized boolean isActive(int id) {
        return objects.containsKey(id);
    }

    /** Return all active IDs */
    public synchronized Set<Integer> activeIds() {
        return new HashSet<>(objects.keySet());
    }

    /** Return active objects */
    public synchronized Collection<T> values() {
        return objects.values();
    }

    public synchronized int size() {
        return objects.size();
    }
}
