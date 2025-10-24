package audioTest;

import audio.config.AudioProfile;
import audio.spatial.AudioZone;
import audio.spatial.AudioZone.AttenuationModel;
import audio.spatial.AudioZone.ShapeType;
import math.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link AudioZone}.
 *
 * Covers:
 *  - Sphere and box containment
 *  - Blend factor falloff
 *  - Multiple attenuation models
 *  - Active/inactive behavior
 *  - Equality and hash consistency
 */
public class AudioZoneTest {

    private AudioProfile profile;
    private AudioZone zone;
    private Vec3 center;

    @BeforeEach
    void setup() {
        profile = new AudioProfile("CaveProfile");
        center = new Vec3(0, 0, 0);
        zone = new AudioZone("TestZone", center, 10f, profile);
    }

    // -------------------------------------------------------
    // Containment and shape tests
    // -------------------------------------------------------

    @Test
    void testContainsInsideSphere() {
        Vec3 inside = new Vec3(0, 0, 5);
        assertTrue(zone.contains(inside), "Point within radius should be inside");
    }

    @Test
    void testContainsOutsideSphere() {
        Vec3 outside = new Vec3(0, 0, 15);
        assertFalse(zone.contains(outside), "Point outside radius should not be inside");
    }

    @Test
    void testBoxContainsLogic() {
        AudioZone boxZone = new AudioZone("BoxZone", center, 5f, profile, ShapeType.BOX);
        boxZone.setHalfExtents(new Vec3(5, 5, 5));

        assertTrue(boxZone.contains(new Vec3(3, 3, 3)), "Point within box bounds should be inside");
        assertFalse(boxZone.contains(new Vec3(6, 0, 0)), "Point outside box bounds should be outside");
    }

    // -------------------------------------------------------
    // Blend factor tests
    // -------------------------------------------------------

    @Test
    void testBlendFactorAtCenterIsMax() {
        float blend = zone.computeBlendFactor(center);
        assertEquals(1.0f, blend, 1e-6, "Center point should have full blend factor");
    }

    @Test
    void testBlendFactorAtEdgeIsZero() {
        Vec3 edge = new Vec3(10, 0, 0);
        float blend = zone.computeBlendFactor(edge);
        assertEquals(0f, blend, 1e-6, "Blend factor should drop to zero at radius edge");
    }

    @Test
    void testBlendFactorSmoothFalloff() {
        Vec3 mid = new Vec3(5, 0, 0);
        float blend = zone.computeBlendFactor(mid);
        assertTrue(blend > 0 && blend < 1, "Blend factor should smoothly fall off inside zone");
    }

    // -------------------------------------------------------
    // Attenuation model tests
    // -------------------------------------------------------

    @Test
    void testAttenuationNone() {
        zone.setAttenuationModel(AttenuationModel.NONE);
        float gain = zone.computeAttenuation(new Vec3(9, 0, 0));
        assertEquals(1.0f, gain, 1e-6, "No attenuation model should always return full gain");
    }

    @Test
    void testAttenuationLinear() {
        zone.setAttenuationModel(AttenuationModel.LINEAR);
        float near = zone.computeAttenuation(new Vec3(1, 0, 0));
        float mid = zone.computeAttenuation(new Vec3(5, 0, 0));
        float far = zone.computeAttenuation(new Vec3(9, 0, 0));

        assertTrue(near > mid && mid > far, "Linear attenuation should decrease with distance");
    }

    @Test
    void testAttenuationExponential() {
        zone.setAttenuationModel(AttenuationModel.EXPONENTIAL);
        float near = zone.computeAttenuation(new Vec3(1, 0, 0));
        float mid = zone.computeAttenuation(new Vec3(5, 0, 0));
        float far = zone.computeAttenuation(new Vec3(9, 0, 0));

        assertTrue(near > mid && mid > far, "Exponential attenuation should fall off rapidly with distance");
    }

    @Test
    void testAttenuationInverseSquare() {
        zone.setAttenuationModel(AttenuationModel.INVERSE_SQUARE);
        float near = zone.computeAttenuation(new Vec3(1, 0, 0));
        float mid = zone.computeAttenuation(new Vec3(5, 0, 0));
        float far = zone.computeAttenuation(new Vec3(9, 0, 0));

        assertTrue(near > mid && mid > far, "Inverse-square attenuation should decrease with squared distance");
    }

    @Test
    void testAttenuationClampedToMin() {
        zone.setAttenuationModel(AttenuationModel.LINEAR);
        zone.setMinAttenuation(0.3f);
        float gain = zone.computeAttenuation(new Vec3(20, 0, 0)); // outside radius
        assertEquals(0.3f, gain, 1e-6, "Gain should never drop below min attenuation");
    }

    // -------------------------------------------------------
    // Active and inactive states
    // -------------------------------------------------------

    @Test
    void testInactiveZoneReturnsZeroGainAndBlend() {
        zone.setActive(false);
        Vec3 pos = new Vec3(0, 0, 0);

        assertFalse(zone.contains(pos), "Inactive zone should not contain any point");
        assertEquals(0f, zone.computeBlendFactor(pos), "Blend factor should be zero for inactive zone");
        assertEquals(0f, zone.computeAttenuation(pos), "Attenuation should be zero for inactive zone");
    }

    // -------------------------------------------------------
    // Equality and hashcode
    // -------------------------------------------------------

    @Test
    void testEqualityAndHashCode() {
        AudioZone same = new AudioZone("TestZone", new Vec3(0, 0, 0), 10f, profile);
        AudioZone different = new AudioZone("OtherZone", new Vec3(1, 1, 1), 5f, profile);

        assertEquals(zone, same, "Zones with same parameters should be equal");
        assertEquals(zone.hashCode(), same.hashCode(), "Equal zones should have same hashCode");
        assertNotEquals(zone, different, "Zones with different attributes should not be equal");
    }

    // -------------------------------------------------------
    // Box attenuation tests
    // -------------------------------------------------------

    @Test
    void testBoxAttenuationApproximation() {
        AudioZone boxZone = new AudioZone("BoxZone", center, 5f, profile, ShapeType.BOX);
        boxZone.setHalfExtents(new Vec3(5, 5, 5));
        boxZone.setAttenuationModel(AttenuationModel.LINEAR);

        float centerGain = boxZone.computeAttenuation(new Vec3(0, 0, 0));
        float edgeGain = boxZone.computeAttenuation(new Vec3(5, 0, 0));

        assertTrue(centerGain > edgeGain, "Gain should be higher at the center of the box zone");
    }

    // -------------------------------------------------------
    // Misc: ToString and debug info
    // -------------------------------------------------------

    @Test
    void testToStringContainsNameAndShape() {
        String result = zone.toString();
        assertTrue(result.contains("TestZone"));
        assertTrue(result.contains("SPHERE"));
    }
}
