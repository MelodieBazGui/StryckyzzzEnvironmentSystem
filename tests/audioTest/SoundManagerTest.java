package audioTest;

import org.junit.jupiter.api.*;

import audio.SoundManager;

import javax.sound.sampled.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class SoundManagerTest {
    private SoundManager manager;
    private File testWav;

    @BeforeEach
    void setup() throws Exception {
        manager = new SoundManager();

        // Generate a 1-second test tone (440Hz sine wave)
        float sampleRate = 44100;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        byte[] data = new byte[(int) (sampleRate * 2)];
        for (int i = 0; i < data.length / 2; i++) {
            short sample = (short) (Math.sin(2 * Math.PI * 440 * i / sampleRate) * Short.MAX_VALUE);
            data[i * 2] = (byte) (sample & 0xFF);
            data[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        // Save to a temporary WAV
        testWav = new File("test_tone.wav");
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             AudioInputStream ais = new AudioInputStream(bais, format, data.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, testWav);
        }
    }

    @Test
    void testPlayWavFileDoesNotThrow() {
        assertDoesNotThrow(() -> manager.playWavFile(testWav));
    }

    @Test
    void testPlayPcmBufferDoesNotThrow() {
        assertDoesNotThrow(() -> {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            byte[] buf = new byte[44100 * 2];
            manager.playPcmBuffer(buf, format);
        });
    }

    @AfterEach
    void cleanup() {
        testWav.delete();
        manager.shutdown();
    }
}
