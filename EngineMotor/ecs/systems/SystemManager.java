package ecs.systems;

import utils.Logger;

import java.util.*;
import java.util.concurrent.*;

import ecs.ECSManager;
import ecs.SystemBase;

/**
 * Manages all ECS systems.
 * Executes updates in parallel using a thread pool and supports a deferred command buffer.
 */
public class SystemManager {

    private static final Logger logger = new Logger(SystemManager.class);

    private final List<SystemBase> systems = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor;
    private final Queue<Runnable> deferredCommands = new ConcurrentLinkedQueue<>();

    public SystemManager(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
        logger.info("SystemManager initialized with " + threadCount + " threads.");
    }

    // -----------------------------
    // Core API
    // -----------------------------

    public void register(SystemBase system) {
        systems.add(system);
        logger.info("System registered: " + system.getClass().getSimpleName());
    }

    public void unregister(SystemBase system) {
        systems.remove(system);
        logger.info("System unregistered: " + system.getClass().getSimpleName());
    }

    public void updateAll(ECSManager ecs, float deltaTime) {
        List<Future<?>> futures = new ArrayList<>();

        synchronized (systems) {
            for (SystemBase system : systems) {
                Future<?> future = executor.submit(() -> {
                    try {
                        system.update(ecs, deltaTime);
                    } catch (Exception e) {
                        logger.error("System failed: " + system.getClass().getSimpleName(), e);
                    }
                });
                futures.add(future);
            }
        }

        // Wait for all systems to finish before applying deferred commands
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception ignored) {}
        }

        applyDeferredCommands();
    }

    private void applyDeferredCommands() {
        Runnable command;
        while ((command = deferredCommands.poll()) != null) {
            try {
                command.run();
            } catch (Exception e) {
                logger.error("Deferred command execution failed", e);
            }
        }
    }

    // -----------------------------
    // Deferred Command Buffer API
    // -----------------------------

    /**
     * Enqueue a command to be executed safely after the current update frame.
     */
    public void enqueueCommand(Runnable command) {
        deferredCommands.add(command);
    }

    public int getPendingCommandCount() {
        return deferredCommands.size();
    }

    // -----------------------------
    // Utility
    // -----------------------------

    public List<SystemBase> getSystems() {
        return Collections.unmodifiableList(new ArrayList<>(systems));
    }

    public int count() {
        return systems.size();
    }

    public void shutdown() {
        executor.shutdownNow();
        systems.clear();
        deferredCommands.clear();
        logger.info("SystemManager shut down cleanly.");
    }
}
