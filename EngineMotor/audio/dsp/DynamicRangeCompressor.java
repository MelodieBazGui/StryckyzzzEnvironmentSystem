package audio.dsp;

public final class DynamicRangeCompressor {
    private final float sampleRate;
    private final float attackCoef;
    private final float releaseCoef;
    private float thresholdDb = -12f;
    private float ratio = 4f;
    private float makeupDb = 0f;
    private float detectorState = 0f; // linear RMS/peak detector

    public DynamicRangeCompressor(float sampleRate, float attackMs, float releaseMs) {
        this.sampleRate = sampleRate;
        this.attackCoef = (float) Math.exp(-1.0 / (0.001 * attackMs * sampleRate));
        this.releaseCoef = (float) Math.exp(-1.0 / (0.001 * releaseMs * sampleRate));
    }

    public void setThresholdDb(float db) { this.thresholdDb = db; }
    public void setRatio(float ratio) { this.ratio = Math.max(1f, ratio); }
    public void setMakeupDb(float db) { this.makeupDb = db; }

    /**
     * Processes mono float buffer in-place (samples in -1..1).
     */
    public void process(float[] buf) {
        float kneeLinear = dbToLinear(thresholdDb);
        float makeup = dbToLinear(makeupDb);
        for (int i = 0; i < buf.length; i++) {
            float abs = Math.abs(buf[i]);
            // simple peak detector with attack/release
            if (abs > detectorState) detectorState = attackCoef * detectorState + (1f - attackCoef) * abs;
            else detectorState = releaseCoef * detectorState + (1f - releaseCoef) * abs;

            float env = Math.max(detectorState, 1e-9f);
            float envDb = linearToDb(env);

            float gainDb = 0f;
            if (envDb > thresholdDb) {
                float over = envDb - thresholdDb;
                gainDb = - (over - (over / ratio));
            }
            float gain = dbToLinear(gainDb) * makeup;
            buf[i] *= gain;
        }
    }

    private static float dbToLinear(float db) { return (float) Math.pow(10.0, db / 20.0); }
    private static float linearToDb(float v) { return (float) (20.0 * Math.log10(Math.max(v, 1e-9f))); }
}
