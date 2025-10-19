package ecs.components;

import ecs.Component;

public class SoundComponent implements Component {
    public String wavPath; // path to asset or null
    public boolean loop = false;
    public float volume = 1.0f;
    public boolean playOnAdd = false;

    public SoundComponent() {}
    public SoundComponent(String wavPath) { this.wavPath = wavPath; }
}
