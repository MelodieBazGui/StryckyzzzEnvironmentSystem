package ecs;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import utils.Logger;

/**
 * Base class for all ECS systems.
 * Provides safe lifecycle management, pause/resume, and blocking shutdown.
 * Systems should override {@link #update(ECSManager, float)} for logic,
 * and optionally {@link #onInit()} / {@link #onShutdown()} for setup/cleanup.
 */
public abstract class SystemBase implements Runnable {

    protected final Logger log = new Logger(getClass());

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused  = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition resumeCondition = lock.newCondition();

    private Thread workerThread;
    private ECSManager ecsRef;
    private float deltaTime;

    // -------------------------------------------------------------------------
    // Lifecycle methods
    // -------------------------------------------------------------------------

    /**
     * Called automatically when the system is initialized and started.
     * Override for custom setup logic (resource allocation, caching, etc.)
     */
    protected void onInit() {}

    /**
     * Called automatically when the system is shutting down.
     * Override for cleanup (freeing buffers, releasing GPU, etc.)
     */
    protected void onShutdown() {}

    /**
     * Called every frame to perform system logic.
     */
    public abstract void update(ECSManager ecs, float deltaTime);

    /**
     * Initialize and start the system in a dedicated thread if desired.
     * @param ecs ECS manager reference
     * @param async whether to run asynchronously in a worker thread
     */
    public synchronized void start(ECSManager ecs, boolean async) {
        if (running.get()) {
            log.warn("System already running: " + getClass().getSimpleName());
            return;
        }

        ecsRef = ecs;
        running.set(true);
        paused.set(false);
        onInit();
        log.info("System started: " + getClass().getSimpleName());

        if (async) {
            workerThread = new Thread(this, getClass().getSimpleName() + "-Thread");
            workerThread.start();
        }
    }

    /** Synchronous update call (if not using threads). */
    public void tick(ECSManager ecs, float deltaTime) {
        if (!running.get() || paused.get()) return;
        try {
            long start = System.nanoTime();
            update(ecs, deltaTime);
            long elapsed = System.nanoTime() - start;
            if (elapsed > 1_000_000)
                log.info(String.format("System %s tick: %.3f ms", getClass().getSimpleName(), elapsed / 1_000_000f));
        } catch (Throwable t) {
            log.error("Error during system update", this, t);
        }
    }

    // -------------------------------------------------------------------------
    // Thread control (for async systems)
    // -------------------------------------------------------------------------

    @Override
    public void run() {
        log.info("System thread running: " + getClass().getSimpleName());
        while (running.get()) {
            lock.lock();
            try {
                while (paused.get()) {
                    resumeCondition.await();
                }
            } catch (InterruptedException e) {
                log.warn("System thread interrupted: " + getClass().getSimpleName());
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }

            if (ecsRef != null) tick(ecsRef, deltaTime);

            try {
                Thread.sleep(1); // prevent busy spin
            } catch (InterruptedException ignored) {}
        }
        log.info("System thread exiting: " + getClass().getSimpleName());
    }

    /** Pause system execution safely. */
    public void pause() {
        if (paused.compareAndSet(false, true)) {
            log.info("System paused: " + getClass().getSimpleName());
        }
    }

    /** Resume system execution if paused. */
    public void resume() {
        lock.lock();
        try {
            if (paused.compareAndSet(true, false)) {
                resumeCondition.signalAll();
                log.info("System resumed: " + getClass().getSimpleName());
            }
        } finally {
            lock.unlock();
        }
    }

    /** True if the system is actively running (not paused or shut down). */
    public boolean isRunning() { return running.get(); }

    /** True if the system is paused. */
    public boolean isPaused() { return paused.get(); }

    // -------------------------------------------------------------------------
    // Shutdown & wait
    // -------------------------------------------------------------------------

    /**
     * Gracefully stops the system and waits for the thread to finish if async.
     * Safe to call multiple times.
     */
    public synchronized void shutdown() {
        if (!running.get()) return;

        running.set(false);
        paused.set(false);
        resume(); // wake any waiting thread

        if (workerThread != null && workerThread.isAlive()) {
            try {
                workerThread.join(5000);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for system shutdown: " + getClass().getSimpleName());
                Thread.currentThread().interrupt();
            }
        }

        try {
            onShutdown();
        } catch (Throwable t) {
            log.error("Error during system shutdown", this, t);
        }

        log.info("System shut down: " + getClass().getSimpleName());
    }

    /** Blocks until the system finishes its current update or stops running. */
    public void waitForCompletion() {
        if (workerThread != null && workerThread.isAlive()) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("waitForCompletion interrupted: " + getClass().getSimpleName());
            }
        }
    }

    /** Update deltaTime for threaded systems. */
    public void setDeltaTime(float dt) { this.deltaTime = dt; }
}
