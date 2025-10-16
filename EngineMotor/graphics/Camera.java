package graphics;

import math.Vec3;
import utils.Logger;

/**
 * Simple 3D Camera with position, rotation (pitch, yaw, roll),
 * and directional vector updates.
 * Designed for integration with MouvementHandler & CameraMouvementHandler.
 * @author EmeJay
 */
public class Camera {
    private static final Logger logger = new Logger(Camera.class);

    private Vec3 position;
    private Vec3 rotation; // pitch (X), yaw (Y), roll (Z)
    private Vec3 forward;
    private Vec3 right;
    private Vec3 up;

    private boolean dirty; // if true, needs to recalculate direction vectors

    public Camera() {
        this.position = new Vec3(0, 0, 0);
        this.rotation = new Vec3(0, 0, 0);
        this.forward = new Vec3(0, 0, -1);
        this.right = new Vec3(1, 0, 0);
        this.up = new Vec3(0, 1, 0);
        this.dirty = true;
        updateVectors();
        logger.info("Camera initialized at origin with default orientation");
    }

    // -----------------------------
    // Position / Rotation setters
    // -----------------------------

    public void setPosition(Vec3 pos) {
        this.position.set(pos);
    }

    public void move(Vec3 delta) {
        this.position.add(delta);
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setRotation(Vec3 rot) {
        this.rotation.set(rot);
        this.dirty = true;
        updateVectors();
    }

    public Vec3 getRotation() {
        return rotation;
    }

    public void addRotation(Vec3 delta) {
        this.rotation.add(delta);
        this.dirty = true;
        updateVectors();
    }

    // -----------------------------
    // Direction vectors
    // -----------------------------

    /** Updates forward/right/up based on current yaw/pitch/roll. */
    private void updateVectors() {
        if (!dirty) return;
        float yawRad = (float) Math.toRadians(rotation.getY());
        float pitchRad = (float) Math.toRadians(rotation.getX());

        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);

        forward.set(sinYaw * cosPitch, sinPitch, -cosYaw * cosPitch).normalize();
        right.set(Vec3.cross(forward, new Vec3(0, 1, 0))).normalize();
        up.set(Vec3.cross(right, forward)).normalize();

        dirty = false;
    }

    public Vec3 getForward() {
        updateVectors();
        return forward;
    }

    public Vec3 getRight() {
        updateVectors();
        return right;
    }

    public Vec3 getUp() {
        updateVectors();
        return up;
    }

    // -----------------------------
    // Utility
    // -----------------------------

    public void reset() {
        position.zero();
        rotation.zero();
        dirty = true;
        updateVectors();
    }

    @Override
    public String toString() {
        return "Camera[pos=" + position + ", rot=" + rotation + "]";
    }
}
