package audio.dsp;

import java.util.*;

/**
 * Simple bus mixer: multiple sources per bus (floats non-interleaved).
 * Each bus has a gain; master has a limiter to prevent clipping.
 */
public final class BusMixer {
    private final Map<String, Float> busGains = new LinkedHashMap<>();
    private final float limiterThreshold = 0.99f;

    public void addBus(String name, float gain) { busGains.put(name, gain); }
    public void setBusGain(String name, float gain) { busGains.put(name, gain); }

    /**
     * Mix sources map: busName -> list of source buffers (mono) into output stereo floats
     * Assumes all buffers same length.
     */
    public void mix(Map<String, List<float[]>> sources, float[] outL, float[] outR) {
        Arrays.fill(outL, 0f);
        Arrays.fill(outR, 0f);

        for (Map.Entry<String, List<float[]>> e : sources.entrySet()) {
            String bus = e.getKey();
            float gain = busGains.getOrDefault(bus, 1f);
            for (float[] src : e.getValue()) {
                for (int i = 0; i < src.length && i < outL.length; i++) {
                    // simple center panning for bus (could be per-source)
                    float s = src[i] * gain;
                    outL[i] += s * 0.7071f;
                    outR[i] += s * 0.7071f;
                }
            }
        }

        // final soft clip limiter
        for (int i = 0; i < outL.length; i++) {
            float mL = Math.abs(outL[i]);
            float mR = Math.abs(outR[i]);
            float peak = Math.max(mL, mR);
            if (peak > limiterThreshold) {
                float factor = limiterThreshold / peak;
                outL[i] *= factor;
                outR[i] *= factor;
            }
        }
    }
}
