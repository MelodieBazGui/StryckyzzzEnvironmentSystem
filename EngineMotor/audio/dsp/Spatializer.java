package audio.dsp;

import math.Vec3;

/**
 * Simple spatialization utilities:
 *  - distance attenuation (inverse-square with clamp)
 *  - stereo panning (angle-based)
 */
public final class Spatializer {

    /** Compute linear amplitude attenuation from distance */
    public static float distanceAttenuation(Vec3 listenerPos, Vec3 sourcePos, float maxDistance) {
        float dx = sourcePos.getX() - listenerPos.getX();
        float dy = sourcePos.getY() - listenerPos.getY();
        float dz = sourcePos.getZ() - listenerPos.getZ();
        float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (dist <= 0.0001f) return 1.0f;
        if (dist >= maxDistance) return 0f;
        // simple inverse-linear falloff (tweak as needed)
        float att = 1.0f - (dist / maxDistance);
        return Math.max(0f, att);
    }

    /** Compute stereo pan -1..1 based on horizontal angle to listener forward */
    public static float computePan(Vec3 listenerPos, Vec3 listenerForward, Vec3 sourcePos) {
        float lx = listenerForward.getX();
        float lz = listenerForward.getZ();
        float sx = sourcePos.getX() - listenerPos.getX();
        float sz = sourcePos.getZ() - listenerPos.getZ();
        // project onto horizontal plane and compute angle between forward and source vector
        float dot = lx * sx + lz * sz;
        float magA = (float) Math.sqrt(lx*lx + lz*lz);
        float magB = (float) Math.sqrt(sx*sx + sz*sz);
        if (magA * magB == 0f) return 0f;
        float cos = dot / (magA * magB);
        cos = Math.max(-1f, Math.min(1f, cos));
        float angle = (float) Math.acos(cos); // 0..pi
        // determine sign via cross
        float cross = lx * sz - lz * sx;
        float sign = (cross < 0) ? -1f : 1f;
        float pan = sign * (angle / (float) Math.PI) * 2f; // scale to -1..1
        pan = Math.max(-1f, Math.min(1f, pan));
        return pan;
    }

    /** Apply pan & overall gain to interleaved stereo 16-bit PCM buffer in-place.
     *  pan -1..1, gain 0..1
     */
    public static void applyPanAndGain(byte[] buffer, int offset, int length, float pan, float gain) {
        // convert pan to left/right multipliers using constant-power panning
        float angle = (pan + 1f) * 0.25f * (float) Math.PI; // -1 -> 0, +1 -> pi/2
        float leftGain = (float) Math.cos(angle) * gain;
        float rightGain = (float) Math.sin(angle) * gain;

        int idx = offset;
        while (idx < offset + length) {
            int lo = buffer[idx++] & 0xFF;
            int hi = buffer[idx++] & 0xFF;
            short sL = (short) ((hi << 8) | lo);

            lo = buffer[idx++] & 0xFF;
            hi = buffer[idx++] & 0xFF;
            short sR = (short) ((hi << 8) | lo);

            int newL = (int) (sL * leftGain);
            int newR = (int) (sR * rightGain);

            buffer[idx - 4] = (byte) (newL & 0xFF);
            buffer[idx - 3] = (byte) ((newL >> 8) & 0xFF);
            buffer[idx - 2] = (byte) (newR & 0xFF);
            buffer[idx - 1] = (byte) ((newR >> 8) & 0xFF);
        }
    }
}

