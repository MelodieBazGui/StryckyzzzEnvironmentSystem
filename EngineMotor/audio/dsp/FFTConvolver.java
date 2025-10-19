package audio.dsp;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe, normalized FFT-based convolution engine.
 * Supports single and multi-channel input.
 *
 * Designed for real-time reverb, spatial audio, and zone mixing.
 *
 * Author: EmeJay, 2025
 */
public class FFTConvolver {
    private final int blockSize;
    private final int fftSize;
    private final float[] impulseFFT; // precomputed FFT of impulse
    private final ConcurrentMap<Long, float[]> threadBuffers = new ConcurrentHashMap<>();

    private final FFT fft;

    /**
     * Create a new FFT convolver with a given impulse response and block size.
     * @param impulseResponse impulse response (time-domain samples)
     * @param blockSize number of samples processed per block
     */
    public FFTConvolver(float[] impulseResponse, int blockSize) {
        this.blockSize = blockSize;
        this.fftSize = nextPowerOfTwo(blockSize * 2);
        this.fft = new FFT(fftSize);

        float[] paddedImpulse = Arrays.copyOf(impulseResponse, fftSize);
        this.impulseFFT = new float[fftSize * 2];
        fft.forward(paddedImpulse, impulseFFT);
    }

    /**
     * Process a single-channel buffer through the convolver.
     * @param input samples
     * @return convolved (same length as input)
     */
    public float[] process(float[] input) {
        float[] fftBuf = threadBuffers.computeIfAbsent(Thread.currentThread().getId(),
                id -> new float[fftSize * 2]);

        Arrays.fill(fftBuf, 0);
        float[] paddedInput = Arrays.copyOf(input, fftSize);
        fft.forward(paddedInput, fftBuf);

        multiplyComplexInPlace(fftBuf, impulseFFT);

        fft.inverse(fftBuf, true); // normalized inverse FFT

        // return only the valid block portion
        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = fftBuf[i * 2]; // take real parts only
        }

        return output;
    }

    /**
     * Process multiple channels in parallel.
     * @param input multi-channel input [channel][samples]
     * @return multi-channel output
     */
    public float[][] processMulti(float[][] input) {
        float[][] output = new float[input.length][];
        Arrays.parallelSetAll(output, ch -> process(input[ch]));
        return output;
    }

    /**
     * Frequency-domain multiply: (a + bi)*(c + di) = (ac−bd) + (ad+bc)i
     */
    private static void multiplyComplexInPlace(float[] a, float[] b) {
        for (int i = 0; i < a.length; i += 2) {
            float ar = a[i];
            float ai = a[i + 1];
            float br = b[i];
            float bi = b[i + 1];

            a[i]     = ar * br - ai * bi;
            a[i + 1] = ar * bi + ai * br;
        }
    }

    private static int nextPowerOfTwo(int n) {
        int v = 1;
        while (v < n) v <<= 1;
        return v;
    }

    /**
     * Simple float-based FFT implementation (Cooley–Tukey radix-2).
     * Not super optimized, but correct and stable for audio.
     */
    private static class FFT {
        private final int size;
        private final float[] cosTable;
        private final float[] sinTable;

        FFT(int size) {
            this.size = size;
            cosTable = new float[size / 2];
            sinTable = new float[size / 2];
            for (int i = 0; i < size / 2; i++) {
                double angle = -2.0 * Math.PI * i / size;
                cosTable[i] = (float) Math.cos(angle);
                sinTable[i] = (float) Math.sin(angle);
            }
        }

        /** Forward FFT: fills dst as [real0, imag0, real1, imag1, ...] */
        void forward(float[] src, float[] dst) {
            int n = size;
            Arrays.fill(dst, 0);
            for (int i = 0; i < src.length && i < n; i++) dst[i * 2] = src[i];

            bitReverse(dst, n);

            for (int len = 2; len <= n; len <<= 1) {
                int half = len >> 1;
                int step = n / len;
                for (int i = 0; i < n; i += len) {
                    for (int k = 0; k < half; k++) {
                        int even = (i + k) * 2;
                        int odd = (i + k + half) * 2;
                        float wr = cosTable[k * step];
                        float wi = sinTable[k * step];
                        float orr = dst[odd];
                        float oii = dst[odd + 1];
                        float tr = wr * orr - wi * oii;
                        float ti = wr * oii + wi * orr;
                        dst[odd] = dst[even] - tr;
                        dst[odd + 1] = dst[even + 1] - ti;
                        dst[even] += tr;
                        dst[even + 1] += ti;
                    }
                }
            }
        }

        /** Inverse FFT, normalized if normalize=true */
        void inverse(float[] data, boolean normalize) {
            int n = size;

            // conjugate
            for (int i = 1; i < data.length; i += 2)
                data[i] = -data[i];

            // forward FFT
            bitReverse(data, n);

            for (int len = 2; len <= n; len <<= 1) {
                int half = len >> 1;
                int step = n / len;
                for (int i = 0; i < n; i += len) {
                    for (int k = 0; k < half; k++) {
                        int even = (i + k) * 2;
                        int odd = (i + k + half) * 2;
                        float wr = cosTable[k * step];
                        float wi = -sinTable[k * step]; // inverse uses -sin
                        float orr = data[odd];
                        float oii = data[odd + 1];
                        float tr = wr * orr - wi * oii;
                        float ti = wr * oii + wi * orr;
                        data[odd] = data[even] - tr;
                        data[odd + 1] = data[even + 1] - ti;
                        data[even] += tr;
                        data[even + 1] += ti;
                    }
                }
            }

            // conjugate again
            for (int i = 1; i < data.length; i += 2)
                data[i] = -data[i];

            // normalize
            if (normalize) {
                float scale = 1f / n;
                for (int i = 0; i < data.length; i++)
                    data[i] *= scale;
            }
        }

        private void bitReverse(float[] data, int n) {
            int j = 0;
            for (int i = 0; i < n; i++) {
                if (i < j) {
                    int i2 = i * 2;
                    int j2 = j * 2;
                    float tr = data[i2];
                    float ti = data[i2 + 1];
                    data[i2] = data[j2];
                    data[i2 + 1] = data[j2 + 1];
                    data[j2] = tr;
                    data[j2 + 1] = ti;
                }
                int m = n >> 1;
                while (j >= m && m > 0) {
                    j -= m;
                    m >>= 1;
                }
                j += m;
            }
        }
    }
}
