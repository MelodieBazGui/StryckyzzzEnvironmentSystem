package registries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bodies.Shape;

/**
 * Registry for shapes. Assigns IDs and tracks them.
 */
public final class ShapeRegistry {
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, Shape> shapes = new ConcurrentHashMap<>();

    public int register(Shape s) {
        int id = nextId.getAndIncrement();
        shapes.put(id, s);
        return id;
    }

    public Shape get(int id) {
        return shapes.get(id);
    }

    public boolean isActive(int id) {
        return shapes.containsKey(id);
    }

    public void remove(int id) {
        shapes.remove(id);
    }

    public void clear() {
        shapes.clear();
    }
}
