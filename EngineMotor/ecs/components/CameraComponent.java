package ecs.components;

import ecs.Component;
import math.*;

/**
 * ECS camera component.
 * 
 * Stores view and projection parameters for 3D rendering.
 * Integrates seamlessly with Mat4 and Quat-based math.
 * 
 * Works in a right-handed coordinate system:
 * - Forward: -Z
 * - Up: +Y
 * - Right: +X
 */
public final class CameraComponent implements Component {

    // --- Spatial data ---
    public final Vec3 position = new Vec3();
    public final Quat rotation = new Quat(1, 0, 0, 0);

    // --- Projection data ---
    public float fovY = (float) Math.toRadians(60f);
    public float aspect = 16f / 9f;
    public float near = 0.1f;
    public float far = 1000f;

    // --- Cached matrices ---
    private final Mat4 view = new Mat4();
    private final Mat4 projection = new Mat4();

    public CameraComponent() {}

    /** Builds and returns the current view matrix. */
    public Mat4 viewMatrix() {
        // inverse of TRS(position, rotation, scale=1)
        Quat invRot = rotation.inverse();
        Vec3 invPos = rotation.invTransform(position).negate();

        Mat4 rotM = Mat4.fromQuat(invRot);
        Mat4 transM = Mat4.fromTranslation(invPos);

        return transM.mul(rotM);
    }

    /** Builds and returns the current projection matrix. */
    public Mat4 projectionMatrix() {
        return Mat4.perspective(fovY, aspect, near, far);
    }

    /** Returns combined view-projection matrix. */
    public Mat4 viewProjectionMatrix() {
        return projectionMatrix().mul(viewMatrix());
    }

    // --- Utility setters ---
    public CameraComponent setPerspective(float fovYRadians, float aspect, float near, float far) {
        this.fovY = fovYRadians;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
        return this;
    }

    public CameraComponent lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        position.set(eye);
        Vec3 forward = Vec3.sub(target, eye).normalize();
        Vec3 right = Vec3.cross(up, forward).normalize();
        Vec3 correctedUp = Vec3.cross(forward, right).normalize();

        // Construct rotation quaternion from basis
        float m00 = right.getX(), m01 = right.getY(), m02 = right.getZ();
        float m10 = correctedUp.getX(), m11 = correctedUp.getY(), m12 = correctedUp.getZ();
        float m20 = -forward.getX(), m21 = -forward.getY(), m22 = -forward.getZ();

        float trace = m00 + m11 + m22;
        Quat q = new Quat();
        if (trace > 0) {
            float s = (float)Math.sqrt(trace + 1.0f) * 2f;
            q = new Quat(
                0.25f * s,
                (m21 - m12) / s,
                (m02 - m20) / s,
                (m10 - m01) / s
            );
        } else {
            // fallback (rare)
            q = new Quat(1,0,0,0);
        }

        rotation.set(q).normalize();
        return this;
    }

    /** Returns the camera's forward vector (-Z). */
    public Vec3 forward() {
        return rotation.transform(new Vec3(0, 0, -1));
    }

    /** Returns the camera's up vector (+Y). */
    public Vec3 up() {
        return rotation.transform(new Vec3(0, 1, 0));
    }

    /** Returns the camera's right vector (+X). */
    public Vec3 right() {
        return rotation.transform(new Vec3(1, 0, 0));
    }
}

