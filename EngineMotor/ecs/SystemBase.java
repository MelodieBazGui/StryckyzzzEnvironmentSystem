package ecs;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import utils.Logger;

/**
 * Base class for all ECS systems.
 * Provides safe lifecycle management, pause/resume, and blocking shutdown.
 * Systems should override {@link #update(ECSManager, float)} for logic,
 * and optionally {@link #onInit(ECSManager)} / {@link #onShutdown()} for setup/cleanup.
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
    // Lifecycle hooks (for subclass override)
    // -------------------------------------------------------------------------

    /** Called when the system starts up. Override for custom setup logic. */
    protected void onInit(ECSManager ecs) {}

    /** Called when the system shuts down. Override for cleanup logic. */
    protected void onShutdown() {}

    /** Called every frame (or tick) to perform the system's main logic. */
    public abstract void update(ECSManager ecs, float deltaTime);

    // -------------------------------------------------------------------------
    // Initialization / start
    // -------------------------------------------------------------------------

    public void initialize(ECSManager ecs) {
        try {
            onInit(ecs);
            log.info("System initialized: " + getClass().getSimpleName());
        } catch (Throwable t) {
            log.error("System initialization failed: " + getClass().getSimpleName(), t);
        }
    }
    
    /**
     * Initializes and starts the system.
     * @param ecs the ECSManager reference
     * @param async if true, runs in a dedicated background thread
     */
    public synchronized void start(ECSManager ecs, boolean async) {
        if (running.get()) {
            log.warn("System already running: " + getClass().getSimpleName());
            return;
        }

        ecsRef = ecs;
        running.set(true);
        paused.set(false);

        try {
            onInit(ecs);
            log.info("System started: " + getClass().getSimpleName());
        } catch (Throwable t) {
            log.error("System initialization failed: " + getClass().getSimpleName(), t);
        }

        if (async) {
            workerThread = new Thread(this, getClass().getSimpleName() + "-Thread");
            workerThread.start();
        }
    }

    /** Called directly for synchronous system updates (non-threaded). */
    public void tick(ECSManager ecs, float deltaTime) {
        if (!running.get() || paused.get()) return;
        try {
            long start = System.nanoTime();
            update(ecs, deltaTime);
            long elapsed = System.nanoTime() - start;
            if (elapsed > 1_000_000) {
                log.info(String.format("System %s tick: %.3f ms",
                        getClass().getSimpleName(), elapsed / 1_000_000f));
            }
        } catch (Throwable t) {
            log.error("Error during system update: " + getClass().getSimpleName(), t);
        }
    }

    // -------------------------------------------------------------------------
    // Threaded execution loop
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
                Thread.currentThread().interrupt();
                log.warn("System thread interrupted: " + getClass().getSimpleName());
                break;
            } finally {
                lock.unlock();
            }

            if (ecsRef != null) tick(ecsRef, deltaTime);

            try {
                Thread.sleep(1); // prevent CPU spin
            } catch (InterruptedException ignored) {}
        }
        log.info("System thread exiting: " + getClass().getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Thread control
    // -------------------------------------------------------------------------

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

    /** Returns true if the system is running and not paused. */
    public boolean isRunning() { return running.get(); }

    /** Returns true if the system is paused. */
    public boolean isPaused() { return paused.get(); }

    // -------------------------------------------------------------------------
    // Shutdown & waiting
    // -------------------------------------------------------------------------

    /** Gracefully stops the system and waits for its thread to terminate. */
    public synchronized void shutdown() {
        if (!running.get()) return;

        running.set(false);
        paused.set(false);
        resume(); // wake any waiting threads

        if (workerThread != null && workerThread.isAlive()) {
            try {
                workerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for system shutdown: " + getClass().getSimpleName());
            }
        }

        try {
            onShutdown();
        } catch (Throwable t) {
            log.error("Error during system shutdown: " + getClass().getSimpleName(), t);
        }

        log.info("System shut down: " + getClass().getSimpleName());
    }

    /** Waits until the system thread terminates or finishes current work. */
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

    /** Update the delta time for threaded systems. */
    public void setDeltaTime(float dt) { this.deltaTime = dt; }

    /** Optional explicit ECS reference getter. */
    public ECSManager getECS() { return ecsRef; }
}
