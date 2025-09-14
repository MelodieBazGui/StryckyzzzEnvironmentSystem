package engine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides unique IDs for objects in the engine.
 */
public final class IdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(0);

    private IdGenerator() {}

    public static int nextId() {
        return counter.getAndIncrement();
    }

    public static void reset() {
        counter.set(0);
    }
}
