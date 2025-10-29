package ecs;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queues ECS mutations to apply after current system updates finish.
 * Bind to an ECSManager once, then enqueue Runnables or use the helpers below.
 */
public final class DeferredCommandBuffer {
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();
    private final ECSManager ecs;

    /** Bind the buffer to a specific ECS instance. */
    public DeferredCommandBuffer(ECSManager ecs) {
        this.ecs = Objects.requireNonNull(ecs, "ecs");
    }

    /** Preferred: enqueue a custom command. */
    public void add(Runnable cmd) {
        commands.add(Objects.requireNonNull(cmd, "cmd"));
    }

    /** Back-compat alias for older call sites. */
    public void addCommand(Runnable cmd) {
        add(cmd);
    }

    // -------- Convenience helpers (no ecs param needed) --------

    /** Defer: register an existing entity instance. */
    public void addEntity(Entity e) {
        add(() -> ecs.registerEntity(e));
    }

    /** Defer: destroy an entity by ID. */
    public void destroyEntity(Entity e) {
        add(() -> ecs.destroyEntity(e));
    }

    /** Defer: add a component to an entity now (at flush). */
    public <T extends Component> void addComponent(int id, T c) {
        add(() -> ecs.addComponentNow(id, c));
    }

    /** Defer: remove a component type from an entity (at flush). */
    public <T extends Component> void removeComponent(int id, Class<T> type) {
        add(() -> ecs.removeComponentNow(id, type));
    }

    /** Apply all queued commands. Continues on exceptions. */
    public void flush() {
        Runnable r;
        while ((r = commands.poll()) != null) {
            try {
                r.run();
            } catch (Throwable t) {
                // Keep going; you can swap this to your Logger if desired.
                t.printStackTrace();
            }
        }
    }

    /** True if there are no pending commands. */
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    /** Number of pending commands. */
    public int size() {
        return commands.size();
    }

    /** Drop all queued commands without executing them. */
    public void clear() {
        commands.clear();
    }
}
