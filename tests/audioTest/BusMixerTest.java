package audioTest;

import org.junit.jupiter.api.Test;

import audio.dsp.BusMixer;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class BusMixerTest {

    @Test
    public void testMixAndLimiter() {
        BusMixer mixer = new BusMixer();
        mixer.addBus("sfx", 1f);
        mixer.addBus("music", 0.5f);

        int len = 256;
        float[] src1 = new float[len]; Arrays.fill(src1, 0.7f); // loud source
        float[] src2 = new float[len]; Arrays.fill(src2, 0.7f);

        Map<String, List<float[]>> map = new HashMap<>();
        map.put("sfx", Arrays.asList(src1, src2)); // sfx bus has two sources

        float[] outL = new float[len];
        float[] outR = new float[len];
        mixer.mix(map, outL, outR);

        for (int i = 0; i < len; i++) {
            assertTrue(Math.abs(outL[i]) <= 1.0f);
            assertTrue(Math.abs(outR[i]) <= 1.0f);
        }
    }
}
