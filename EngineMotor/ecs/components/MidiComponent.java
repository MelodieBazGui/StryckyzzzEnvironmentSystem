package ecs.components;

import audio.MidiPlayer;
import ecs.Component;

public class MidiComponent implements Component {
    public String filePath;
    public boolean autoPlay;
    public transient MidiPlayer player;

    public MidiComponent(String filePath, boolean autoPlay) {
        this.filePath = filePath;
        this.autoPlay = autoPlay;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }
}
