package utils;

public class Time {
    private static long lastFrameTime = System.nanoTime();
    private static float deltaTime = 0.0f;

    /**
     * Call this once per frame/update to refresh delta time.
     */
    public static void update() {
        long now = System.nanoTime();
        deltaTime = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
    }

    /**
     * @return Time between frames in seconds.
     */
    public static float getDeltaTime() {
        return deltaTime;
    }

    /**
     * @return Time since the program started (in seconds).
     */
    public static float getElapsedTime() {
        return (System.nanoTime() - startTime) / 1_000_000_000.0f;
    }

    private static final long startTime = System.nanoTime();
}
