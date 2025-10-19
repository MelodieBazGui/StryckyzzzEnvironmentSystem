package audio.dsp;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

/**
 * Lightweight streamable DSP:
 *  - small convolution reverb (IR generated from exponential decay)
 *  - simple single-pole IIR low-pass filter (per-channel)
 *
 * All operations expect 16-bit signed little-endian stereo PCM frames.
 */
public final class AudioDSPProcessor {

    private final float sampleRate;
    private float[] irLeft;
    private float[] irRight;
    private float lowPassCutoff = Float.MAX_VALUE; // Hz, disabled if large
    private float reverbMix = 0.25f; // 0..1
    private final float[][] convBuffers; // per-channel circular conv history

    // single-pole IIR state per channel
    private float[] lpPrev;

    public AudioDSPProcessor(AudioFormat format, int irLengthMs) {
        this.sampleRate = format.getSampleRate();
        int irSamples = Math.max(64, (int) (irLengthMs / 1000.0f * sampleRate));
        this.irLeft = generateExpIR(irSamples, 2.0f); // decay
        this.irRight = Arrays.copyOf(irLeft, irLeft.length);
        this.convBuffers = new float[2][irLeft.length];
        this.lpPrev = new float[2];
        Arrays.fill(lpPrev, 0f);
    }

    /** Generate an exponential-decay impulse response (mono). */
    private static float[] generateExpIR(int samples, double decaySeconds) {
        float[] ir = new float[samples];
        for (int i = 0; i < samples; i++) {
            double t = i / (double) samples * decaySeconds;
            ir[i] = (float) (Math.exp(-t * 3.0) * (1.0 - (i / (double) samples) * 0.1)); // shaped
        }
        return ir;
    }

    /** Set reverb mix 0..1 */
    public void setReverbMix(float mix) {
        this.reverbMix = Math.max(0f, Math.min(1f, mix));
    }

    /** Set low-pass cutoff in Hz (<= 20k). If set to large, disabled. */
    public void setLowPassCutoff(float hz) {
        this.lowPassCutoff = Math.max(50f, Math.min(20000f, hz));
    }

    /** Process an interleaved stereo buffer of 16-bit PCM in place.
     *  buffer contains signed 16-bit LE samples; offset and length are bytes (multiple of 4).
     */
    public void processBuffer(byte[] buffer, int offset, int length, AudioFormat format) {
        final int frameSize = format.getFrameSize(); // expects 4 for stereo 16-bit
        final int frames = length / frameSize;

        // Temporary arrays to hold float samples for left/right
        float[] left = new float[frames];
        float[] right = new float[frames];

        // decode
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

        // apply convolution reverb (naive streaming convolution using small IR)
        convolveStreaming(left, right);

        // apply low-pass if set
        if (lowPassCutoff < 19999f) {
            applyLowPass(left, 0);
            applyLowPass(right, 1);
        }

        // write back
        idx = offset;
        for (int i = 0; i < frames; i++) {
            int sampleL = (int) Math.max(-1.0f, Math.min(1.0f, left[i])) * 32767;
            buffer[idx++] = (byte) (sampleL & 0xFF);
            buffer[idx++] = (byte) ((sampleL >> 8) & 0xFF);
            int sampleR = (int) Math.max(-1.0f, Math.min(1.0f, right[i])) * 32767;
            buffer[idx++] = (byte) (sampleR & 0xFF);
            buffer[idx++] = (byte) ((sampleR >> 8) & 0xFF);
        }
    }

    /** Naive streaming convolution: push frames into circular convBuffers and compute dot with IR.
     *  This is O(N*irLen) per buffer length; IR kept small (e.g. 512..2048) to remain realtime.
     */
    private void convolveStreaming(float[] left, float[] right) {
        int irLen = irLeft.length;
        for (int i = 0; i < left.length; i++) {
            // shift buffer to the right (inefficient but simple). For production replace with ring-indexing.
            System.arraycopy(convBuffers[0], 0, convBuffers[0], 1, irLen - 1);
            System.arraycopy(convBuffers[1], 0, convBuffers[1], 1, irLen - 1);
            convBuffers[0][0] = left[i];
            convBuffers[1][0] = right[i];

            // compute dot
            float accL = 0f;
            float accR = 0f;
            for (int j = 0; j < irLen; j++) {
                accL += convBuffers[0][j] * irLeft[j];
                accR += convBuffers[1][j] * irRight[j];
            }

            // mix original + reverb
            left[i] = left[i] * (1f - reverbMix) + accL * reverbMix;
            right[i] = right[i] * (1f - reverbMix) + accR * reverbMix;
        }
    }

    /** Simple single-pole low-pass per-channel */
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
}
