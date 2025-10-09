package mathTest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import noise.OpenSimplexNoise;

class OpenSimplexNoiseTest {

    @Test
    void testDeterministicNoise2D() {
        OpenSimplexNoise noise1 = new OpenSimplexNoise(1234L);
        OpenSimplexNoise noise2 = new OpenSimplexNoise(1234L);

        double n1 = noise1.noise2D(0.5, 0.5);
        double n2 = noise2.noise2D(0.5, 0.5);

        assertEquals(n1, n2, 1e-10, "Same seed should yield same 2D noise");
    }

    @Test
    void testDeterministicNoise3D() {
        OpenSimplexNoise noise = new OpenSimplexNoise(42L);
        double n1 = noise.noise3D(1.0, 2.0, 3.0);
        double n2 = noise.noise3D(1.0, 2.0, 3.0);
        assertEquals(n1, n2, 1e-10, "3D noise should be deterministic for same coords");
    }

    @Test
    void testRange2D() {
        OpenSimplexNoise noise = new OpenSimplexNoise(99L);
        for (int i = 0; i < 100; i++) {
            double val = noise.noise2D(i * 0.1, i * 0.2);
            assertTrue(val >= -1.1 && val <= 1.1, "Noise value should stay in [-1,1]");
        }
    }

    @Test
    void testContinuity() {
        OpenSimplexNoise noise = new OpenSimplexNoise(777L);
        double a = noise.noise2D(0.1, 0.1);
        double b = noise.noise2D(0.11, 0.1);
        assertTrue(Math.abs(a - b) < 0.2, "Noise should change smoothly for small deltas");
    }

    @Test
    void testNoise4DConsistency() {
        OpenSimplexNoise noise = new OpenSimplexNoise(1337L);
        double n1 = noise.noise4D(0.1, 0.2, 0.3, 0.4);
        double n2 = noise.noise4D(0.1, 0.2, 0.3, 0.4);
        assertEquals(n1, n2, 1e-10);
    }
}

