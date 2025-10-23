package audio.config;

import audio.spatial.AudioZone;
import math.Vec3;
import java.util.*;

public class AudioConfigManager {
    private SoundConfig globalConfig = new SoundConfig();
    private final List<AudioZone> zones = new ArrayList<>();

    public void registerZone(AudioZone zone) {
        zones.add(zone);
    }

    public void updateListenerPosition(Vec3 listenerPos) {
        for (AudioZone zone : zones) {
            if (zone.contains(listenerPos)) {
                applyProfile(zone.getProfile());
                break;
            }
        }
    }

    private void applyProfile(AudioProfile profile) {
        globalConfig.addProfile("active", profile);
        // Could also apply DSP chain reconfiguration in real-time here
    }

    public SoundConfig getCurrentConfig() {
        return globalConfig;
    }
}
