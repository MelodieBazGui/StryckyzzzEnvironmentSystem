package audioTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import audio.config.AudioProfile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AudioProfile.
 * Covers parameter clamping, EQ management, blending, and equality.
 *
 * Author: EmeJay, 2025
 */
public class AudioProfileTest {

    private AudioProfile profile;

    @BeforeEach
    void setup() {
        profile = new AudioProfile();
    }

    // =============================
    // Basic Getters/Setters
    // =============================

    @Test
    void testDefaultValues() {
        assertEquals(1.0f, profile.getVolume());
        assertEquals(0.3f, profile.getReverbMix());
        assertEquals(20000f, profile.getLowPassCutoff());
        assertEquals(20f, profile.getHighPassCutoff());
        assertEquals(1.0f, profile.getDynamicRange());
        assertEquals(1.0f, profile.getSpatialBlend());
        assertEquals(1.0f, profile.getDistanceRolloff());
        assertEquals(50.0f, profile.getMaxDistance());
    }

    @Test
    void testClamping() {
        profile.setVolume(5f);
        profile.setReverbMix(-1f);
        profile.setLowPassCutoff(-100f);
        profile.setHighPassCutoff(99999f);
        profile.setDynamicRange(5f);
        profile.setSpatialBlend(5f);
        profile.setDistanceRolloff(0f);
        profile.setMaxDistance(-5f);

        assertEquals(2f, profile.getVolume());
        assertEquals(0f, profile.getReverbMix());
        assertEquals(20f, profile.getLowPassCutoff());
        assertEquals(20000f, profile.getHighPassCutoff());
        assertEquals(2f, profile.getDynamicRange());
        assertEquals(1f, profile.getSpatialBlend());
        assertEquals(0.1f, profile.getDistanceRolloff());
        assertEquals(1f, profile.getMaxDistance());
    }

    // =============================
    // EQ Curve Tests
    // =============================

    @Test
    void testEqCurveAddAndGet() {
        profile.setEqPoint(100f, 3f);
        profile.setEqPoint(1000f, -6f);

        Map<Float, Float> eq = profile.getEqCurve();
        assertEquals(2, eq.size());
        assertEquals(3f, eq.get(100f));
        assertEquals(-6f, eq.get(1000f));
    }

    @Test
    void testEqCurveClamp() {
        profile.setEqPoint(200f, 100f);
        profile.setEqPoint(400f, -100f);

        assertEquals(24f, profile.getEqCurve().get(200f));
        assertEquals(-24f, profile.getEqCurve().get(400f));
    }

    @Test
    void testEqCurveRemoveAndClear() {
        profile.setEqPoint(500f, 2f);
        assertEquals(1, profile.getEqCurve().size());

        profile.removeEqPoint(500f);
        assertTrue(profile.getEqCurve().isEmpty());

        profile.setEqPoint(800f, 5f);
        profile.clearEqCurve();
        assertTrue(profile.getEqCurve().isEmpty());
    }

    // =============================
    // Metadata
    // =============================

    @Test
    void testNameAndEnvironment() {
        profile.setName("Cave");
        profile.setEnvironmentType("Underwater");
        assertEquals("Cave", profile.getName());
        assertEquals("Underwater", profile.getEnvironmentType());
    }

    @Test
    void testNullName() {
        profile.setName(null);
        assertEquals("Unnamed", profile.getName());
    }

    // =============================
    // Blend Tests
    // =============================

    @Test
    void testBlendProfiles() {
        AudioProfile a = new AudioProfile();
        a.setVolume(1f);
        a.setReverbMix(0.2f);
        a.setLowPassCutoff(1000f);
        a.setHighPassCutoff(200f);
        a.setDynamicRange(0.8f);
        a.setSpatialBlend(0.5f);
        a.setDistanceRolloff(1f);
        a.setMaxDistance(10f);
        a.setName("A");
        a.setEnvironmentType("Indoor");
        a.setEqPoint(100f, 3f);

        AudioProfile b = new AudioProfile();
        b.setVolume(0f);
        b.setReverbMix(1f);
        b.setLowPassCutoff(10000f);
        b.setHighPassCutoff(500f);
        b.setDynamicRange(1.2f);
        b.setSpatialBlend(1f);
        b.setDistanceRolloff(2f);
        b.setMaxDistance(100f);
        b.setName("B");
        b.setEnvironmentType("Outdoor");
        b.setEqPoint(100f, -3f);

        AudioProfile blended = a.blend(b, 0.5f);

        assertEquals(0.5f, blended.getVolume(), 1e-6);
        assertEquals(0.6f, blended.getReverbMix(), 1e-6);
        assertEquals(5500f, blended.getLowPassCutoff(), 1e-6);
        assertEquals(350f, blended.getHighPassCutoff(), 1e-6);
        assertEquals(1.0f, blended.getDynamicRange(), 1e-6);
        assertEquals(0.75f, blended.getSpatialBlend(), 1e-6);
        assertEquals(1.5f, blended.getDistanceRolloff(), 1e-6);
        assertEquals(55f, blended.getMaxDistance(), 1e-6);
        assertEquals("Outdoor", blended.getEnvironmentType()); // t=0.5 → other wins
        assertEquals("A_blend_B", blended.getName());
        assertEquals(0f, blended.getEqCurve().get(100f), 1e-6);
    }

    @Test
    void testBlendClampT() {
        AudioProfile a = new AudioProfile();
        a.setVolume(0f);
        AudioProfile b = new AudioProfile();
        b.setVolume(2f);

        AudioProfile blendBelow0 = a.blend(b, -1f);
        AudioProfile blendAbove1 = a.blend(b, 2f);

        assertEquals(0f, blendBelow0.getVolume());
        assertEquals(2f, blendAbove1.getVolume());
    }

    // =============================
    // Equality
    // =============================

    @Test
    void testEqualsAndHashCode() {
        AudioProfile a = new AudioProfile();
        a.setVolume(0.9f);
        a.setEqPoint(100f, 5f);

        AudioProfile b = new AudioProfile(a);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToStringContainsKeyValues() {
        String str = profile.toString();
        assertTrue(str.contains("vol"));
        assertTrue(str.contains("reverb"));
        assertTrue(str.contains("LP"));
        assertTrue(str.contains("HP"));
    }
}
