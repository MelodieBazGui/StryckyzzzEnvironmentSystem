package audioTest;

import audio.dsp.FFTConvolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FFTConvolver — validates normalization, correctness,
 * multichannel consistency, and multithreaded behavior.
 */
public class FFTConvolverTest {

    private FFTConvolver convolver;
    private float[] impulse;
    private static final int BLOCK_SIZE = 256;

    @BeforeEach
    public void setup() {
        // identity impulse (1 followed by zeros)
        impulse = new float[BLOCK_SIZE];
        impulse[0] = 1.0f;
        convolver = new FFTConvolver(impulse, BLOCK_SIZE);
    }

    @Test
    public void testImpulseResponseProducesSameSignal() {
        float[] input = new float[BLOCK_SIZE];
        for (int i = 0; i < input.length; i++) input[i] = (float) Math.sin(i * 0.1);

        float[] output = convolver.process(input);

        // output should be nearly identical to input
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], output[i], 1e-4,
                    "Output mismatch at index " + i);
        }
    }

    @Test
    public void testDecayImpulseProducesSmoothedOutput() {
        float[] decay = new float[BLOCK_SIZE];
        for (int i = 0; i < decay.length; i++)
            decay[i] = (float) Math.exp(-i / 32.0);

        FFTConvolver smoother = new FFTConvolver(decay, BLOCK_SIZE);

        float[] impulseSignal = new float[BLOCK_SIZE];
        impulseSignal[0] = 1.0f;

        float[] output = smoother.process(impulseSignal);

        // output should decay smoothly
        assertTrue(output[1] < output[0], "Output should decay");
        assertTrue(output[output.length - 1] < output[output.length / 2],
                "End of output should be smallest");
    }

    @Test
    public void testMultiChannelProcessing() {
        float[][] multi = new float[2][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            multi[0][i] = (float) Math.sin(i * 0.1);
            multi[1][i] = (float) Math.cos(i * 0.1);
        }

        float[][] out = convolver.processMulti(multi);

        for (int ch = 0; ch < 2; ch++) {
            for (int i = 0; i < BLOCK_SIZE; i++) {
                assertEquals(multi[ch][i], out[ch][i], 1e-4,
                        "Mismatch in channel " + ch + " at index " + i);
            }
        }
    }

    @Test
    public void testParallelConsistency() throws Exception {
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        float[] input = new float[BLOCK_SIZE];
        for (int i = 0; i < input.length; i++) input[i] = (float) Math.sin(i * 0.05);

        Callable<float[]> task = () -> convolver.process(input);

        Future<float[]>[] futures = new Future[threads];
        for (int t = 0; t < threads; t++) futures[t] = executor.submit(task);

        float[] reference = futures[0].get();
        for (int t = 1; t < threads; t++) {
            float[] result = futures[t].get();
            for (int i = 0; i < BLOCK_SIZE; i++) {
                assertEquals(reference[i], result[i], 1e-4,
                        "Parallel channel mismatch at idx=" + i);
            }
        }

        executor.shutdown();
    }

    @Test
    public void testNormalizationPreventsAmplitudeExplosion() {
        Random rnd = new Random(42);
        float[] noisyImpulse = new float[BLOCK_SIZE];
        for (int i = 0; i < noisyImpulse.length; i++)
            noisyImpulse[i] = rnd.nextFloat() * 0.1f;

        FFTConvolver reverb = new FFTConvolver(noisyImpulse, BLOCK_SIZE);

        float[] input = new float[BLOCK_SIZE];
        for (int i = 0; i < input.length; i++) input[i] = (float) Math.sin(i * 0.2);

        float[] out = reverb.process(input);

        float max = 0;
        for (float v : out) max = Math.max(max, Math.abs(v));

        assertTrue(max <= 1.0f, "Signal normalized: expected <= 1.0, got " + max);
    }

    @Test
    public void testSilenceInputGivesSilenceOutput() {
        float[] input = new float[BLOCK_SIZE];
        float[] output = convolver.process(input);

        for (float v : output) {
            assertEquals(0f, v, 1e-6, "Non-zero output from silence");
        }
    }
}
