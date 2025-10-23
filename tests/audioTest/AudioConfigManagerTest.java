package audioTest;

import org.junit.jupiter.api.*;

import audio.config.AudioConfigManager;
import audio.config.AudioProfile;
import audio.config.SoundConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests AudioConfigManager global state and persistence-like behavior.
 */
public class AudioConfigManagerTest {

    private AudioConfigManager mgr;

    @BeforeEach
    void setup() {
        mgr = new AudioConfigManager();
    }

    @Test
    void testDefaultConfigExists() {
        assertNotNull(mgr.getGlobalConfig());
        assertEquals(1.0f, mgr.getGlobalConfig().getMasterVolume());
    }

    @Test
    void testProfileRegistrationAndRetrieval() {
        AudioProfile p = new AudioProfile();
        p.setName("Dungeon");
        mgr.addProfile("Dungeon", p);

        assertTrue(mgr.hasProfile("Dungeon"));
        assertEquals(p, mgr.getProfile("Dungeon"));
    }

    @Test
    void testRemoveProfile() {
        mgr.addProfile("Forest", new AudioProfile());
        mgr.removeProfile("Forest");
        assertFalse(mgr.hasProfile("Forest"));
    }

    @Test
    void testCloneAndApplyConfig() {
        SoundConfig original = mgr.getGlobalConfig();
        original.setMusicVolume(0.2f);

        SoundConfig copy = new SoundConfig(original);
        copy.setMusicVolume(0.5f);
        mgr.applyConfig(copy);

        assertEquals(0.5f, mgr.getGlobalConfig().getMusicVolume());
    }

    @Test
    void testResetToDefaults() {
        mgr.getGlobalConfig().setMasterVolume(0.1f);
        mgr.resetToDefaults();
        assertEquals(1.0f, mgr.getGlobalConfig().getMasterVolume());
    }
}
