package audio;

import javax.sound.sampled.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PCMCache loads decoded PCM audio and stores it as float[] per channel (stereo).
 * Useful for quick looping and reusing in mixer.
 */
public final class PCMCache {

    private static final AudioFormat TARGET_FORMAT = new AudioFormat(44100f, 16, 2, true, false);
    private final Map<String, PCMData> cache = new ConcurrentHashMap<>();

    public PCMData load(String path) throws Exception {
        PCMData cached = cache.get(path);
        if (cached != null) return cached;

        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
        AudioFormat src = ais.getFormat();
        AudioInputStream converted = AudioSystem.getAudioInputStream(TARGET_FORMAT, ais);

        // read all bytes
        byte[] bytes = converted.readAllBytes();
        converted.close();

        // convert bytes to float arrays per channel
        int frameSize = TARGET_FORMAT.getFrameSize(); // 4 bytes
        int frames = bytes.length / frameSize;
        float[] left = new float[frames];
        float[] right = new float[frames];

        int idx = 0;
        for (int i = 0; i < frames; i++) {
            int lo = bytes[idx++] & 0xFF;
            int hi = bytes[idx++] & 0xFF;
            short sL = (short) ((hi << 8) | lo);
            left[i] = sL / 32768.0f;

            lo = bytes[idx++] & 0xFF;
            hi = bytes[idx++] & 0xFF;
            short sR = (short) ((hi << 8) | lo);
            right[i] = sR / 32768.0f;
        }

        PCMData pd = new PCMData(left, right, TARGET_FORMAT);
        cache.put(path, pd);
        return pd;
    }

    public boolean isCached(String path) {
        return cache.containsKey(path);
    }

    /** Small holder for PCM floats */
    public static final class PCMData {
        public final float[] left;
        public final float[] right;
        public final AudioFormat format;
        public final int frames;

        public PCMData(float[] left, float[] right, AudioFormat format) {
            this.left = left;
            this.right = right;
            this.format = format;
            this.frames = Math.min(left.length, right.length);
        }
    }
}
