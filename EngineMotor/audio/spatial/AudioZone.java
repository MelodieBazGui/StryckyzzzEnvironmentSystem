package audio.spatial;

import audio.config.AudioProfile;
import math.Vec3;

import java.util.Objects;

/**
 * A 3D audio zone with either spherical or box shape.
 * Provides containment testing, smooth blend factor and attenuation.
 */
public class AudioZone {

    public enum ShapeType { SPHERE, BOX }
    public enum AttenuationModel { NONE, LINEAR, EXPONENTIAL, INVERSE_SQUARE }

    private final String name;
    private final Vec3 position;
    private final float radius;              // used also as default half-extent for box
    private final AudioProfile profile;
    private final ShapeType shape;

    private boolean active = true;
    private float priority = 1.0f;           // higher wins
    private float blendStrength = 1.0f;      // 0..1 multiplier on computed blend factor

    private Vec3 halfExtents;                // for BOX; defaults to (r,r,r)
    private AttenuationModel attenuationModel = AttenuationModel.LINEAR;
    private float minAttenuation = 0.1f;

    public AudioZone(String name, Vec3 position, float radius, AudioProfile profile) {
        this(name, position, radius, profile, ShapeType.SPHERE);
    }

    public AudioZone(String name, Vec3 position, float radius, AudioProfile profile, ShapeType shape) {
        this.name = name;
        this.position = position;
        this.radius = radius;
        this.profile = profile;
        this.shape = shape;
        this.halfExtents = new Vec3(radius, radius, radius);
    }

    // ---------------------------
    // Queries
    // ---------------------------

    public boolean contains(Vec3 listenerPos) {
        if (!active) return false;

        if (shape == ShapeType.SPHERE) {
            float r2 = radius * radius;
            return position.distanceSquared(listenerPos) <= r2;
        } else {
            Vec3 local = listenerPos.sub(position);
            return Math.abs(local.getX()) <= halfExtents.getX()
                && Math.abs(local.getY()) <= halfExtents.getY()
                && Math.abs(local.getZ()) <= halfExtents.getZ();
        }
    }

    /** 0..1 inside (quadratic ease toward center), 0 outside. */
    public float computeBlendFactor(Vec3 listenerPos) {
        if (!active) return 0f;

        if (shape == ShapeType.SPHERE) {
            float dist = position.distance(listenerPos);
            if (dist >= radius) return 0f;
            float t = 1f - (dist / radius);
            return Math.max(0f, Math.min(1f, t * t * blendStrength));
        } else {
            if (!contains(listenerPos)) return 0f;
            Vec3 local = listenerPos.sub(position);
            float dx = 1f - Math.abs(local.getX() / halfExtents.getX());
            float dy = 1f - Math.abs(local.getY() / halfExtents.getY());
            float dz = 1f - Math.abs(local.getZ() / halfExtents.getZ());
            float v = dx * dy * dz;
            if (v < 0f) v = 0f;
            if (v > 1f) v = 1f;
            return v * blendStrength;
        }
    }

    /** Gain 0..1 based on distance and selected model. */
    public float computeAttenuation(Vec3 listenerPos) {
        if (!active) return 0f;
        if (attenuationModel == AttenuationModel.NONE) return 1f;

        float effRadius = radius;
        if (shape == ShapeType.BOX) {
            // approximate with diagonal length / 2 (distance from center to corner)
            float dx = halfExtents.getX(), dy = halfExtents.getY(), dz = halfExtents.getZ();
            effRadius = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        float dist = position.distance(listenerPos);
        if (dist <= 0.001f) return 1f;

        float norm = Math.min(1f, dist / effRadius);

        switch (attenuationModel) {
            case LINEAR:
                return Math.max(minAttenuation, 1f - norm);
            case EXPONENTIAL:
                return Math.max(minAttenuation, (float) Math.exp(-3f * norm));
            case INVERSE_SQUARE:
                return Math.max(minAttenuation, 1f / (1f + (dist * dist / (effRadius * effRadius))));
            case NONE:
            default:
                return 1f;
        }
    }

    // ---------------------------
    // Getters / Setters
    // ---------------------------

    public String getName() { return name; }
    public Vec3 getPosition() { return position; }
    public float getRadius() { return radius; }
    public AudioProfile getProfile() { return profile; }
    public ShapeType getShape() { return shape; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public float getPriority() { return priority; }
    public void setPriority(float priority) { this.priority = Math.max(0f, priority); }

    public float getBlendStrength() { return blendStrength; }
    public void setBlendStrength(float blendStrength) {
        this.blendStrength = Math.max(0f, Math.min(1f, blendStrength));
    }

    public Vec3 getHalfExtents() { return halfExtents; }
    public void setHalfExtents(Vec3 halfExtents) { this.halfExtents = halfExtents; }

    public AttenuationModel getAttenuationModel() { return attenuationModel; }
    public void setAttenuationModel(AttenuationModel attenuationModel) { this.attenuationModel = attenuationModel; }

    public void setMinAttenuation(float min) { this.minAttenuation = Math.max(0f, Math.min(1f, min)); }

    // ---------------------------
    // Equality / Debug
    // ---------------------------

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
