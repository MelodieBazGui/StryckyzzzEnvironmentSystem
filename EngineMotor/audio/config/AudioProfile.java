package audio.config;

import java.util.*;
import utils.Logger;

/**
 * AudioProfile
 * Describes how sound should behave for a specific environment,
 * zone, or gameplay context. Supports EQ shaping, dynamic range,
 * spatial response, and reverb parameters.
 *
 * Author: EmeJay, 2025
 */
public class AudioProfile {

	private static Logger logger = new Logger(AudioProfile.class);
	
    // Basic mix controls
    private float volume = 1.0f;
    private float reverbMix = 0.3f;
    private float lowPassCutoff = 20000f; // Hz
    private float highPassCutoff = 20f;   // Hz
    private float dynamicRange = 1.0f;    // 0 = compressed, 1 = full

    // Environmental tone shaping
    private final Map<Float, Float> eqCurve = new TreeMap<>(); // frequency (Hz) → gain (dB)

    // Spatial & distance response
    private float spatialBlend = 1.0f;   // 0 = 2D, 1 = 3D
    private float distanceRolloff = 1.0f; // linear falloff multiplier
    private float maxDistance = 50.0f;   // meters

    // UI / contextual metadata
    private String name = "Default";
    private String environmentType = "Generic"; // e.g., "Cave", "Outdoor", "Underwater"

    // -----------------------------
    // Constructors
    // -----------------------------

    public AudioProfile() {}

    public AudioProfile(String name) {
        this.name = name;
        logger.info("Instatiated AudioProfile");
    }

    public AudioProfile(AudioProfile other) {
        copyFrom(other);
        logger.info("Instatiated AudioProfile from other AudioProfie");
    }

