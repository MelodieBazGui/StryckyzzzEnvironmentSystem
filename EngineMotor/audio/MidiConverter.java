package audio;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.*;

/**
 * Converts MIDI data to PCM (WAV or memory buffer).
 * Pure Java SE 21 — no Gervill or external soundbank dependency.
 * 
 * Two modes:
 *  1. Real-time recording to a .wav file.
 *  2. Headless/offscreen rendering to byte[] or AudioInputStream.
 *  @author EmeJay (cleanup, details), Stryckoeurzzz (Main code)
 */
public class MidiConverter {

    private static final AudioFormat DEFAULT_FORMAT = new AudioFormat(44100f, 16, 2, true, false);

    /**
     * Converts a MIDI file to a WAV file by playing it through the system synth
     * and capturing the audio output in real-time.
     */
    public static void convertMidiToWav(File midiFile, File wavFile) throws Exception {
        byte[] audioData = renderMidiToPCM(midiFile);
        writePCMToWav(audioData, wavFile);
    }

    /**
     * Renders a MIDI file to an in-memory PCM byte array (headless mode).
     * Useful for in-engine sound streaming or procedural audio.
     */
    public static byte[] renderMidiToPCM(File midiFile) throws Exception {
        Sequence sequence = MidiSystem.getSequence(midiFile);

        Synthesizer synth = MidiSystem.getSynthesizer();
        Sequencer sequencer = MidiSystem.getSequencer(false);
        synth.open();
        sequencer.open();
        sequencer.setSequence(sequence);

        Transmitter transmitter = sequencer.getTransmitter();
        Receiver receiver = synth.getReceiver();
        transmitter.setReceiver(receiver);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, DEFAULT_FORMAT);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(DEFAULT_FORMAT);
        line.start();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        Thread captureThread = new Thread(() -> {
            while (sequencer.isRunning()) {
			    int read = line.read(buffer, 0, buffer.length);
			    if (read > 0) baos.write(buffer, 0, read);
			}
        }, "MidiCaptureThread");

        captureThread.start();
        sequencer.start();

        while (sequencer.isRunning()) {
            Thread.sleep(50);
        }

        sequencer.stop();
        line.stop();
        line.close();
        sequencer.close();
        synth.close();

        captureThread.join();
        return baos.toByteArray();
    }

    /**
     * Converts raw PCM bytes into an AudioInputStream.
     */
    public static AudioInputStream toAudioStream(byte[] pcmData) {
        return new AudioInputStream(
                new ByteArrayInputStream(pcmData),
                DEFAULT_FORMAT,
                pcmData.length / DEFAULT_FORMAT.getFrameSize()
        );
    }

    /**
     * Writes a PCM byte array to a WAV file.
     */
    public static void writePCMToWav(byte[] pcmData, File wavFile) throws IOException {
        try (AudioInputStream ais = toAudioStream(pcmData)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
        }
    }
}
