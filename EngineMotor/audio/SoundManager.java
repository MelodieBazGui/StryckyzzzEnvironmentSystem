package audio;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * SoundManager: plays WAV files, streams PCM buffers, and supports basic mixing.
 */
public class SoundManager {
    private final ExecutorService playbackPool = Executors.newCachedThreadPool();

    public void playWavFile(File wavFile) {
        playbackPool.submit(() -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
                AudioFormat format = ais.getFormat();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                    line.open(format);
                    line.start();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = ais.read(buf, 0, buf.length)) > 0) {
                        line.write(buf, 0, n);
                    }
                    line.drain();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void playPcmBuffer(byte[] pcm, AudioFormat format) {
        playbackPool.submit(() -> {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(pcm, 0, pcm.length);
                line.drain();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        playbackPool.shutdownNow();
    }
}
