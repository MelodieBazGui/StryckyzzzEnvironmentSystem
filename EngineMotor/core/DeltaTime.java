package core;

public class DeltaTime {
    private long lastTime;
    private float delta;

    public DeltaTime() {
        lastTime = System.nanoTime();
    }

    public void update() {
        long now = System.nanoTime();
        delta = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;
    }

    public float getDelta() {
        return delta;
    }
}
