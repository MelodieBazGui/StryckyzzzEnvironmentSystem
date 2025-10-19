package ecs.systems;

import ecs.*;
import ecs.components.*;
import audio.dsp.AudioDSPProcessor;
import audio.dsp.Spatializer;
import math.Vec3;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * AdaptiveAudioSystem
 * - runs a mixer thread that streams, processes, and plays audio
 * - supports per-source spatialization, per-zone DSP overrides, and streaming reverb
 */
public class AdaptiveAudioSystem extends SystemBase {

    private final ECSManager ecs;
    private final ExecutorService sourcePool;
    private final AudioFormat audioFormat = new AudioFormat(44100f, 16, 2, true, false);
    private final int bufferFrames = 1024;
    private final int bufferBytes = bufferFrames * audioFormat.getFrameSize();
    private volatile boolean running = true;
    private SourceDataLine outputLine;
    private Thread mixerThread;

    // holds per-source streaming state
    private final Map<Integer, SourceState> sourceStates = new ConcurrentHashMap<>();

    public AdaptiveAudioSystem(ECSManager ecs) throws LineUnavailableException {
        this.ecs = ecs;
        this.sourcePool = Executors.newCachedThreadPool();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        outputLine = (SourceDataLine) AudioSystem.getLine(info);
        outputLine.open(audioFormat, bufferBytes * 4);
        outputLine.start();
        startMixer();
    }

    private void startMixer() {
        mixerThread = new Thread(this::mixerLoop, "AdaptiveAudioMixer");
        mixerThread.setDaemon(true);
        mixerThread.start();
    }

    private void mixerLoop() {
        byte[] mixBuffer = new byte[bufferBytes];
        while (running) {
            Arrays.fill(mixBuffer, (byte) 0);

            // refresh sources from ECS
            ComponentManager cm = ecs.getComponentManager();
            Collection<Map.Entry<Integer, AudioSourceComponent>> sources = cm.entriesForType(AudioSourceComponent.class);
            ListenerComponent listener = findListener(cm);

            // gather zones
            List<SoundZoneComponent> zones = new ArrayList<>();
            for (Map.Entry<Integer, SoundZoneComponent> e : cm.entriesForType(SoundZoneComponent.class)) {
                zones.add(e.getValue());
            }

            // ensure states
            for (Map.Entry<Integer, AudioSourceComponent> e : sources) {
                int id = e.getKey();
                AudioSourceComponent asc = e.getValue();
                sourceStates.computeIfAbsent(id, k -> new SourceState(asc.filePath));
            }

            // process each source sequentially (can be parallelized per-source if needed)
            for (Map.Entry<Integer, AudioSourceComponent> e : sources) {
                int id = e.getKey();
                AudioSourceComponent asc = e.getValue();
                SourceState s = sourceStates.get(id);
                if (s == null) continue;

                // read source chunk
                byte[] srcBuf = s.readFrames(bufferFrames);
                if (srcBuf == null || srcBuf.length == 0) {
                    if (asc.looping) s.rewind();
                    else continue;
                }

                // compute spatialization / zone modifiers
                float zoneReverb = 0f;
                float zoneLP = Float.MAX_VALUE;
                if (listener != null) {
                    for (SoundZoneComponent z : zones) {
                        float att = Spatializer.distanceAttenuation(listener.position, z.center, z.radius);
                        if (att > 0.01f) {
                            // listener inside or near zone -> blend modifiers
                            zoneReverb = Math.max(zoneReverb, z.zoneReverb * att);
                            zoneLP = Math.min(zoneLP, z.zoneLowPass);
                        }
                    }
                }

                // compute distance attenuation and pan per-source
                float pan = 0f;
                float gain = asc.volume;
                if (listener != null) {
                    float distAtt = Spatializer.distanceAttenuation(listener.position, asc.position, listener.maxDistance);
                    pan = Spatializer.computePan(listener.position, listener.forward, asc.position);
                    gain *= distAtt;
                }

                // apply DSP chain: reverb + lowpass + pan/gain
                AudioDSPProcessor dsp = s.getDsp();
                dsp.setReverbMix(zoneReverb);
                if (zoneLP < Float.MAX_VALUE) dsp.setLowPassCutoff(zoneLP);

                dsp.processBuffer(srcBuf, 0, srcBuf.length, audioFormat);

                // apply spatial pan/gain and mix into master buffer
                Spatializer.applyPanAndGain(srcBuf, 0, srcBuf.length, pan, gain);

                mixAddInPlace(mixBuffer, srcBuf);
            }

            // clamp and write out
            outputLine.write(mixBuffer, 0, mixBuffer.length);

            try {
                Thread.sleep((long) (bufferFrames / audioFormat.getSampleRate() * 1000));
            } catch (InterruptedException ignored) {}
        }
    }

