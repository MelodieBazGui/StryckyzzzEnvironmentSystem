package ecs.components;

import audio.MidiPlayer;
import ecs.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines multiple audio layers for adaptive music.
 * Each layer (like "ambient" or "combat") has its own MidiPlayer and volume.
 */
public class AudioLayerComponent implements Component {
    public static class Layer {
        public String name;
        public String filePath;
        public MidiPlayer player;
        public float targetVolume = 1.0f;
        public float currentVolume = 0.0f;
        public boolean active = false;

        public Layer(String name, String filePath) {
            this.name = name;
            this.filePath = filePath;
        }
    }

    private final Map<String, Layer> layers = new HashMap<>();

    public void addLayer(String name, String filePath) {
        layers.put(name, new Layer(name, filePath));
    }

    public Layer getLayer(String name) {
        return layers.get(name);
    }

    public Map<String, Layer> getLayers() {
        return layers;
    }
}
