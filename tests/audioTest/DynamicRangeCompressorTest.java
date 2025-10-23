package audioTest;

import org.junit.jupiter.api.Test;

import audio.dsp.DynamicRangeCompressor;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicRangeCompressorTest {

    @Test
    public void testGainReductionOnLoudSignal() {
        DynamicRangeCompressor comp = new DynamicRangeCompressor(44100f, 2f, 50f);
        comp.setThresholdDb(-20f);
        comp.setRatio(8f);
        comp.setMakeupDb(0f);

        float[] buf = new float[1024];
        for (int i = 0; i < buf.length; i++) buf[i] = (i < 10) ? 0.9f : 0f;

        double before = rms(buf);
        comp.process(buf);
        double after = rms(buf);

        assertTrue(after < before, "Compressor should reduce RMS of loud transient");
    }

    private static double rms(float[] arr) {
        double e = 0;
        for (float v : arr) e += v*v;
        return Math.sqrt(e / arr.length);
    }
}