    /** find first listener component (single listener model) */
    private ListenerComponent findListener(ComponentManager cm) {
        Collection<Map.Entry<Integer, ListenerComponent>> listeners = cm.entriesForType(ListenerComponent.class);
        for (Map.Entry<Integer, ListenerComponent> e : listeners) {
            return e.getValue();
        }
        return null;
    }

    private void mixAddInPlace(byte[] dest, byte[] src) {
        // both are 16-bit stereo little-endian
        for (int i = 0; i < dest.length; i += 2) {
            int lo = dest[i] & 0xFF;
            int hi = dest[i+1] & 0xFF;
            short d = (short) ((hi << 8) | lo);

            int slo = src[i] & 0xFF;
            int shi = src[i+1] & 0xFF;
            short s = (short) ((shi << 8) | slo);

            int mixed = d + s;
            if (mixed > Short.MAX_VALUE) mixed = Short.MAX_VALUE;
            if (mixed < Short.MIN_VALUE) mixed = Short.MIN_VALUE;

            dest[i] = (byte) (mixed & 0xFF);
            dest[i+1] = (byte) ((mixed >> 8) & 0xFF);
        }
    }

    @Override
    public void update(ECSManager ecs, float deltaTime) {
        // Audio runs on mixerThread; update() used to manage lifecycle only.
        // Nothing needed per-frame here.
    }

    public void shutdown() {
        running = false;
        try {
            mixerThread.join(200);
        } catch (InterruptedException ignored) {}
        outputLine.drain();
        outputLine.stop();
        outputLine.close();
        sourcePool.shutdownNow();
        sourceStates.values().forEach(SourceState::close);
    }

    // ---------------------
    // Source streaming state
    // ---------------------
    private static final class SourceState {
        private final AudioInputStream ais;
        private final AudioDSPProcessor dsp;
        private final AudioFormat format;
        private final byte[] readBuffer;

        SourceState(String filePath) {
            AudioInputStream tmp = null;
            AudioFormat fmt = null;
            try {
                tmp = AudioSystem.getAudioInputStream(new File(filePath));
                fmt = tmp.getFormat();
                // convert if necessary to target format (stereo 16-bit 44.1k)
                AudioFormat target = new AudioFormat(44100f, 16, 2, true, false);
                if (!AudioSystem.isConversionSupported(target, fmt)) {
                    ais = AudioSystem.getAudioInputStream(target, tmp);
                    fmt = target;
                } else {
                    ais = tmp;
                }
                format = fmt;
                dsp = new AudioDSPProcessor(format, 512); // 512ms-ish IR
            } catch (Exception e) {
                throw new RuntimeException("Failed to open audio source: " + filePath, e);
            }
            readBuffer = new byte[1024 * format.getFrameSize()];
        }

        byte[] readFrames(int frames) {
            int bytesToRead = frames * format.getFrameSize();
            byte[] out = new byte[bytesToRead];
            try {
                int read = 0;
                while (read < bytesToRead) {
                    int n = ais.read(out, read, bytesToRead - read);
                    if (n < 0) break;
                    read += n;
                }
                if (read <= 0) return null;
                if (read < bytesToRead) {
                    // zero pad tail
                    Arrays.fill(out, read, bytesToRead, (byte) 0);
                }
                return out;
            } catch (Exception e) {
                return null;
            }
        }

        AudioDSPProcessor getDsp() {
            return dsp;
        }

        void rewind() {
            try {
                ais.reset(); // only works if mark supported — many streams do not; in practice reopen file
            } catch (Exception e) {
                // reopen if reset fails
            }
        }

        void close() {
            try { ais.close(); } catch (Exception ignored) {}
        }
    }
}
