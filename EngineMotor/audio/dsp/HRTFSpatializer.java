package audio.dsp;

/**
 * Lightweight spatializer: computes stereo L/R from mono input using:
 *  - stereo pan (angle -> L/R gain)
 *  - ILD (level difference) as function of angle
 *  - ITD approximated as sample delay for one channel
 *
 * Input: mono buffer `in`; output: two float arrays `left` and `right` same length.
 */
public final class HRTFSpatializer {
    private final float sampleRate;
    private final int maxDelaySamples = 64;

    public HRTFSpatializer(float sampleRate) { this.sampleRate = sampleRate; }

    /**
     * angle: -1..1 where -1 = hard left, 0 center, 1 = hard right
     * distance: 0..infty used for distance attenuation (clamped)
     */
    public void spatialize(float[] in, float[] left, float[] right, float angle, float distance) {
        if (left.length != in.length || right.length != in.length)
            throw new IllegalArgumentException("buffers must match length");

        angle = Math.max(-1f, Math.min(1f, angle));
        distance = Math.max(0.1f, distance);

        // simple pan law (equal-power)
        double theta = (angle + 1.0) * (Math.PI / 4.0); // maps -1..1 -> 0..pi/2
        float gL = (float) Math.cos(theta);
        float gR = (float) Math.sin(theta);

        // ILD adds level offset: small dB difference based on angle
        float ildDb = Math.abs(angle) * 6f; // up to 6 dB difference
        if (angle < 0) { // left side favored
            gL *= dbToLinear(ildDb);
        } else {
            gR *= dbToLinear(ildDb);
        }

        // ITD: delay the farther ear by up to maxDelaySamples
        int itdSamples = (int) (Math.abs(angle) * maxDelaySamples);

        float distAtt = 1f / (1f + 0.2f * (distance - 1f)); // simple attenuation

        // apply simple delay via shifting (costly but simple)
        if (itdSamples == 0) {
            for (int i = 0; i < in.length; i++) {
                left[i] = in[i] * gL * distAtt;
                right[i] = in[i] * gR * distAtt;
            }
        } else {
            // shift left or right
            if (angle < 0) { // left lead, right delayed
                for (int i = 0; i < in.length; i++) {
                    left[i] = in[i] * gL * distAtt;
                    int j = i - itdSamples;
                    right[i] = (j >= 0) ? in[j] * gR * distAtt : 0f;
                }
            } else { // right lead, left delayed
                for (int i = 0; i < in.length; i++) {
                    right[i] = in[i] * gR * distAtt;
                    int j = i - itdSamples;
                    left[i] = (j >= 0) ? in[j] * gL * distAtt : 0f;
                }
            }
        }
    }

    private static float dbToLinear(double db) {
        return (float) Math.pow(10.0, db / 20.0);
    }
}
