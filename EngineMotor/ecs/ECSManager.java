package ecs;

import engine.IdRegistry;
import utils.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

import ecs.systems.EntityManager;
import ecs.systems.SystemManager;

/**
 * Thread-safe ECS with ID registry, component pools, and deferred updates.
 */
public class ECSManager {
    private static final Logger logger = new Logger(ECSManager.class);

    private final IdRegistry<Entity> entities = new IdRegistry<>();
    private final ComponentManager components = new ComponentManager();
    private final DeferredCommandBuffer commandBuffer = new DeferredCommandBuffer();
    private final List<SystemBase> systems = new CopyOnWriteArrayList<>();
    private final ExecutorService pool;

	private ComponentManager componentManager;
	private EntityManager entityManager;
	private SystemManager systemManager;

    public ECSManager() {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        pool = Executors.newFixedThreadPool(threads);
        this.componentManager = new ComponentManager();
        this.entityManager = new EntityManager();
    }

    public Entity createEntity() {
        Entity e = new Entity();
        registerEntity(e);
        return e;
    }

    protected void registerEntity(Entity e) {
        entities.register(e);
    }

    public void destroyEntity(int id) {
        entities.unregister(id);
    }

    // ------------- Component API -------------

    public <T extends Component> void addComponentNow(int id, T c) {
        components.addComponent(id, c);
    }

    public <T extends Component> void removeComponentNow(int id, Class<T> type) {
        components.removeComponent(id, type);
    }

    public <T extends Component> T getComponent(int id, Class<T> type) {
        return components.getComponent(id, type);
    }

    public <T extends Component> boolean hasComponent(int id, Class<T> type) {
        return components.hasComponent(id, type);
    }

    // ------------- Deferred API -------------

    public DeferredCommandBuffer commands() {
        return commandBuffer;
    }

    // ------------- Systems -------------

    public void addSystem(SystemBase system) {
        systems.add(system);
    }

    public void update(float dt) {
        if (systems.isEmpty()) return;

        List<Callable<Void>> tasks = new ArrayList<>();
        for (SystemBase system : systems) {
            tasks.add(() -> {
                system.update(this, dt);
                return null;
            });
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("ECS interrupted: " + e.getMessage(),e);
        }

        // Apply deferred commands safely
        commandBuffer.flush();
    }

    public void shutdown() {
        pool.shutdownNow();
    }

    //------------- Getters for Managers -------------
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

	public BooleanSupplier isEntityAlive(int id) {
		// TODO Auto-generated method stub
		return null;
	}
}
