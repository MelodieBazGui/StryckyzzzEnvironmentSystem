package audioTest;

import audio.config.AudioProfile;
import audio.spatial.AudioZone;
import audio.spatial.AudioZone.AttenuationModel;
import audio.spatial.AudioZone.ShapeType;
import math.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for AudioZone class including attenuation, blending,
 * containment, equality, and activation behavior.
 */
public class AudioZoneTest {

    @Test
    void testContainsSphere() {
        AudioProfile profile = new AudioProfile("cave");
        AudioZone zone = new AudioZone("ZoneA", new Vec3(0, 0, 0), 10f, profile);
        assertTrue(zone.contains(new Vec3(0, 0, 0)));
        assertTrue(zone.contains(new Vec3(5, 0, 0)));
        assertFalse(zone.contains(new Vec3(11, 0, 0)));
    }

    @Test
    void testContainsBox() {
        AudioProfile profile = new AudioProfile("room");
        AudioZone zone = new AudioZone("BoxZone", new Vec3(0, 0, 0), 10f, profile, ShapeType.BOX);
        zone.setHalfExtents(new Vec3(5, 5, 5));

        assertTrue(zone.contains(new Vec3(4.9f, 0, 0)));
        assertFalse(zone.contains(new Vec3(6, 0, 0)));
    }

    @Test
    void testBlendFactorSphere() {
        AudioProfile profile = new AudioProfile("forest");
        AudioZone zone = new AudioZone("ZoneBlend", new Vec3(0, 0, 0), 10f, profile);

        float inside = zone.computeBlendFactor(new Vec3(0, 0, 0));
        float mid = zone.computeBlendFactor(new Vec3(5, 0, 0));
        float edge = zone.computeBlendFactor(new Vec3(10, 0, 0));

        assertTrue(inside > mid && mid > edge);
        assertEquals(0f, edge, 0.001);
    }

    @Test
    void testInactiveZone() {
        AudioProfile profile = new AudioProfile("muted");
        AudioZone zone = new AudioZone("Inactive", new Vec3(0, 0, 0), 5f, profile);
        zone.setActive(false);
        assertFalse(zone.contains(new Vec3(0, 0, 0)));
        assertEquals(0f, zone.computeBlendFactor(new Vec3(0, 0, 0)));
        assertEquals(0f, zone.computeAttenuation(new Vec3(0, 0, 0)));
    }

    @Test
    void testEqualsAndHashCode() {
        AudioProfile p1 = new AudioProfile("hall");
        AudioProfile p2 = new AudioProfile("hall");
        AudioZone z1 = new AudioZone("Z", new Vec3(0, 0, 0), 5f, p1);
        AudioZone z2 = new AudioZone("Z", new Vec3(0, 0, 0), 5f, p2);

        assertEquals(z1, z2);
        assertEquals(z1.hashCode(), z2.hashCode());
    }

    @Test
    void testPriorityAndBlendStrength() {
        AudioZone zone = new AudioZone("PriorityTest", new Vec3(0, 0, 0), 10f, new AudioProfile("ambient"));
        zone.setPriority(5f);
        zone.setBlendStrength(0.6f);
        assertEquals(5f, zone.getPriority());
        assertEquals(0.6f, zone.getBlendStrength(), 0.001);
    }

    @Test
    void testAttenuationLinear() {
        AudioZone zone = new AudioZone("Linear", new Vec3(0, 0, 0), 10f, new AudioProfile("linear"));
        zone.setAttenuationModel(AttenuationModel.LINEAR);
        zone.setMinAttenuation(0.1f);

        float near = zone.computeAttenuation(new Vec3(1, 0, 0));
        float far = zone.computeAttenuation(new Vec3(9, 0, 0));

        assertTrue(near > far);
        assertTrue(far >= 0.1f && far <= 1f);
    }

    @Test
    void testAttenuationExponential() {
        AudioZone zone = new AudioZone("Expo", new Vec3(0, 0, 0), 10f, new AudioProfile("exp"));
        zone.setAttenuationModel(AttenuationModel.EXPONENTIAL);

        float center = zone.computeAttenuation(new Vec3(0, 0, 0));
        float far = zone.computeAttenuation(new Vec3(10, 0, 0));

        assertEquals(1f, center, 0.001);
        assertTrue(far < center);
    }

    @Test
    void testAttenuationInverseSquare() {
        AudioZone zone = new AudioZone("InvSq", new Vec3(0, 0, 0), 10f, new AudioProfile("inv"));
        zone.setAttenuationModel(AttenuationModel.INVERSE_SQUARE);

        float close = zone.computeAttenuation(new Vec3(1, 0, 0));
        float far = zone.computeAttenuation(new Vec3(10, 0, 0));

        assertTrue(close > far);
        assertTrue(far >= zone.getBlendStrength() * 0f);
    }

    @Test
    void testAttenuationNone() {
        AudioZone zone = new AudioZone("NoAtt", new Vec3(0, 0, 0), 10f, new AudioProfile("none"));
        zone.setAttenuationModel(AttenuationModel.NONE);
        assertEquals(1f, zone.computeAttenuation(new Vec3(100, 0, 0)));
    }

    @Test
    void testBoxAttenuationApproximation() {
        AudioZone zone = new AudioZone("Box", new Vec3(0, 0, 0), 5f, new AudioProfile("box"), ShapeType.BOX);
        zone.setHalfExtents(new Vec3(5, 5, 5));
        zone.setAttenuationModel(AttenuationModel.LINEAR);

        float center = zone.computeAttenuation(new Vec3(0, 0, 0));
        float edge = zone.computeAttenuation(new Vec3(5, 5, 5));

        assertTrue(center > edge);
        assertTrue(edge >= 0f);
    }
}
