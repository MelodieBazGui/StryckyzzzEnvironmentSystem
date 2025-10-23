package audio.dsp;

/**
 * Simple frequency-dependent low-pass "occlusion" filter.
 * Simulates sound muffling when an object is between listener and source.
 *
 *  - occlusion = 0 → no filtering (clear)
 *  - occlusion = 1 → heavy muffling (~500 Hz cutoff)
 */
public final class OcclusionFilter {

    private final float sampleRate;
    private float cutoffHz = 20000f;
    private float targetCutoff = 20000f;
    private float[] prev = new float[]{0f, 0f};
    private float occlusion = 0f;

    public OcclusionFilter(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Sets the occlusion factor (0..1).
     * 0 = fully open (no muffling),
     * 1 = fully occluded (strong low-pass).
     */
    public void setOcclusion(float factor) {
        if (Float.isNaN(factor)) factor = 0f;
        factor = Math.max(0f, Math.min(1f, factor));
        this.occlusion = factor;

        // map occlusion to cutoff frequency (exponential mapping)
        // so that muffling feels natural:
        //   0 → 18 kHz, 1 → 500 Hz
        this.targetCutoff = 500f + (float) Math.pow(1f - factor, 2.5f) * 17500f;
    }

    /**
     * Directly set cutoff (used for tests).
     */
    public void setCutoffHz(float hz) {
        if (hz < 20f) hz = 20f;
        if (hz > 20000f) hz = 20000f;
        this.targetCutoff = hz;
        this.cutoffHz = hz;
    }

    /**
     * Get current cutoff in Hz.
     */
    public float getCutoffHz() {
        return cutoffHz;
    }

    /**
     * Get current occlusion factor.
     */
    public float getOcclusion() {
        return occlusion;
    }

    /**
     * Process mono channel in place.
     * Uses a single-pole IIR low-pass filter.
     * Maintains previous sample per channel.
     */
    public void process(float[] buf, int channelIndex) {
        // smooth cutoff changes for stability
        cutoffHz += (targetCutoff - cutoffHz) * 0.1f;

        float rc = 1f / (2f * (float) Math.PI * cutoffHz);
        float dt = 1f / sampleRate;
        float alpha = dt / (rc + dt);

        float p = prev[channelIndex];
        for (int i = 0; i < buf.length; i++) {
            p += alpha * (buf[i] - p);
            buf[i] = p;
        }
        prev[channelIndex] = p;
    }
}
