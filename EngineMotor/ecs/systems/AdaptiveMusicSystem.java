package ecs.systems;

import ecs.*;
import ecs.components.AudioLayerComponent;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Adaptive music system supporting multiple layers per entity.
 * Smoothly crossfades between active layers using multithreading.
 */
public class AdaptiveMusicSystem extends SystemBase {

    private final ExecutorService executor;
    private final ConcurrentHashMap<Integer, Future<?>> activeTasks = new ConcurrentHashMap<>();

    public AdaptiveMusicSystem() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "AudioLayerWorker");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void update(ECSManager ecs, float deltaTime) {
        ComponentManager cm = ecs.getComponentManager();

        for (Map.Entry<Integer, AudioLayerComponent> entry : cm.entriesForType(AudioLayerComponent.class)) {
            int entity = entry.getKey();
            AudioLayerComponent comp = entry.getValue();

            if (!activeTasks.containsKey(entity)) {
                Future<?> task = executor.submit(() -> updateLayers(entity, comp, deltaTime));
                activeTasks.put(entity, task);
            }
        }
    }

    private void updateLayers(int entityId, AudioLayerComponent comp, float deltaTime) {
        for (AudioLayerComponent.Layer layer : comp.getLayers().values()) {
            try {
                if (layer.active && layer.player == null) {
                    layer.player = new audio.MidiPlayer();
                    layer.player.load(new File(layer.filePath));
                    layer.player.setVolume(layer.currentVolume);
                    layer.player.play();
                }

                // Smooth volume fade toward target
                float fadeSpeed = 1.5f * deltaTime;
                if (Math.abs(layer.currentVolume - layer.targetVolume) > 0.01f) {
                    layer.currentVolume += (layer.targetVolume - layer.currentVolume) * fadeSpeed;
                    if (layer.player != null)
                        layer.player.setVolume(layer.currentVolume);
                }

                // Stop inactive layers gracefully
                if (!layer.active && layer.player != null && layer.currentVolume <= 0.01f) {
                    layer.player.stop();
                    layer.player.close();
                    layer.player = null;
                }

            } catch (Exception e) {
                System.err.println("[AdaptiveMusicSystem] Error in entity " + entityId + ": " + e.getMessage());
            }
        }
    }

    /** Toggle which layer is active and which fades out */
    public void activateLayer(AudioLayerComponent comp, String layerName) {
        for (AudioLayerComponent.Layer l : comp.getLayers().values()) {
            boolean enable = l.name.equals(layerName);
            l.active = enable;
            l.targetVolume = enable ? 1.0f : 0.0f;
        }
    }

    public void shutdown() {
        for (Future<?> f : activeTasks.values()) {
            f.cancel(true);
        }
        executor.shutdownNow();
    }
}
