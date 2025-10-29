package engine;

import java.util.*;

/**
 * A general-purpose manager for objects identified by unique integer IDs.
 * Works similarly to EntityManager but is generic.
 */
public class IdManager<T> {
    private final IdRegistry<T> registry = new IdRegistry<>();

    /** Register an object and return its assigned ID. */
    public synchronized int add(T obj) {
        return registry.register(obj);
    }

    /** Remove an object by ID. */
    public void remove(int id) {
        registry.unregister(id);
    }

    /** Retrieve an object by ID. */
    public T get(int id) {
        return registry.get(id);
    }

    /** Check whether an ID is still active. */
    public boolean contains(int id) {
        return registry.get(id) != null;
    }

    /** Return all active objects. */
    public Collection<T> getAll() {
        return registry.values();
    }

    /** Number of active objects. */
    public int count() {
        return registry.size();
    }

    /** Clear all entries. */
    public void clear() {
        registry.clear();
    }
}
