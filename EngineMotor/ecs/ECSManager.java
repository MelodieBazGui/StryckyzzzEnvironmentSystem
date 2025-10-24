package ecs;

import engine.IdRegistry;
import utils.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

import ecs.systems.EntityManager;
import ecs.systems.SystemManager;

/**
 * Thread-safe ECS core:
 * - Entity registry and ID management
 * - Component storage and retrieval
 * - Deferred command buffer (safe modifications after system updates)
 * - Parallel system updates using a fixed thread pool
 * - Complies with existing MovementSystem and ECS API structure
 */
public final class ECSManager {
    private static final Logger logger = new Logger(ECSManager.class);

    // === Core Managers ===
    private final IdRegistry<Entity> entities = new IdRegistry<>();
    private final ComponentManager componentManager = new ComponentManager();
    private final EntityManager entityManager = new EntityManager();
    private final SystemManager systemManager = new SystemManager();
    private final DeferredCommandBuffer commandBuffer = new DeferredCommandBuffer();

    // === Systems ===
    private final List<SystemBase> systems = new CopyOnWriteArrayList<>();

    // === Thread Pool ===
    private final ExecutorService pool;

    public ECSManager() {
        final int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        this.pool = Executors.newFixedThreadPool(threads);
    }

    // -------------------------------------------------------------------------
    // Entity API
    // -------------------------------------------------------------------------
    public Entity createEntity() {
        Entity e = new Entity();
        registerEntity(e);
        return e;
    }

    protected void registerEntity(Entity e) {
        entities.register(e);
        entityManager.addEntity(e);
    }

    public void destroyEntity(int id) {
        componentManager.removeAllComponents(id);
        entities.unregister(id);
        entityManager.removeEntity(id);
    }

    public boolean isAlive(int id) {
        return entities.contains(id);
    }

    public BooleanSupplier isEntityAlive(int id) {
        return () -> entities.contains(id);
    }

    public Entity getEntity(int id) {
        return entities.get(id);
    }

    /** Snapshot of all current entities. Thread-safe copy. */
    public List<Entity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }

    /** Returns all entities that have ALL specified component types. */
    @SafeVarargs
    public final List<Entity> getEntitiesWith(Class<? extends Component>... types) {
        Collection<Entity> src = entities.values();
        if (src.isEmpty()) return Collections.emptyList();

        List<Entity> out = new ArrayList<>(src.size());
        outer:
        for (Entity e : src) {
            int id = e.getId();
            for (Class<? extends Component> t : types) {
                if (!componentManager.hasComponent(id, t)) continue outer;
            }
            out.add(e);
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Component API
    // -------------------------------------------------------------------------
    public <T extends Component> void addComponentNow(int id, T component) {
        componentManager.addComponent(id, component);
    }

    public <T extends Component> void removeComponentNow(int id, Class<T> type) {
        componentManager.removeComponent(id, type);
    }

    public <T extends Component> T getComponent(int id, Class<T> type) {
        return componentManager.getComponent(id, type);
    }

    public <T extends Component> boolean hasComponent(int id, Class<T> type) {
        return componentManager.hasComponent(id, type);
    }

    // -------------------------------------------------------------------------
    // Systems API
    // -------------------------------------------------------------------------
    public void addSystem(SystemBase system) {
        systems.add(system);
        systemManager.addSystem(system);
        try {
            system.initialize(this);
        } catch (Throwable t) {
            logger.error("System initialize failed: " + system.getClass().getSimpleName(), t);
        }
    }

    public void removeSystem(SystemBase system) {
        systems.remove(system);
        systemManager.removeSystem(system);
    }

    /**
     * Runs all systems (parallel if possible), then flushes deferred commands.
     */
    public void update(float dt) {
        if (systems.isEmpty()) return;

        List<Callable<Void>> tasks = new ArrayList<>(systems.size());
        for (SystemBase s : systems) {
            tasks.add(() -> {
                try {
                    s.update(this, dt);
                } catch (Throwable t) {
                    logger.error("System update failed: " + s.getClass().getSimpleName(), t);
                }
                return null;
            });
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("ECS update interrupted", ie);
        }

        // Apply deferred add/remove component/entity commands
        try {
            commandBuffer.flush();
        } catch (Throwable t) {
            logger.error("Deferred command flush failed", t);
        }
    }

    public void shutdown() {
        for (SystemBase s : systems) {
            try {
                s.shutdown();
            } catch (Throwable t) {
                logger.warn("System shutdown failed: " + s.getClass().getSimpleName(), t);
            }
        }
        systems.clear();
        pool.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }

    public DeferredCommandBuffer commands() {
        return commandBuffer;
    }
}
