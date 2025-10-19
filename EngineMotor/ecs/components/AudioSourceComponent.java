package ecs.components;

import ecs.Component;
import math.Vec3;

/**
 * Represents a streaming audio source backed by a WAV file (PCM).
 * For MIDI playback, pre-render to WAV and use this component.
 */
public class AudioSourceComponent implements Component {
    public String filePath;      // path to WAV asset (PCM stereo 16-bit)
    public float volume = 1f;
    public Vec3 position = new Vec3(0,0,0);
    public boolean looping = false;
    public boolean playing = false;

    public AudioSourceComponent(String filePath) {
        this.filePath = filePath;
    }
}
