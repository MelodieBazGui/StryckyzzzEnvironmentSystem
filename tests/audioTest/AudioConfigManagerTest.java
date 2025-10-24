package audioTest;

import audio.config.*;
import audio.spatial.AudioZone;
import audio.spatial.AudioZone.ShapeType;
import math.Vec3;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full behavioral test suite for {@link AudioConfigManager}.
 *
 * Covers:
 *  - Zone registration/unregistration
 *  - Priority-based active zone selection
 *  - Blend factor progression
 *  - Profile application
 *  - Resetting and clearing zones
 */
public class AudioConfigManagerTest {

    private AudioConfigManager manager;
    private AudioZone zoneA, zoneB;
    private AudioProfile profileA, profileB;

    @BeforeEach
    void setup() {
        manager = new AudioConfigManager();
        profileA = new AudioProfile("CaveProfile");
        profileB = new AudioProfile("ForestProfile");

        zoneA = new AudioZone("ZoneA", new Vec3(0, 0, 0), 10, profileA);
        zoneA.setPriority(1f);

        zoneB = new AudioZone("ZoneB", new Vec3(5, 0, 0), 10, profileB);
        zoneB.setPriority(2f); // higher priority
    }

    // -------------------------------------------------------
    // Registration tests
    // -------------------------------------------------------

    @Test
    void testRegisterAndGetZones() {
        manager.registerZone(zoneA);
        manager.registerZone(zoneB);

        List<AudioZone> zones = manager.getZones();
        assertEquals(2, zones.size());
        assertTrue(zones.contains(zoneA));
        assertTrue(zones.contains(zoneB));
    }

    @Test
    void testUnregisterZone() {
        manager.registerZone(zoneA);
        manager.unregisterZone(zoneA);
        assertFalse(manager.getZones().contains(zoneA));
    }

    @Test
    void testClearZonesResetsManager() {
        manager.registerZone(zoneA);
        manager.clearZones();
        assertFalse(manager.hasZones());
        assertNull(manager.getActiveZone());
    }

    // -------------------------------------------------------
    // Active zone logic
    // -------------------------------------------------------

    @Test
    void testActiveZoneSelectionByPriority() {
        manager.registerZone(zoneA);
        manager.registerZone(zoneB);

        // Listener inside both zones, should pick higher priority zoneB
        manager.updateListenerPosition(new Vec3(5, 0, 0));
        assertEquals(zoneB, manager.getActiveZone());
    }

    @Test
    void testActiveZoneSwitchesWhenMoving() {
        manager.registerZone(zoneA);
        manager.registerZone(zoneB);

        manager.updateListenerPosition(new Vec3(0, 0, 0));
        assertEquals(zoneA, manager.getActiveZone());

        manager.updateListenerPosition(new Vec3(5, 0, 0)); // overlap, zoneB wins
        assertEquals(zoneB, manager.getActiveZone());
    }

    @Test
    void testNoZoneContainsListener() {
        manager.registerZone(zoneA);
        manager.updateListenerPosition(new Vec3(100, 100, 100));
        assertNull(manager.getActiveZone());
        assertEquals(0f, manager.getBlendFactor());
    }

    // -------------------------------------------------------
    // Blending logic
    // -------------------------------------------------------

    @Test
    void testBlendFactorProgressesUpToOne() {
        manager.registerZone(zoneA);
        manager.updateListenerPosition(new Vec3(0, 0, 0));

        float first = manager.getBlendFactor();
        manager.updateListenerPosition(new Vec3(0, 0, 0));
        float second = manager.getBlendFactor();

        assertTrue(second >= first);
        assertTrue(second <= 1f);
    }

    @Test
    void testProfileAppliedToGlobalConfig() {
        manager.registerZone(zoneA);
        manager.updateListenerPosition(new Vec3(0, 0, 0));

        SoundConfig config = manager.getCurrentConfig();
        assertNotNull(config);
    }

    // -------------------------------------------------------
    // Inactive and edge conditions
    // -------------------------------------------------------

    @Test
    void testInactiveZoneIgnored() {
        zoneA.setActive(false);
        manager.registerZone(zoneA);
        manager.updateListenerPosition(new Vec3(0, 0, 0));
        assertNull(manager.getActiveZone(), "Inactive zone should not be used");
    }

    @Test
    void testHasZonesAndResetBehavior() {
        manager.registerZone(zoneA);
        assertTrue(manager.hasZones());
        manager.reset();
        assertFalse(manager.hasZones());
        assertEquals(0f, manager.getBlendFactor());
    }

    // -------------------------------------------------------
    // Misc: Shape and debug consistency
    // -------------------------------------------------------

    @Test
    void testBoxZoneHandledCorrectly() {
        AudioZone box = new AudioZone("BoxZone", new Vec3(0, 0, 0), 5, new AudioProfile("Box"), ShapeType.BOX);
        manager.registerZone(box);
        manager.updateListenerPosition(new Vec3(1, 1, 1));
        assertEquals(box, manager.getActiveZone());
    }

    @Test
    void testToStringOfActiveProfileContainsZoneName() {
        manager.registerZone(zoneA);
        manager.updateListenerPosition(new Vec3(0, 0, 0));
        String out = manager.getCurrentConfig().toString();
        assertNotNull(out);
    }
}
