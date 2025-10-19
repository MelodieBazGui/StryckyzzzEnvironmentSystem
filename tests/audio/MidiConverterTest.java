package audio;

import org.junit.jupiter.api.*;

import javax.sound.midi.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class MidiConverterTest {

    private File midiFile, wavFile;

    @BeforeEach
    void setup() throws Exception {
        midiFile = File.createTempFile("testMidi", ".mid");
        wavFile = File.createTempFile("testMidi", ".wav");

        Sequence seq = new Sequence(Sequence.PPQ, 480);
        Track track = seq.createTrack();

        ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 90);
        track.add(new MidiEvent(on, 0));
        ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0);
        track.add(new MidiEvent(off, 480));

        MidiSystem.write(seq, 1, midiFile);
    }

    @Test
    void testMidiToMemory() throws Exception {
        byte[] pcm = MidiConverter.renderMidiToPCM(midiFile);
        assertNotNull(pcm);
        assertTrue(pcm.length > 0);
    }

    @Test
    void testMidiToWav() throws Exception {
        MidiConverter.convertMidiToWav(midiFile, wavFile);
        assertTrue(wavFile.exists());
        assertTrue(wavFile.length() > 0);
    }

    @AfterEach
    void cleanup() {
        midiFile.delete();
        wavFile.delete();
    }
}
