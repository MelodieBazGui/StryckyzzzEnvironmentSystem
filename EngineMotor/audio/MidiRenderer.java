package audio;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.*;

/**
 * Renders a MIDI file to a WAV file using the default Java synthesizer.
 * Works in JDK 17+ / 21 SE without relying on Gervill.
 */
public final class MidiRenderer {

    private MidiRenderer() {}

    public static void renderToWav(File midiFile, File wavFile, float sampleRate) throws Exception {
        Sequence sequence = MidiSystem.getSequence(midiFile);

        // Use default Synthesizer and Sequencer
        Synthesizer synth = MidiSystem.getSynthesizer();
        Sequencer sequencer = MidiSystem.getSequencer(false);
        synth.open();
        sequencer.open();
        sequencer.setSequence(sequence);

        // Connect sequencer → synth
        Transmitter transmitter = sequencer.getTransmitter();
        Receiver receiver = synth.getReceiver();
        transmitter.setReceiver(receiver);

        // Prepare audio capture
        AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thread captureThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (sequencer.isRunning()) {
			    int read = line.read(buffer, 0, buffer.length);
			    if (read > 0) baos.write(buffer, 0, read);
			}
        });

        // Start playback and capture
        captureThread.start();
        sequencer.start();

        while (sequencer.isRunning()) {
            Thread.sleep(50);
        }

        line.stop();
        line.close();
        captureThread.join();

        byte[] audioData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);

        sequencer.close();
        synth.close();
    }
}
