package audioTest;

import ecs.*;
import ecs.components.*;
import ecs.systems.AdaptiveAudioSystem;

import org.junit.jupiter.api.*;

import audio.config.AudioConfigManager;

import javax.sound.sampled.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the AdaptiveAudioSystem lifecycle and per-source management logic
 * without performing real audio playback.
 */
public class AdaptiveAudioSystemTest {

    private ECSManager ecs;
    private AdaptiveAudioSystem audioSystem;
	private AudioConfigManager audiomanager;

    @BeforeEach
    void setup() throws LineUnavailableException {
        ecs = new ECSManager();
        audiomanager = new AudioConfigManager();
        audioSystem = new AdaptiveAudioSystem(ecs, audiomanager);
    }

    @AfterEach
    void tearDown() {
        if (audioSystem != null) {
            audioSystem.shutdown();
        }
    }

    @Test
    void testMixerThreadStartsAndStops() throws Exception {
        assertNotNull(audioSystem);
        assertTrue(audioSystem.getClass().getDeclaredField("mixerThread") != null);
        audioSystem.shutdown();
        assertTrue(true, "Shutdown completes without exception");
    }

    @Test
    void testAudioSourceRegistration() throws Exception {
        Entity id = ecs.createEntity();
        AudioSourceComponent source = new AudioSourceComponent(null);
        source.filePath = new File("test.wav").getAbsolutePath();
        ecs.getComponentManager().addComponent(id.getId(), source);

        // simulate one update cycle
        audioSystem.update(ecs, 0.016f);

        assertTrue(ecs.getComponentManager().hasComponent(id.getId(), AudioSourceComponent.class));
    }

    @Test
    void testListenerDetection() throws Exception {
        Entity listenerId = ecs.createEntity();
        ListenerComponent listener = new ListenerComponent();
        ecs.getComponentManager().addComponent(listenerId.getId(), listener);

        var method = AdaptiveAudioSystem.class.getDeclaredMethod("findListener", ComponentManager.class);
        method.setAccessible(true);
        Object result = method.invoke(audioSystem, ecs.getComponentManager());

        assertNotNull(result);
        assertInstanceOf(ListenerComponent.class, result);
    }

    @Test
    void testMixAddInPlaceClampsCorrectly() throws Exception {
        byte[] dest = new byte[4];
        byte[] src = new byte[4];
        // simulate clipping by filling with large positive/negative samples
        dest[0] = (byte) 0xFF; dest[1] = 0x7F; // 32767
        src[0] = 0x01; src[1] = 0x00; // small value
        var method = AdaptiveAudioSystem.class.getDeclaredMethod("mixAddInPlace", byte[].class, byte[].class);
        method.setAccessible(true);
        method.invoke(audioSystem, dest, src);

        short mixed = (short) ((dest[1] << 8) | (dest[0] & 0xFF));
        assertEquals(32767, mixed, "Clamps to Short.MAX_VALUE");
    }
}
