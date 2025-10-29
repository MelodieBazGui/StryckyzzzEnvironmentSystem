package engine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides globally unique IDs for objects in the engine.
 */
public final class IdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(0);

    private IdGenerator() {}

    /** Generate a unique sequential ID */
    public static int nextId() {
        return counter.getAndIncrement();
    }

    /** Reset ID counter to 0 */
    public static void reset() {
        counter.set(0);
    }
}
