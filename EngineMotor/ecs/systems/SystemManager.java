package ecs.systems;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ecs.ECSManager;
import ecs.SystemBase;

/**
 * Handles all ECS systems — registration, updates, and removal.
 */
public class SystemManager {
    private final List<SystemBase> systems = new CopyOnWriteArrayList<>();

    /** Add a system to the manager */
    public void register(SystemBase system) {
        systems.add(system);
    }

    /** Remove a system */
    public void unregister(SystemBase system) {
        systems.remove(system);
    }

    /** Run update() on all registered systems sequentially */
    public void updateAll(ECSManager ecs, float deltaTime) {
        for (SystemBase system : systems) {
            system.update(ecs, deltaTime);
        }
    }

    /** Return all active systems */
    public List<SystemBase> getSystems() {
        return Collections.unmodifiableList(systems);
    }

    /** Returns number of active systems */
    public int count() {
        return systems.size();
    }
}
