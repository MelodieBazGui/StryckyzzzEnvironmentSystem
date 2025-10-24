package audio.config;

import audio.spatial.AudioZone;
import math.Vec3;
import java.util.*;

/**
 * Central manager responsible for coordinating active {@link AudioZone}s
 * and applying their {@link AudioProfile}s to the global {@link SoundConfig}.
 *
 * Supports:
 *  - Zone registration/unregistration
 *  - Priority + proximity selection
 *  - Blending between overlapping zones
 *  - Fallback to previous zone when leaving all
 */
public class AudioConfigManager {

    private SoundConfig globalConfig = new SoundConfig();
    private final List<AudioZone> zones = new ArrayList<>();

    private AudioZone activeZone;
    private AudioZone previousZone;
    private float blendFactor = 0f;

    // -------------------------------------------------------
    // Zone management
    // -------------------------------------------------------

    public void registerZone(AudioZone zone) {
        if (zone != null && !zones.contains(zone)) {
            zones.add(zone);
        }
    }

    public void unregisterZone(AudioZone zone) {
        zones.remove(zone);
        if (activeZone == zone) activeZone = null;
    }

    public void clearZones() {
        zones.clear();
        activeZone = null;
        previousZone = null;
        globalConfig = new SoundConfig();
        blendFactor = 0f;
    }

    public List<AudioZone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    // -------------------------------------------------------
    // Listener update logic
    // -------------------------------------------------------

    /**
     * Updates listener position and determines which zone should be active.
     * Picks the highest-priority zone containing the listener.
     * If priorities tie, chooses the nearest zone.
     */
    public void updateListenerPosition(Vec3 listenerPos) {
        if (zones.isEmpty()) return;

        AudioZone newZone = findBestZone(listenerPos);

        if (newZone != activeZone) {
            previousZone = activeZone;
            activeZone = newZone;
            blendFactor = 0f;
        }

        if (activeZone != null) {
            blendFactor = Math.min(1f, blendFactor + 0.1f);
            applyBlendedProfile(listenerPos);
        } else if (previousZone != null) {
            // Fade out when leaving all zones
            blendFactor = Math.max(0f, blendFactor - 0.1f);
            if (blendFactor <= 0f) previousZone = null;
        }
    }

    /**
     * Determines the most appropriate zone based on priority and proximity.
     */
    private AudioZone findBestZone(Vec3 listenerPos) {
        AudioZone best = null;
        float bestPriority = Float.NEGATIVE_INFINITY;
        float bestDistSq = Float.MAX_VALUE;

        for (AudioZone zone : zones) {
            if (!zone.isActive() || !zone.contains(listenerPos)) continue;

            float p = zone.getPriority();
            float distSq = zone.getPosition().distanceSquared(listenerPos);

            if (p > bestPriority ||
                (p == bestPriority && distSq < bestDistSq)) {
                best = zone;
                bestPriority = p;
                bestDistSq = distSq;
            }
        }

        return best;
    }

    // -------------------------------------------------------
    // Profile blending and config application
    // -------------------------------------------------------

    private void applyBlendedProfile(Vec3 listenerPos) {
        if (activeZone == null) return;

        AudioProfile current = activeZone.getProfile();
        AudioProfile previous = (previousZone != null) ? previousZone.getProfile() : null;
        AudioProfile blended = (previous != null)
                ? previous.blend(current, blendFactor)
                : current.copy();

        // Tag metadata for debugging / tests
        blended.setName("blendedProfile");
        blended.setEnvironmentType("activeZone:" + activeZone.getName());

        globalConfig.addProfile("active", blended);
    }

    // -------------------------------------------------------
    // Query methods
    // -------------------------------------------------------

    public AudioZone getActiveZone() { return activeZone; }

    public AudioZone getPreviousZone() { return previousZone; }

    public float getBlendFactor() { return blendFactor; }

    public SoundConfig getCurrentConfig() { return globalConfig; }

    public boolean hasZones() { return !zones.isEmpty(); }

    public void reset() { clearZones(); }
}
