package ecs;

import engine.IdGenerator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity represents a unique object in the ECS.
 * Each has an integer ID for fast lookup and serialization.
 */
public class Entity {
    private final int id;
    private final Map<Class<? extends Component>, Component> components = new ConcurrentHashMap<>();

    public Entity() {
        this.id = IdGenerator.nextId();
    }

    public int getId() {
        return id;
    }

    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T extends Component> T getComponent(Class<T> type) {
        return type.cast(components.get(type));
    }

    public boolean hasComponent(Class<? extends Component> type) {
        return components.containsKey(type);
    }

    public void removeComponent(Class<? extends Component> type) {
        components.remove(type);
    }

    public Collection<Component> getAllComponents() {
        return components.values();
    }

    @Override
    public String toString() {
        return "Entity{id=" + id + ", components=" + components.keySet() + "}";
    }
}
