package audio;

import org.junit.jupiter.api.*;
import javax.sound.midi.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class MidiPlayerTest {
    private File midiFile;

    @BeforeEach
    void createMidi() throws Exception {
        midiFile = File.createTempFile("midiplayer", ".mid");
        Sequence seq = new Sequence(Sequence.PPQ, 480);
        Track track = seq.createTrack();

        ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100);
        track.add(new MidiEvent(on, 0));
        ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0);
        track.add(new MidiEvent(off, 480));

        MidiSystem.write(seq, 1, midiFile);
    }

    @Test
    void testPlayStop() throws Exception {
        MidiPlayer player = new MidiPlayer();
        player.load(midiFile);
        player.play();
        Thread.sleep(200);
        assertTrue(player.isPlaying());
        player.stop();
        assertFalse(player.isPlaying());
        player.close();
    }

    @AfterEach
    void cleanup() {
        midiFile.delete();
    }
}
