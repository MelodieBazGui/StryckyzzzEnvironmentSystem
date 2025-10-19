package audio;

import javax.sound.midi.*;
import java.io.File;

/**
 * Simple MIDI player that supports volume control and asynchronous playback.
 * Works with the default Java Synthesizer.
 */
public class MidiPlayer {
    private Sequencer sequencer;
    private Synthesizer synth;
    private MidiChannel[] channels;
    private float volume = 1.0f;
    private boolean isLoaded = false;

    public MidiPlayer() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();

            sequencer = MidiSystem.getSequencer(false);
            sequencer.getTransmitter().setReceiver(synth.getReceiver());
            sequencer.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(File file) throws Exception {
        Sequence sequence = MidiSystem.getSequence(file);
        sequencer.setSequence(sequence);
        isLoaded = true;
    }

    public void play() {
        if (sequencer != null && isLoaded) {
            sequencer.start();
        }
    }

    public void stop() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
        }
    }

    public void close() {
        if (sequencer != null) sequencer.close();
        if (synth != null) synth.close();
    }

    public boolean isPlaying() {
        return sequencer != null && sequencer.isRunning();
    }

    /** Set playback volume (0.0 – 1.0) for all MIDI channels */
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(volume, 1f));
        if (channels != null) {
            int midiVol = (int) (127 * this.volume);
            for (MidiChannel channel : channels) {
                if (channel != null) channel.controlChange(7, midiVol);
            }
        }
    }

    public float getVolume() {
        return volume;
    }
}
