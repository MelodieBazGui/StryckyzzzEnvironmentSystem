package audio.dsp;

import java.util.Arrays;

/**
 * Streaming FFT convolution using overlap-add.
 * - Input/output are float[] per-channel (mono or stereo).
 * - IR is float[] per-channel.
 *
 * Usage:
 *   FFTConvolver conv = new FFTConvolver(ir, blockSize);
 *   float[] out = conv.process(inputBlock); // inputBlock length == blockSize
 *
 * Internals:
 *  - Uses power-of-two FFT with radix-2 iterative algorithm (simple).
 *  - For reasonable IR sizes (<= 65536) this performs well in Java.
 */
public final class FFTConvolver {

    private final int blockSize;      // frames per block
    private final int fftSize;        // power-of-two >= blockSize + irLen - 1
    private final int halfSize;       // fftSize/2
    private final float[] irFreqReal; // IR FFT real (interleaved as real, imag)
    private final float[] irFreqImag;
    private final float[] overlap;    // overlap buffer length = irLen - 1
    private final int irLen;

    public FFTConvolver(float[] ir, int blockSize) {
        if (ir == null || ir.length == 0)
            throw new IllegalArgumentException("IR must not be empty");
        this.irLen = ir.length;
        this.blockSize = blockSize;
        int needed = blockSize + irLen - 1;
        this.fftSize = nextPowerOfTwo(needed);
        this.halfSize = fftSize / 2;
        // precompute IR FFT
        float[] padded = new float[fftSize];
        System.arraycopy(ir, 0, padded, 0, irLen);
        ComplexFFT.fft(padded, fftSize); // packs into real,imag arrays via helper
        this.irFreqReal = ComplexFFT.getReal(); // internal static-results
        this.irFreqImag = ComplexFFT.getImag();
        // overlap buffer
        this.overlap = new float[irLen - 1];
    }

    /**
     * Process a single input block (length == blockSize) and return convolved output
     * of length blockSize + irLen - 1.
     */
    public float[] process(float[] inputBlock) {
        if (inputBlock.length != blockSize)
            throw new IllegalArgumentException("inputBlock length must equal blockSize");

        // zero-pad input to fftSize
        float[] inPadded = new float[fftSize];
        System.arraycopy(inputBlock, 0, inPadded, 0, blockSize);

        // FFT input
        ComplexFFT.fft(inPadded, fftSize);
        float[] inReal = ComplexFFT.getReal();
        float[] inImag = ComplexFFT.getImag();

        // multiply inFreq * irFreq (complex multiply)
        float[] outReal = new float[fftSize];
        float[] outImag = new float[fftSize];
        for (int k = 0; k < fftSize; k++) {
            float a = inReal[k];
            float b = inImag[k];
            float c = irFreqReal[k];
            float d = irFreqImag[k];
            // (a+ib)*(c+id) = (ac - bd) + i(ad + bc)
            outReal[k] = a * c - b * d;
            outImag[k] = a * d + b * c;
        }

        // inverse FFT result into time domain (re-using ComplexFFT)
        ComplexFFT.ifft(outReal, outImag, fftSize);
        float[] time = ComplexFFT.getReal(); // real part contains samples length fftSize

        // sum overlap + current block output (length = blockSize + irLen - 1)
        int outLen = blockSize + irLen - 1;
        float[] output = new float[outLen];
        // first add overlap
        int copyOverlap = Math.min(overlap.length, outLen);
        System.arraycopy(overlap, 0, output, 0, copyOverlap);
        // add time (first outLen samples)
        for (int i = 0; i < outLen; i++) {
            output[i] += time[i];
        }
        // compute new overlap = tail of time (positions blockSize .. outLen-1)
        int newOverlapLen = irLen - 1;
        Arrays.fill(overlap, 0f);
        for (int i = 0; i < newOverlapLen; i++) {
            int srcIdx = blockSize + i;
            if (srcIdx < time.length) overlap[i] = time[srcIdx];
            else overlap[i] = 0f;
        }
        return output;
    }

    private static int nextPowerOfTwo(int v) {
        int p = 1;
        while (p < v) p <<= 1;
        return p;
    }

    // ------------------------------
    // Internal FFT helper (Cooley-Tukey radix-2 iterative)
    // Stores results in ComplexFFT.getReal()/getImag()
    // ------------------------------
    private static final class ComplexFFT {
        // We'll use thread-local arrays to avoid repeated allocation when called from multiple convolvers
        private static float[] real;
        private static float[] imag;

        static void fft(float[] data, int n) {
            // 'data' is real time-domain array length n, we compute full complex FFT
            // Build complex arrays
            ensureCapacity(n);
            for (int i = 0; i < n; i++) {
                real[i] = data[i];
                imag[i] = 0f;
            }
            fftInPlace(real, imag, n);
        }

        static void ifft(float[] r, float[] im, int n) {
            ensureCapacity(n);
            // Copy input
            System.arraycopy(r, 0, real, 0, n);
            System.arraycopy(im, 0, imag, 0, n);

            // Conjugate for IFFT
            for (int i = 0; i < n; i++) imag[i] = -imag[i];

            fftInPlace(real, imag, n);

            // Conjugate back and scale by 1/n
            float scale = 1.0f / n;
            for (int i = 0; i < n; i++) {
                real[i] = real[i] * scale;
                imag[i] = -imag[i] * scale;
            }
        }


        static float[] getReal() { return real; }
        static float[] getImag() { return imag; }

        private static void ensureCapacity(int n) {
            if (real == null || real.length < n) {
                real = new float[n];
                imag = new float[n];
            }
        }

        /** iterative in-place FFT */
        private static void fftInPlace(float[] r, float[] im, int n) {
            int levels = 31 - Integer.numberOfLeadingZeros(n); // log2(n)
            // bit-reverse copy
            for (int i = 0; i < n; i++) {
                int j = Integer.reverse(i) >>> (32 - levels);
                if (j > i) {
                    float tr = r[i]; r[i] = r[j]; r[j] = tr;
                    float ti = im[i]; im[i] = im[j]; im[j] = ti;
                }
            }
            for (int size = 2; size <= n; size <<= 1) {
                int half = size >>> 1;
                double theta = -2 * Math.PI / size;
                double wpr = Math.cos(theta);
                double wpi = Math.sin(theta);
                for (int i = 0; i < n; i += size) {
                    double wr = 1.0;
                    double wi = 0.0;
                    for (int j = 0; j < half; j++) {
                        int evenIndex = i + j;
                        int oddIndex = i + j + half;
                        float er = (float) (wr * r[oddIndex] - wi * im[oddIndex]);
                        float ei = (float) (wr * im[oddIndex] + wi * r[oddIndex]);

                        r[oddIndex] = r[evenIndex] - er;
                        im[oddIndex] = im[evenIndex] - ei;
                        r[evenIndex] += er;
                        im[evenIndex] += ei;

                        double tmp = wr;
                        wr = tmp * wpr - wi * wpi;
                        wi = tmp * wpi + wi * wpr;
                    }
                }
            }
        }
    }
}
