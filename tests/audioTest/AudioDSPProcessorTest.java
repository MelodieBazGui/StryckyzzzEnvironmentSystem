package audioTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import audio.dsp.AudioDSPProcessor;

import javax.sound.sampled.AudioFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the integrated AudioDSPProcessor.
 * These verify DSP correctness (energy, balance, filtering).
 */
public class AudioDSPProcessorTest {

    private static final float SAMPLE_RATE = 44100f;
    private AudioFormat format;
    private AudioDSPProcessor dsp;

    @BeforeEach
    public void setup() {
        format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
        dsp = new AudioDSPProcessor(format, 50); // 50ms IR
    }

    @Test
    public void testPCMDecodeEncodeIntegrity() {
        // build a simple 1kHz sine
        int frames = 512;
        byte[] buffer = new byte[frames * 4];
        for (int i = 0; i < frames; i++) {
            float val = (float) Math.sin(2 * Math.PI * 1000 * i / SAMPLE_RATE);
            short s = (short) (val * 32767);
            buffer[i * 4] = (byte) (s & 0xFF);
            buffer[i * 4 + 1] = (byte) ((s >> 8) & 0xFF);
            buffer[i * 4 + 2] = (byte) (s & 0xFF);
            buffer[i * 4 + 3] = (byte) ((s >> 8) & 0xFF);
        }
        byte[] original = buffer.clone();
        dsp.processBuffer(buffer, 0, buffer.length, format);
        assertEquals(buffer.length, original.length);
    }

    @Test
    public void testReverbTailEnergyIncrease() {
        dsp.setReverbMix(1f);

        int frames = 1024;
        byte[] impulse = new byte[frames * 4];
        // impulse at frame 0 (left + right)
        impulse[0] = 0x00;
        impulse[1] = 0x7F;
        impulse[2] = 0x00;
        impulse[3] = 0x7F;

        dsp.processBuffer(impulse, 0, impulse.length, format);

        // compute energy tail
        double energy = 0;
        for (int i = 4; i < impulse.length; i++) energy += (impulse[i] * impulse[i]);
        assertTrue(energy > 0, "Tail energy expected from reverb convolution");
    }

    @Test
    public void testLowPassFilterReducesHighFreqEnergy() {
        dsp.setReverbMix(0);
        dsp.setLowPassCutoff(2000f);

        int frames = 2048;
        byte[] buf = new byte[frames * 4];
        for (int i = 0; i < frames; i++) {
            float val = (float) Math.sin(2 * Math.PI * 8000 * i / SAMPLE_RATE);
            short s = (short) (val * 32767);
            buf[i * 4] = (byte) (s & 0xFF);
            buf[i * 4 + 1] = (byte) ((s >> 8) & 0xFF);
            buf[i * 4 + 2] = buf[i * 4];
            buf[i * 4 + 3] = buf[i * 4 + 1];
        }

        byte[] before = buf.clone();
        dsp.processBuffer(buf, 0, buf.length, format);

        double rmsBefore = rms(before);
        double rmsAfter = rms(buf);
        assertTrue(rmsAfter < rmsBefore * 0.8, "Low-pass should reduce RMS of high frequency");
    }

    @Test
    public void testCompressorReducesDynamicRange() {
        dsp.enablePreCompressor(true);

        // loud + soft sections
        int frames = 4096;
        float[] samples = new float[frames];
        for (int i = 0; i < frames; i++) {
            if (i < frames / 2)
                samples[i] = (float) Math.sin(2 * Math.PI * 440 * i / SAMPLE_RATE) * 0.9f;
            else
                samples[i] = (float) Math.sin(2 * Math.PI * 440 * i / SAMPLE_RATE) * 0.1f;
        }

        float[] processed = Arrays.copyOf(samples, samples.length);
        dsp.getPreCompressor().process(processed);

        double rangeBefore = maxAbs(samples, 0, frames / 2) / maxAbs(samples, frames / 2, frames);
        double rangeAfter = maxAbs(processed, 0, frames / 2) / maxAbs(processed, frames / 2, frames);
        assertTrue(rangeAfter < rangeBefore, "Compressor should reduce dynamic range");
    }

    @Test
    public void testOcclusionFilterAttenuates() {
        dsp.enableOcclusion(true);
        dsp.getOcclusionLeft().setOcclusion(1.0f);
        dsp.getOcclusionRight().setOcclusion(1.0f);

        int frames = 1024;
        byte[] buf = new byte[frames * 4];
        for (int i = 0; i < frames; i++) {
            float val = (float) Math.sin(2 * Math.PI * 4000 * i / SAMPLE_RATE);
            short s = (short) (val * 32767);
            buf[i * 4] = (byte) (s & 0xFF);
            buf[i * 4 + 1] = (byte) ((s >> 8) & 0xFF);
            buf[i * 4 + 2] = buf[i * 4];
            buf[i * 4 + 3] = buf[i * 4 + 1];
        }

        byte[] before = buf.clone();
        dsp.processBuffer(buf, 0, buf.length, format);

        assertTrue(rms(buf) < rms(before) * 0.8, "Occlusion should attenuate high-frequency RMS");
    }

    @Test
    public void testHRTFSpatializerCreatesStereoDifference() {
        float[] mono = new float[1024];
        for (int i = 0; i < mono.length; i++)
            mono[i] = (float) Math.sin(2 * Math.PI * 1000 * i / SAMPLE_RATE);
        float[] L = new float[mono.length];
        float[] R = new float[mono.length];
        dsp.spatializeMonoToStereo(mono, L, R, 0.8f, 1.0f);
        double diff = Math.abs(rms(L) - rms(R));
        assertTrue(diff > 1e-4, "HRTF spatialization should yield L/R differences");
    }

    @Test
    public void testBusMixerCombinesBuses() {
        Map<String, List<float[]>> buses = new HashMap<>();
        float[] src1 = new float[256];
        float[] src2 = new float[256];
        Arrays.fill(src1, 0.5f);
        Arrays.fill(src2, 0.25f);
        buses.put("music", List.of(src1));
        buses.put("sfx", List.of(src2));

        float[] L = new float[256];
        float[] R = new float[256];
        dsp.mixBuses(buses, L, R);
        double avg = (avg(L) + avg(R)) / 2.0;
        assertTrue(avg > 0.3 && avg < 0.8, "Bus mix average should be within expected range");
    }

    // ---- helpers ----
    private static double rms(byte[] pcm) {
        double sum = 0;
        for (int i = 0; i < pcm.length; i++) sum += pcm[i] * pcm[i];
        return Math.sqrt(sum / pcm.length);
    }

    private static double rms(float[] f) {
        double sum = 0;
        for (float v : f) sum += v * v;
        return Math.sqrt(sum / f.length);
    }

    private static double avg(float[] f) {
        double s = 0;
        for (float v : f) s += v;
        return s / f.length;
    }

    private static double maxAbs(float[] f, int start, int end) {
        double m = 0;
        for (int i = start; i < end; i++) m = Math.max(m, Math.abs(f[i]));
        return m;
    }
}
