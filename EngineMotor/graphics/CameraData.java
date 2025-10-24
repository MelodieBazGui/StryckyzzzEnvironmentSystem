package graphics;


import ecs.components.CameraComponent;
import math.Mat4;

/**
 * Lightweight data struct used by renderer to send camera matrices.
 */
public final class CameraData {
    public final Mat4 view;
    public final Mat4 projection;

    public CameraData(Mat4 view, Mat4 projection) {
        this.view = view;
        this.projection = projection;
    }

    public static CameraData from(CameraComponent c) {
        return new CameraData(c.viewMatrix(), c.projectionMatrix());
    }

    public float[] viewColumnMajor() {
        return view.toColumnMajorArray();
    }

    public float[] projectionColumnMajor() {
        return projection.toColumnMajorArray();
    }
}
