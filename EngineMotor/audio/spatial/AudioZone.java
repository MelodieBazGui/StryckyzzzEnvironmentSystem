package audio.spatial;

import math.Vec3;
import audio.config.AudioProfile;
import java.util.Objects;

/**
 * Represents a spatial audio zone in 3D space.
 *
 * Zones define localized acoustic environments (e.g. cave, hallway, outdoors)
 * with a radius or bounding box and an AudioProfile that modifies sound
 * behavior when the listener is within or near the zone.
 *
 * Supports:
 *  - Sphere or box zone shapes
 *  - Smooth distance-based blending (fade in/out)
 *  - Attenuation models for realistic falloff
 *  - Zone priorities for overlap resolution
 *  - Activation and blending control
 */
public class AudioZone {

    /** Supported geometric shapes for spatial zones. */
    public enum ShapeType { SPHERE, BOX }

    /** Supported attenuation models. */
    public enum AttenuationModel { NONE, LINEAR, EXPONENTIAL, INVERSE_SQUARE }

    private final String name;
    private final Vec3 position;
    private final float radius;
    private final AudioProfile profile;
    private final ShapeType shape;

    private float blendStrength = 1.0f; // 0..1: how strongly this zone influences audio
    private float priority = 1.0f;      // higher priority wins in overlapping zones
    private boolean active = true;
    private Vec3 halfExtents;           // Used for box-shaped zones
    private AttenuationModel attenuationModel = AttenuationModel.LINEAR;
    private float minAttenuation = 0.1f; // Minimum gain when at edge

    /**
     * Create a spherical audio zone.
     */
    public AudioZone(String name, Vec3 position, float radius, AudioProfile profile) {
        this(name, position, radius, profile, ShapeType.SPHERE);
    }

    /**
     * Create a custom audio zone (sphere or box).
     */
    public AudioZone(String name, Vec3 position, float radius, AudioProfile profile, ShapeType shape) {
        this.name = name;
        this.position = position;
        this.radius = radius;
        this.profile = profile;
        this.shape = shape;
        this.halfExtents = new Vec3(radius, radius, radius);
    }

    /** For box zones, sets the half-extents (half width, height, depth). */
    public void setHalfExtents(Vec3 halfExtents) {
        this.halfExtents = halfExtents;
    }

    /** Returns whether the listener position is inside this zone. */
    public boolean contains(Vec3 listenerPos) {
        if (!active) return false;

        if (shape == ShapeType.SPHERE) {
            float distSq = position.distanceSquared(listenerPos);
            return distSq <= radius * radius;
        } else {
            Vec3 local = listenerPos.sub(position);
            return Math.abs(local.getX()) <= halfExtents.getX() &&
                   Math.abs(local.getY()) <= halfExtents.getY() &&
                   Math.abs(local.getZ()) <= halfExtents.getZ();
        }
    }

    /**
     * Computes a smooth falloff blend factor between 0 (outside) and 1 (inside).
     * Useful for gradual transitions between zones.
     */
    public float computeBlendFactor(Vec3 listenerPos) {
        if (!active) return 0f;

        if (shape == ShapeType.SPHERE) {
            float dist = position.distance(listenerPos);
            if (dist >= radius) return 0f;
            float t = 1f - (dist / radius);
            return (float) Math.pow(t, 2.0f) * blendStrength;
        } else {
            if (!contains(listenerPos)) return 0f;
            Vec3 local = listenerPos.sub(position);
            float dx = 1f - Math.abs(local.getX() / halfExtents.getX());
            float dy = 1f - Math.abs(local.getY() / halfExtents.getY());
            float dz = 1f - Math.abs(local.getZ() / halfExtents.getZ());
            return Math.max(0f, Math.min(1f, (dx * dy * dz) * blendStrength));
        }
    }

    /**
     * Calculates attenuation (gain multiplier between 0 and 1)
     * based on the selected model and listener position.
     */
    public float computeAttenuation(Vec3 listenerPos) {
        if (!active) return 0f;
        if (shape != ShapeType.SPHERE) {
            // Approximate attenuation using box diagonal
            float maxRadius = (float) Math.sqrt(halfExtents.getX() * halfExtents.getX() +
                                                halfExtents.getY() * halfExtents.getY() +
                                                halfExtents.getZ() * halfExtents.getZ());
            return computeAttenuationSphere(listenerPos, maxRadius);
        }
        return computeAttenuationSphere(listenerPos, radius);
    }

    private float computeAttenuationSphere(Vec3 listenerPos, float effectiveRadius) {
        float dist = position.distance(listenerPos); // ✅ FIXED
        if (dist >= effectiveRadius) return minAttenuation;

        float norm = dist / effectiveRadius;

        switch (attenuationModel) {
            case NONE:
                return 1f;
            case LINEAR:
                return Math.max(minAttenuation, 1f - norm);
            case EXPONENTIAL:
                return Math.max(minAttenuation, (float) Math.exp(-3f * norm));
            case INVERSE_SQUARE:
                float factor = 1f / (1f + (dist * dist) / (effectiveRadius * effectiveRadius));
                return Math.max(minAttenuation, factor);
            default:
                return 1f;
        }
    }


    // ========================
    // Getters / Setters
    // ========================

    public AudioProfile getProfile() { return profile; }
    public String getName() { return name; }
    public Vec3 getPosition() { return position; }
    public float getRadius() { return radius; }
    public ShapeType getShape() { return shape; }
    public float getPriority() { return priority; }
    public boolean isActive() { return active; }
    public float getBlendStrength() { return blendStrength; }
    public Vec3 getHalfExtents() { return halfExtents; }
    public AttenuationModel getAttenuationModel() { return attenuationModel; }

    public void setPriority(float priority) { this.priority = Math.max(0f, priority); }
    public void setActive(boolean active) { this.active = active; }
    public void setBlendStrength(float blendStrength) {
        this.blendStrength = Math.max(0f, Math.min(1f, blendStrength));
    }
    public void setAttenuationModel(AttenuationModel model) { this.attenuationModel = model; }
    public void setMinAttenuation(float min) { this.minAttenuation = Math.max(0f, Math.min(1f, min)); }

    // ========================
    // Equality / Hashing
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioZone)) return false;
        AudioZone zone = (AudioZone) o;
        return Float.compare(zone.radius, radius) == 0 &&
               Objects.equals(name, zone.name) &&
               Objects.equals(position, zone.position) &&
               shape == zone.shape &&
               Objects.equals(profile, zone.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position, radius, shape, profile);
    }

    @Override
    public String toString() {
        return "AudioZone[" + name +
               ", shape=" + shape +
               ", pos=" + position +
               ", radius=" + radius +
               ", active=" + active + "]";
    }
}
