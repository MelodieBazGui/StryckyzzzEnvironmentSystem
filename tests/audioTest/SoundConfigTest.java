package audioTest;

import org.junit.jupiter.api.*;

import audio.config.AudioProfile;
import audio.config.SoundConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the SoundConfig class: volumes, profiles, reverb, and environment profiles.
 */
public class SoundConfigTest {

    private SoundConfig config;

    @BeforeEach
    void setup() {
        config = new SoundConfig();
    }

    @Test
    void testVolumeSettersAndClamping() {
        config.setMasterVolume(1.5f);
        config.setMusicVolume(-1f);
        config.setSfxVolume(0.7f);
        config.setAmbientVolume(0.9f);
        config.setUiVolume(5f);
        assertEquals(1.0f, config.getMasterVolume());
        assertEquals(0.0f, config.getMusicVolume());
        assertEquals(0.7f, config.getSfxVolume());
        assertEquals(0.9f, config.getAmbientVolume());
        assertEquals(1.0f, config.getUiVolume());
    }

    @Test
    void testGlobalReverbLevel() {
        config.setGlobalReverbLevel(0.8f);
        assertEquals(0.8f, config.getGlobalReverbLevel());
    }

    @Test
    void testProfileManagement() {
        AudioProfile p1 = new AudioProfile();
        config.addProfile("Cave", p1);
        assertTrue(config.hasProfile("Cave"));
        assertEquals(p1, config.getProfile("Cave"));

        config.removeProfile("Cave");
        assertFalse(config.hasProfile("Cave"));
    }

    @Test
    void testCopyConstructor() {
        config.setMasterVolume(0.5f);
        config.setGlobalReverbLevel(0.9f);
        AudioProfile p = new AudioProfile();
        config.addProfile("Town", p);

        SoundConfig copy = new SoundConfig(config);
        assertEquals(0.5f, copy.getMasterVolume());
        assertEquals(0.9f, copy.getGlobalReverbLevel());
        assertTrue(copy.hasProfile("Town"));
    }

    @Test
    void testToStringContainsAllFields() {
        String str = config.toString();
        assertTrue(str.contains("master"));
        assertTrue(str.contains("music"));
        assertTrue(str.contains("reverb"));
    }

    @Test
    void testEnvironmentProfileSetters() {
        AudioProfile env = new AudioProfile();
        config.setEnvironmentProfile(env);
        assertEquals(env, config.getEnvironmentProfile());
    }
}
