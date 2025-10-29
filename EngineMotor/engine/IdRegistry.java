package engine;

import java.util.*;
import ecs.Entity;

/**
 * Generic registry that maps integer IDs to objects, and recycles IDs when objects are removed.
 * Useful for entities, bodies, shapes, constraints, etc.
 */
public final class IdRegistry<T> {
    private final Map<Integer, T> objects = new HashMap<>();
    private final Queue<Integer> freeIds = new ArrayDeque<>();
    private int nextId = 1; // start IDs at 1 for clarity

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /** Allocate a new ID and register the object. */
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

    /** Register an object with a specific ID (e.g. external or pre-assigned). */
    public synchronized void register(int id, T obj) {
        objects.put(id, obj);
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    // -------------------------------------------------------------------------
    // Removal
    // -------------------------------------------------------------------------

    /** Remove an object by its numeric ID. */
    public synchronized void unregister(int id) {
        if (objects.containsKey(id)) {
            objects.remove(id);
            freeIds.add(id);
        }
    }

    /** ✅ Remove an object by reference (if present). */
    public synchronized void unregister(T obj) {
        Integer targetId = null;
        for (Map.Entry<Integer, T> entry : objects.entrySet()) {
            if (Objects.equals(entry.getValue(), obj)) {
                targetId = entry.getKey();
                break;
            }
        }
        if (targetId != null) {
            objects.remove(targetId);
            freeIds.add(targetId);
        }
    }

    // -------------------------------------------------------------------------
    // Lookup & Info
    // -------------------------------------------------------------------------

    /** Get an object by its ID (null if not found). */
    public synchronized T get(int id) {
        return objects.get(id);
    }

    /** Check if an ID is active. */
    public synchronized boolean contains(int id) {
        return objects.containsKey(id);
    }

    /** Check if a given object is active in this registry. */
    public synchronized boolean contains(Entity e1) {
        return objects.containsValue(e1);
    }

    /** Return all active IDs. */
    public synchronized Set<Integer> activeIds() {
        return new HashSet<>(objects.keySet());
    }

    /** Return all active objects. */
    public synchronized Collection<T> values() {
        return new ArrayList<>(objects.values());
    }

    /** Number of active objects. */
    public synchronized int size() {
        return objects.size();
    }

    /** Clear all entries and reset ID counter. */
    public synchronized void clear() {
        objects.clear();
        freeIds.clear();
        nextId = 1;
    }
}