    // -----------------------------
    // Volume / Reverb / DSP
    // -----------------------------

    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = clamp(volume, 0f, 2f); }

    public float getReverbMix() { return reverbMix; }
    public void setReverbMix(float reverbMix) { this.reverbMix = clamp(reverbMix, 0f, 1f); }

    public float getLowPassCutoff() { return lowPassCutoff; }
    public void setLowPassCutoff(float cutoff) { this.lowPassCutoff = clamp(cutoff, 20f, 20000f); }

    public float getHighPassCutoff() { return highPassCutoff; }
    public void setHighPassCutoff(float cutoff) { this.highPassCutoff = clamp(cutoff, 20f, 20000f); }

    public float getDynamicRange() { return dynamicRange; }
    public void setDynamicRange(float dynamicRange) { this.dynamicRange = clamp(dynamicRange, 0f, 2f); }

    // -----------------------------
    // EQ Curve
    // -----------------------------

    /** Adds or replaces a point in the EQ curve (in Hz, dB gain). */
    public void setEqPoint(float frequency, float gainDb) {
        if (frequency <= 0) throw new IllegalArgumentException("Frequency must be positive");
        eqCurve.put(frequency, clamp(gainDb, -24f, 24f));
    }

    /** Removes a point from the EQ curve. */
    public void removeEqPoint(float frequency) {
        eqCurve.remove(frequency);
    }

    /** Returns an unmodifiable EQ curve map. */
    public Map<Float, Float> getEqCurve() {
        return Collections.unmodifiableMap(eqCurve);
    }

    /** Clears all EQ curve points. */
    public void clearEqCurve() {
        eqCurve.clear();
    }

    // -----------------------------
    // Spatialization
    // -----------------------------

    public float getSpatialBlend() { return spatialBlend; }
    public void setSpatialBlend(float spatialBlend) { this.spatialBlend = clamp(spatialBlend, 0f, 1f); }

    public float getDistanceRolloff() { return distanceRolloff; }
    public void setDistanceRolloff(float distanceRolloff) { this.distanceRolloff = clamp(distanceRolloff, 0.1f, 4f); }

    public float getMaxDistance() { return maxDistance; }
    public void setMaxDistance(float maxDistance) { this.maxDistance = Math.max(1f, maxDistance); }

    // -----------------------------
    // Metadata
    // -----------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null ? "Unnamed" : name); }

    public String getEnvironmentType() { return environmentType; }
    public void setEnvironmentType(String environmentType) { this.environmentType = environmentType; }

    // -----------------------------
    // Blending / Utility
    // -----------------------------

    /**
     * Blend two profiles together. t=0 → this, t=1 → other.
     */
    public AudioProfile blend(AudioProfile other, float t) {
        AudioProfile blended = new AudioProfile();
        t = clamp(t, 0f, 1f);

        blended.volume = lerp(this.volume, other.volume, t);
        blended.reverbMix = lerp(this.reverbMix, other.reverbMix, t);
        blended.lowPassCutoff = lerp(this.lowPassCutoff, other.lowPassCutoff, t);
        blended.highPassCutoff = lerp(this.highPassCutoff, other.highPassCutoff, t);
        blended.dynamicRange = lerp(this.dynamicRange, other.dynamicRange, t);
        blended.spatialBlend = lerp(this.spatialBlend, other.spatialBlend, t);
        blended.distanceRolloff = lerp(this.distanceRolloff, other.distanceRolloff, t);
        blended.maxDistance = lerp(this.maxDistance, other.maxDistance, t);
        blended.environmentType = t < 0.5f ? this.environmentType : other.environmentType;
        blended.name = this.name + "_blend_" + other.name;

        // Merge EQ curves (average gains)
        Set<Float> freqs = new TreeSet<>();
        freqs.addAll(this.eqCurve.keySet());
        freqs.addAll(other.eqCurve.keySet());
        for (Float f : freqs) {
            float a = this.eqCurve.getOrDefault(f, 0f);
            float b = other.eqCurve.getOrDefault(f, 0f);
            blended.eqCurve.put(f, lerp(a, b, t));
        }

        return blended;
    }

    /** Merges another profile’s parameters into this one (useful for AudioConfigManager). */
    public void merge(AudioProfile other) {
        if (other == null) return;
        this.volume = (this.volume + other.volume) / 2f;
        this.reverbMix = (this.reverbMix + other.reverbMix) / 2f;
        this.dynamicRange = (this.dynamicRange + other.dynamicRange) / 2f;
        this.spatialBlend = (this.spatialBlend + other.spatialBlend) / 2f;
        this.environmentType = other.environmentType != null ? other.environmentType : this.environmentType;

        for (Map.Entry<Float, Float> e : other.eqCurve.entrySet()) {
            float freq = e.getKey();
            float gain = e.getValue();
            float current = this.eqCurve.getOrDefault(freq, 0f);
            this.eqCurve.put(freq, (current + gain) / 2f);
        }
    }

    /** Copies all properties from another AudioProfile. */
    public void copyFrom(AudioProfile other) {
        if (other == null) return;
        this.volume = other.volume;
        this.reverbMix = other.reverbMix;
        this.lowPassCutoff = other.lowPassCutoff;
        this.highPassCutoff = other.highPassCutoff;
        this.dynamicRange = other.dynamicRange;
        this.spatialBlend = other.spatialBlend;
        this.distanceRolloff = other.distanceRolloff;
        this.maxDistance = other.maxDistance;
        this.name = other.name;
        this.environmentType = other.environmentType;
        this.eqCurve.clear();
        this.eqCurve.putAll(other.eqCurve);
    }

    /** Deep clone for safety. */
    public AudioProfile copy() {
        return new AudioProfile(this);
    }

    /** Static helper to blend multiple profiles. */
    public static AudioProfile blendList(List<AudioProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) return new AudioProfile();
        if (profiles.size() == 1) return profiles.get(0).copy();

        AudioProfile result = profiles.get(0).copy();
        for (int i = 1; i < profiles.size(); i++) {
            float t = (float) i / (profiles.size() - 1);
            result = result.blend(profiles.get(i), t);
        }
        return result;
    }

    // -----------------------------
    // Dynamic Property Interface
    // -----------------------------
    
    public void setProperty(String key, String value) {
        if (key == null || value == null) return;
        key = key.toLowerCase(Locale.ROOT);

        try {
            switch (key) {
                case "volume": setVolume(Float.parseFloat(value)); break;
                case "reverbmix": setReverbMix(Float.parseFloat(value)); break;
                case "lowpasscutoff": setLowPassCutoff(Float.parseFloat(value)); break;
                case "highpasscutoff": setHighPassCutoff(Float.parseFloat(value)); break;
                case "dynamicrange": setDynamicRange(Float.parseFloat(value)); break;
                case "spatialblend": setSpatialBlend(Float.parseFloat(value)); break;
                case "distancerolloff": setDistanceRolloff(Float.parseFloat(value)); break;
                case "maxdistance": setMaxDistance(Float.parseFloat(value)); break;
                case "environmenttype":
                    setEnvironmentType(value);
                    break;
                case "name":
                    setName(value);
                    break;
                default:
                    logger.error("Unknown AudioProfile property: " + key, null);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid numeric value for property '" + key + "': " + value, e);
        }
    }

    /** Retrieves a property value as a string (for serialization/debug). */
    public String getProperty(String key) {
        if (key == null) return null;
        key = key.toLowerCase(Locale.ROOT);
        switch (key) {
            case "volume": return String.valueOf(volume);
            case "reverbmix": return String.valueOf(reverbMix);
            case "lowpasscutoff": return String.valueOf(lowPassCutoff);
            case "highpasscutoff": return String.valueOf(highPassCutoff);
            case "dynamicrange": return String.valueOf(dynamicRange);
            case "spatialblend": return String.valueOf(spatialBlend);
            case "distancerolloff": return String.valueOf(distanceRolloff);
            case "maxdistance": return String.valueOf(maxDistance);
            case "environmenttype": return environmentType;
            case "name": return name;
            default: return null;
        }
    }

    /** Overload for direct numeric input */
    public void setProperty(String key, float value) {
        setProperty(key, String.valueOf(value));
    }

    
    // -----------------------------
    // Math Helpers
    // -----------------------------

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static float clamp(float v, float min, float max) {
        if (Float.isNaN(v)) return min;
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    // -----------------------------
    // Equality / String
    // -----------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioProfile)) return false;
        AudioProfile that = (AudioProfile) o;
        return Float.compare(that.volume, volume) == 0 &&
               Float.compare(that.reverbMix, reverbMix) == 0 &&
               Float.compare(that.lowPassCutoff, lowPassCutoff) == 0 &&
               Float.compare(that.highPassCutoff, highPassCutoff) == 0 &&
               Float.compare(that.dynamicRange, dynamicRange) == 0 &&
               Float.compare(that.spatialBlend, spatialBlend) == 0 &&
               Float.compare(that.distanceRolloff, distanceRolloff) == 0 &&
               Float.compare(that.maxDistance, maxDistance) == 0 &&
               Objects.equals(name, that.name) &&
               Objects.equals(environmentType, that.environmentType) &&
               Objects.equals(eqCurve, that.eqCurve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(volume, reverbMix, lowPassCutoff, highPassCutoff, dynamicRange,
                            spatialBlend, distanceRolloff, maxDistance, name, environmentType, eqCurve);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "AudioProfile{name='%s', env='%s', vol=%.2f, reverb=%.2f, LP=%.1fHz, HP=%.1fHz, dyn=%.2f, spatial=%.2f, dist=%.1f, eqPoints=%d}",
                name, environmentType, volume, reverbMix, lowPassCutoff, highPassCutoff,
                dynamicRange, spatialBlend, maxDistance, eqCurve.size());
    }
}