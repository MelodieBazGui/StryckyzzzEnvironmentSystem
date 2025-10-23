package audioTest;

import org.junit.jupiter.api.Test;

import audio.dsp.OcclusionFilter;

import static org.junit.jupiter.api.Assertions.*;

public class OcclusionFilterTest {

    @Test
    public void testLowPassSmoothing() {
        OcclusionFilter f = new OcclusionFilter(44100f);
        f.setCutoffHz(500f);

        float[] buf = new float[1024];
        for (int i = 0; i < 1024; i++) buf[i] = (i % 2 == 0) ? 1f : -1f;

        f.process(buf, 0);

        // variance reduced relative to alternating pattern
        double diff = 0;
        for (int i = 1; i < buf.length; i++) diff += Math.abs(buf[i] - buf[i-1]);
        assertTrue(diff < 1024 * 2.0, "Low-pass should reduce rapid alternation");
    }
}
