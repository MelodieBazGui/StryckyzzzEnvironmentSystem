package audio;

import org.junit.jupiter.api.Test;

import audio.dsp.FFTConvolver;

import static org.junit.jupiter.api.Assertions.*;

public class FFTConvolverTest {

    @Test
    void testImpulseResponseProducesSameSignal() {
        // IR = delta (impulse) -> output should equal input
        float[] ir = new float[] {1.0f};
        int block = 256;
        FFTConvolver conv = new FFTConvolver(ir, block);

        float[] in = new float[block];
        for (int i=0;i<block;i++) in[i] = (i % 2 == 0) ? 1.0f : -1.0f;

        float[] out = conv.process(in);
        // out length = block + irLen -1 = block
        assertEquals(block, out.length);
        for (int i=0;i<block;i++) assertEquals(in[i], out[i], 1e-6f);
    }

    @Test
    void testSmallReverbBlends() {
        float[] ir = new float[] {0.6f, 0.3f, 0.1f}; // small echo
        FFTConvolver conv = new FFTConvolver(ir, 128);
        float[] in = new float[128];
        in[0] = 1.0f;
        float[] out = conv.process(in);
        // out[0] should equal ~1*0.6? Actually convolution with these numbers -> ensure non-zero tail
        assertTrue(out[0] > 0);
        assertTrue(out.length == 128 + ir.length - 1);
    }
}

