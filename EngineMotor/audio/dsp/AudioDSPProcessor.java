package audio.dsp;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;
import java.util.Map;

/**
 * AudioDSPProcessor — enhanced DSP chain integrating:
 *  - convolution reverb (streaming ring buffer)
 *  - optional dynamic range compressor (pre/post)
 *  - optional occlusion low-pass filters (per-channel)
 *  - single-pole low-pass
 *  - HRTF helper + BusMixer available for higher-level routing
 *
 * Input/output: interleaved 16-bit little-endian stereo PCM.
 */
public final class AudioDSPProcessor {

    private final float sampleRate;
    private float[] irLeft;
    private float[] irRight;
    private float lowPassCutoff = Float.MAX_VALUE; // Hz, disabled if large
    private float reverbMix = 0.5f; // 0..1

    private final float[][] convBuffers; // per-channel circular conv history
    private int convPos = 0;

    // single-pole IIR state per channel
    private final float[] lpPrev;

    // --- Integrated DSP modules ---
    private final DynamicRangeCompressor preCompressor;
    private final DynamicRangeCompressor postCompressor;
    private final OcclusionFilter occlusionLeft;
    private final OcclusionFilter occlusionRight;
    private final HRTFSpatializer hrtf;
    private final BusMixer busMixer;

    // Feature toggles
    private boolean enablePreCompressor = false;
    private boolean enablePostCompressor = false;
    private boolean enableOcclusion = false;

    public AudioDSPProcessor(AudioFormat format, int irLengthMs) {
        this.sampleRate = format.getSampleRate();

        int irSamples = Math.max(256, (int) (irLengthMs / 1000.0f * sampleRate));
        this.irLeft = generateExpIR(irSamples, 3.0);
        this.irRight = Arrays.copyOf(irLeft, irLeft.length);
        this.convBuffers = new float[2][irLeft.length];

        this.lpPrev = new float[2];
        Arrays.fill(lpPrev, 0f);

        // instantiate integrated modules with reasonable defaults
        this.preCompressor = new DynamicRangeCompressor(sampleRate, 2f, 50f);
        this.postCompressor = new DynamicRangeCompressor(sampleRate, 10f, 200f);

        this.occlusionLeft = new OcclusionFilter(sampleRate);
        this.occlusionRight = new OcclusionFilter(sampleRate);

        this.hrtf = new HRTFSpatializer(sampleRate);
        this.busMixer = new BusMixer();
    }

    // -------------------------
    // IR generation (normalized)
    // -------------------------
    private static float[] generateExpIR(int samples, double decaySeconds) {
        float[] ir = new float[samples];
        double sum = 0.0;
        for (int i = 0; i < samples; i++) {
            double t = i / (double) samples * decaySeconds;
            double v = Math.exp(-t * 1.5);
            ir[i] = (float) v;
            sum += v * v;
        }
        float norm = (float) (1.0 / Math.sqrt(Math.max(1e-12, sum)));
        for (int i = 0; i < samples; i++) ir[i] *= norm;
        return ir;
    }

    // -------------------------
    // External configuration
    // -------------------------
    public void setReverbMix(float mix) { this.reverbMix = Math.max(0f, Math.min(1f, mix)); }
    public float getReverbMix() { return reverbMix; }

    public void setLowPassCutoff(float hz) { this.lowPassCutoff = Math.max(50f, Math.min(20000f, hz)); }
    public float getLowPassCutoff() { return lowPassCutoff; }

    public float[] getImpulseResponseLeft() { return Arrays.copyOf(irLeft, irLeft.length); }

    public void enablePreCompressor(boolean v) { this.enablePreCompressor = v; }
    public void enablePostCompressor(boolean v) { this.enablePostCompressor = v; }
    public void enableOcclusion(boolean v) { this.enableOcclusion = v; }

    public DynamicRangeCompressor getPreCompressor() { return preCompressor; }
    public DynamicRangeCompressor getPostCompressor() { return postCompressor; }
    public OcclusionFilter getOcclusionLeft() { return occlusionLeft; }
    public OcclusionFilter getOcclusionRight() { return occlusionRight; }
    public HRTFSpatializer getHrtf() { return hrtf; }
    public BusMixer getBusMixer() { return busMixer; }

