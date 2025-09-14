package registries;

import constraints.Constraint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registry for constraints between bodies.
 */
public final class ConstraintRegistry {
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, Constraint> constraints = new ConcurrentHashMap<>();

    public int register(Constraint c) {
        int id = nextId.getAndIncrement();
        c.setId(id);
        constraints.put(id, c);
        return id;
    }

    public Constraint get(int id) {
        return constraints.get(id);
    }

    public boolean isActive(int id) {
        return constraints.containsKey(id);
    }

    public void remove(int id) {
        constraints.remove(id);
    }

    public void clear() {
        constraints.clear();
    }

    public Iterable<Constraint> all() {
        return constraints.values();
    }
}
