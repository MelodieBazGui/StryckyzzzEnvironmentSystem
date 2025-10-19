package ecs.systems;

import ecs.*;
import audio.MidiPlayer;
import ecs.components.MidiComponent;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Multithreaded system for handling MIDI playback.
 * Each entity with a MidiComponent is processed concurrently.
 */
public class MidiPlayerSystem extends SystemBase {

    // Thread pool to manage MIDI playback threads
    private final ExecutorService executor;
    private final ConcurrentHashMap<Integer, Future<?>> activeTasks;

    public MidiPlayerSystem() {
        this.executor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                r -> {
                    Thread t = new Thread(r, "MidiPlayerWorker");
                    t.setDaemon(true);
                    return t;
                }
        );
        this.activeTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void update(ECSManager ecs, float deltaTime) {
        ComponentManager cm = ecs.getComponentManager();

        for (Map.Entry<Integer, MidiComponent> entry : cm.entriesForType(MidiComponent.class)) {
            int entity = entry.getKey();
            MidiComponent mc = entry.getValue();

            // Skip entities already handled
            if (activeTasks.containsKey(entity)) {
                Future<?> f = activeTasks.get(entity);
                if (f.isDone() || f.isCancelled()) {
                    activeTasks.remove(entity);
                }
                continue;
            }

            // Schedule new playback task if needed
            if (mc.autoPlay && !mc.isPlaying()) {
                Future<?> future = executor.submit(() -> playMidiForEntity(entity, mc));
                activeTasks.put(entity, future);
            }
        }
    }

    private void playMidiForEntity(int entityId, MidiComponent mc) {
        try {
            if (mc.player == null) {
                mc.player = new MidiPlayer();
                mc.player.load(new File(mc.filePath));
            }

            mc.player.play();

            // Wait for the song to end before cleanup
            while (mc.player.isPlaying()) {
                Thread.sleep(100);
            }

            mc.player.close();

        } catch (Exception e) {
            System.err.println("[MidiPlayerSystem] Error on entity " + entityId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        for (Future<?> f : activeTasks.values()) {
            f.cancel(true);
        }
        activeTasks.clear();

        executor.shutdownNow();
    }
}