    // -------------------------
    // Main processing entry
    // -------------------------
    /**
     * Processes an interleaved stereo buffer of 16-bit PCM in place.
     * offset/length are bytes and length must be a multiple of frameSize (4).
     */
    public void processBuffer(byte[] buffer, int offset, int length, AudioFormat format) {
        final int frameSize = format.getFrameSize(); // expects 4 for stereo 16-bit
        final int frames = length / frameSize;
        if (frames <= 0) return;

        // decode to floats
        float[] left = new float[frames];
        float[] right = new float[frames];

        int idx = offset;
        for (int i = 0; i < frames; i++) {
            int lo = buffer[idx++] & 0xFF;
            int hi = buffer[idx++] & 0xFF;
            short sL = (short) ((hi << 8) | lo);
            left[i] = sL / 32768.0f;

            lo = buffer[idx++] & 0xFF;
            hi = buffer[idx++] & 0xFF;
            short sR = (short) ((hi << 8) | lo);
            right[i] = sR / 32768.0f;
        }

        // Pre-compressor (optional)
        if (enablePreCompressor) {
            preCompressor.process(left);
            preCompressor.process(right);
        }

        // Convolution reverb (streaming)
        convolveStreaming(left, right);

        // Occlusion (muffling) optional
        if (enableOcclusion) {
            occlusionLeft.process(left, 0);
            occlusionRight.process(right, 1);
        }

        // Low-pass global (applied after occlusion)
        if (lowPassCutoff < 19999f) {
            applyLowPass(left, 0);
            applyLowPass(right, 1);
        }

        // Post-compressor (optional)
        if (enablePostCompressor) {
            postCompressor.process(left);
            postCompressor.process(right);
        }

        // encode back to PCM
        idx = offset;
        for (int i = 0; i < frames; i++) {
            int sampleL = (int) (Math.max(-1.0f, Math.min(1.0f, left[i])) * 32767);
            buffer[idx++] = (byte) (sampleL & 0xFF);
            buffer[idx++] = (byte) ((sampleL >> 8) & 0xFF);
            int sampleR = (int) (Math.max(-1.0f, Math.min(1.0f, right[i])) * 32767);
            buffer[idx++] = (byte) (sampleR & 0xFF);
            buffer[idx++] = (byte) ((sampleR >> 8) & 0xFF);
        }
    }

    // -------------------------
    // Streaming convolution using ring-indexed buffer
    // -------------------------
    private void convolveStreaming(float[] left, float[] right) {
        final int irLen = irLeft.length;
        for (int i = 0; i < left.length; i++) {
            convBuffers[0][convPos] = left[i];
            convBuffers[1][convPos] = right[i];

            float accL = 0f;
            float accR = 0f;
            int bufIdx = convPos;
            for (int j = 0; j < irLen; j++) {
                accL += convBuffers[0][bufIdx] * irLeft[j];
                accR += convBuffers[1][bufIdx] * irRight[j];
                bufIdx--;
                if (bufIdx < 0) bufIdx = irLen - 1;
            }

            left[i] = left[i] * (1f - reverbMix) + accL * reverbMix;
            right[i] = right[i] * (1f - reverbMix) + accR * reverbMix;

            convPos++;
            if (convPos >= irLen) convPos = 0;
        }
    }

    // -------------------------
    // low-pass (single pole)
    // -------------------------
    private void applyLowPass(float[] samples, int channelIndex) {
        float rc = 1.0f / (2.0f * (float) Math.PI * lowPassCutoff);
        float dt = 1.0f / sampleRate;
        float alpha = dt / (rc + dt);
        float prev = lpPrev[channelIndex];
        for (int i = 0; i < samples.length; i++) {
            prev = prev + alpha * (samples[i] - prev);
            samples[i] = prev;
        }
        lpPrev[channelIndex] = prev;
    }

    // -------------------------
    // Helpers exposed for convenience
    // -------------------------

    /**
     * Spatialize a mono source into stereo buffers using the integrated HRTFSpatializer.
     * This does NOT affect the main processing chain; callers should feed the returned stereo buffers
     * to the mixer or directly convert to PCM and feed processBuffer.
     *
     * @param mono input mono float array (-1..1)
     * @param outL left output (same length)
     * @param outR right output (same length)
     * @param angle -1..1 (left..right)
     * @param distance >0
     */
    public void spatializeMonoToStereo(float[] mono, float[] outL, float[] outR, float angle, float distance) {
        hrtf.spatialize(mono, outL, outR, angle, distance);
    }

    /**
     * Mix buses using the integrated BusMixer. Sources map should be busName -> list of mono float arrays.
     */
    public void mixBuses(Map<String, java.util.List<float[]>> sources, float[] outL, float[] outR) {
        busMixer.mix(sources, outL, outR);
    }
}
