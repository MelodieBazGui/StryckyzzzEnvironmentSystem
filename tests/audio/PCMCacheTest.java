package audio;

import org.junit.jupiter.api.Test;

import audio.dsp.FFTConvolver;

import static org.junit.jupiter.api.Assertions.*;

public class PCMCacheTest {

	@Test
	void testImpulseResponseProducesSameSignal() {
	    float[] ir = new float[] {1.0f};
	    int block = 256;
	    FFTConvolver conv = new FFTConvolver(ir, block);

	    float[] in = new float[block];
	    for (int i = 0; i < block; i++) in[i] = (i % 2 == 0) ? 1.0f : -1.0f;

	    float[] out = conv.process(in);
	    assertEquals(block, out.length);
	    for (int i = 0; i < block; i++) {
	        assertEquals(in[i], out[i], 1e-4f, "Signal amplitude should match");
	    }
	}

}
