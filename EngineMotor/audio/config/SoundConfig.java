package audio.config;

import java.util.*;

/**
 * SoundConfig
 * Global audio configuration container.
 * Stores global and per-profile sound behavior such as volume, reverb, and mix parameters.
 *
 * Author: EmeJay, 2025
 */
public class SoundConfig {

    // Global volume controls
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private float ambientVolume = 1.0f;
    private float uiVolume = 1.0f;
    private float globalReverbLevel = 0.5f;

    // Global environmental profile
    private AudioProfile environmentProfile = new AudioProfile();

    // Named audio behavior profiles (for zones, materials, or mix states)
    private final Map<String, AudioProfile> profiles = new HashMap<>();

    // -----------------------------
    // Constructors
    // -----------------------------

    public SoundConfig() {
        profiles.put("default", new AudioProfile());
    }

    public SoundConfig(SoundConfig other) {
        this.masterVolume = other.masterVolume;
        this.musicVolume = other.musicVolume;
        this.sfxVolume = other.sfxVolume;
        this.ambientVolume = other.ambientVolume;
        this.uiVolume = other.uiVolume;
        this.globalReverbLevel = other.globalReverbLevel;
        this.environmentProfile = new AudioProfile(other.environmentProfile);
        for (Map.Entry<String, AudioProfile> e : other.profiles.entrySet()) {
            profiles.put(e.getKey(), new AudioProfile(e.getValue()));
        }
    }

    // -----------------------------
    // Getters / Setters
    // -----------------------------

    public float getMasterVolume() { return masterVolume; }
    public void setMasterVolume(float masterVolume) { this.masterVolume = clamp(masterVolume); }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = clamp(musicVolume); }

    public float getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(float sfxVolume) { this.sfxVolume = clamp(sfxVolume); }

    public float getAmbientVolume() { return ambientVolume; }
    public void setAmbientVolume(float ambientVolume) { this.ambientVolume = clamp(ambientVolume); }

    public float getUiVolume() { return uiVolume; }
    public void setUiVolume(float uiVolume) { this.uiVolume = clamp(uiVolume); }

    public float getGlobalReverbLevel() { return globalReverbLevel; }
    public void setGlobalReverbLevel(float globalReverbLevel) { this.globalReverbLevel = clamp(globalReverbLevel); }

    public AudioProfile getEnvironmentProfile() { return environmentProfile; }
    public void setEnvironmentProfile(AudioProfile environmentProfile) {
        this.environmentProfile = (environmentProfile == null) ? new AudioProfile() : environmentProfile;
    }

    // -----------------------------
    // Profile Management
    // -----------------------------

    public void addProfile(String name, AudioProfile profile) {
        if (name == null || profile == null)
            throw new IllegalArgumentException("Profile name and instance cannot be null");
        profiles.put(name, new AudioProfile(profile));
    }

    public AudioProfile getProfile(String name) {
        return profiles.get(name);
    }

    public boolean hasProfile(String name) {
        return profiles.containsKey(name);
    }

    public void removeProfile(String name) {
        profiles.remove(name);
    }

    public Map<String, AudioProfile> getProfiles() {
        return Collections.unmodifiableMap(profiles);
    }

    public void clearProfiles() {
        profiles.clear();
    }

    /**
     * Merge another SoundConfig into this one.
     * The other config’s profiles overwrite existing ones of the same name.
     */
    public void merge(SoundConfig other) {
        if (other == null) return;
        this.masterVolume = clamp((this.masterVolume + other.masterVolume) / 2f);
        this.musicVolume = clamp((this.musicVolume + other.musicVolume) / 2f);
        this.sfxVolume = clamp((this.sfxVolume + other.sfxVolume) / 2f);
        this.ambientVolume = clamp((this.ambientVolume + other.ambientVolume) / 2f);
        this.uiVolume = clamp((this.uiVolume + other.uiVolume) / 2f);
        this.globalReverbLevel = clamp((this.globalReverbLevel + other.globalReverbLevel) / 2f);

        // Blend environment profile
        AudioProfile blended = new AudioProfile(this.environmentProfile);
        blended.setReverbMix((this.environmentProfile.getReverbMix() + other.environmentProfile.getReverbMix()) / 2f);
        this.environmentProfile = blended;

        for (Map.Entry<String, AudioProfile> e : other.profiles.entrySet()) {
            profiles.put(e.getKey(), new AudioProfile(e.getValue()));
        }
    }

    // -----------------------------
    // Utility
    // -----------------------------

    private float clamp(float v) {
        if (Float.isNaN(v)) return 0f;
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    // -----------------------------
    // Equality / Hashing / String
    // -----------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoundConfig)) return false;
        SoundConfig that = (SoundConfig) o;
        return Float.compare(that.masterVolume, masterVolume) == 0 &&
               Float.compare(that.musicVolume, musicVolume) == 0 &&
               Float.compare(that.sfxVolume, sfxVolume) == 0 &&
               Float.compare(that.ambientVolume, ambientVolume) == 0 &&
               Float.compare(that.uiVolume, uiVolume) == 0 &&
               Float.compare(that.globalReverbLevel, globalReverbLevel) == 0 &&
               Objects.equals(environmentProfile, that.environmentProfile) &&
               Objects.equals(profiles, that.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterVolume, musicVolume, sfxVolume, ambientVolume,
                            uiVolume, globalReverbLevel, environmentProfile, profiles);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "SoundConfig{master=%.2f, music=%.2f, sfx=%.2f, ambient=%.2f, ui=%.2f, reverb=%.2f, profiles=%d}",
                masterVolume, musicVolume, sfxVolume, ambientVolume, uiVolume,
                globalReverbLevel, profiles.size());
    }

    // -----------------------------
    // Builder Pattern
    // -----------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SoundConfig cfg = new SoundConfig();

        public Builder masterVolume(float v) { cfg.setMasterVolume(v); return this; }
        public Builder musicVolume(float v) { cfg.setMusicVolume(v); return this; }
        public Builder sfxVolume(float v) { cfg.setSfxVolume(v); return this; }
        public Builder ambientVolume(float v) { cfg.setAmbientVolume(v); return this; }
        public Builder uiVolume(float v) { cfg.setUiVolume(v); return this; }
        public Builder globalReverb(float v) { cfg.setGlobalReverbLevel(v); return this; }
        public Builder environmentProfile(AudioProfile p) { cfg.setEnvironmentProfile(p); return this; }
        public Builder addProfile(String name, AudioProfile p) { cfg.addProfile(name, p); return this; }

        public SoundConfig build() { return new SoundConfig(cfg); }
    }
}
