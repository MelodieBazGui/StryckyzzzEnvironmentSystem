package audioTest;

import org.junit.jupiter.api.Test;

import audio.dsp.HRTFSpatializer;

import static org.junit.jupiter.api.Assertions.*;

public class HRTFSpatializerTest {

    @Test
    public void testITDAndILDEffect() {
        HRTFSpatializer s = new HRTFSpatializer(44100f);
        int len = 256;
        float[] in = new float[len];
        in[10] = 1f; // impulse at sample 10

        float[] L = new float[len];
        float[] R = new float[len];

        // Hard left
        s.spatialize(in, L, R, -1f, 1f);
        // left should have immediate impulse, right delayed or smaller
        assertTrue(L[10] > 0.5f);
        assertTrue(R[10] == 0f || R[10] < 0.1f);

        // Center
        float[] Lc = new float[len];
        float[] Rc = new float[len];
        s.spatialize(in, Lc, Rc, 0f, 1f);
        // center yields both channels roughly equal
        assertTrue(Math.abs(Lc[10] - Rc[10]) < 1e-6 || (Lc[10] > 0f && Rc[10] > 0f));
    }

    @Test
    public void testDistanceAttenuation() {
        HRTFSpatializer s = new HRTFSpatializer(44100f);
        int len = 128;
        float[] in = new float[len]; in[0] = 1f;
        float[] L1 = new float[len]; float[] R1 = new float[len];
        s.spatialize(in, L1, R1, 0f, 1f);
        float[] L2 = new float[len]; float[] R2 = new float[len];
        s.spatialize(in, L2, R2, 0f, 10f);
        assertTrue(Math.abs(L1[0]) > Math.abs(L2[0]));
        assertTrue(Math.abs(R1[0]) > Math.abs(R2[0]));
    }
}
