package ecs;

import ecs.systems.*;
import utils.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/**
 * Thread-safe ECS core:
 * - Entity creation/destruction through EntityManager
 * - Component storage and retrieval
 * - Deferred command buffer
 * - Parallel system updates
 */
public final class ECSManager {
    private static final Logger logger = new Logger(ECSManager.class);

    // === Core Managers ===
    private final EntityManager entityManager = new EntityManager();
    private final ComponentManager componentManager = new ComponentManager();
    private final SystemManager systemManager = new SystemManager(Runtime.getRuntime().availableProcessors());
    private final DeferredCommandBuffer commandBuffer = new DeferredCommandBuffer(this);

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
    /** Create and register a new entity in the ECS. */
    public Entity createEntity() {
        Entity e = new Entity();
        entityManager.addEntity(e);
        logger.info("Created Entity ID: " + e.getId());
        return e;
    }
    
	public void registerEntity(Entity e) {
		entityManager.addEntity(e);
        logger.info("Created Entity ID: " + e.getId());
	}

    /** Destroy an entity and remove all its components. */
    public void destroyEntity(Entity e) {
        if (e == null) return;
        componentManager.removeAllComponents(e.getId());
        entityManager.removeEntity(e);
        logger.info("Destroyed Entity ID: " + e.getId());
    }

    /** Check whether an entity is currently alive. */
    public boolean isAlive(Entity e) {
        return entityManager.isAlive(e);
    }

    /** Returns a lambda to safely check entity lifetime later. */
    public BooleanSupplier isEntityAlive(Entity e) {
        return () -> entityManager.isAlive(e);
    }

    /** Retrieve an entity by ID. */
    public Entity getEntity(int id) {
        return entityManager.getEntity(id);
    }

    /** Snapshot of all current entities (thread-safe copy). */
    public List<Entity> getAllEntities() {
        return new ArrayList<>(entityManager.getAllEntities());
    }

    /** Returns all entities that have ALL specified component types. */
    @SafeVarargs
    public final List<Entity> getEntitiesWith(Class<? extends Component>... types) {
        Collection<Entity> src = entityManager.getAllEntities();
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
        systemManager.register(system);
        try {
            system.initialize(this);
        } catch (Throwable t) {
            logger.error("System initialize failed: " + system.getClass().getSimpleName(), t);
        }
    }

    public void removeSystem(SystemBase system) {
        systems.remove(system);
        systemManager.unregister(system);
    }

    /** Runs all systems (parallel if possible), then flushes deferred commands. */
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

        try {
            commandBuffer.flush();
        } catch (Throwable t) {
            logger.error("Deferred command flush failed", t);
        }
    }

    /** Gracefully shuts down all systems and worker threads. */
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
