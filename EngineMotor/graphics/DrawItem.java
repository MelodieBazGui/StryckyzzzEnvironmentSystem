package graphics;

import math.Mat4;

/** Immutable draw command consumed by the renderer each frame. */
public final class DrawItem {
    public final Model model;       // must be GPU uploaded
    public final Mat4  modelMatrix; // row-major storage
    public final RenderMode mode;
    public final float lineWidth;   // for LINES
    public final float pointSize;   // for POINTS

    public DrawItem(Model model, Mat4 modelMatrix, RenderMode mode, float lineWidth, float pointSize) {
        this.model = model;
        this.modelMatrix = modelMatrix;
        this.mode = mode;
        this.lineWidth = lineWidth;
        this.pointSize = pointSize;
    }

    /** Column-major for glUniformMatrix4fv(..., transpose=false). */
    public float[] modelMatrixColumnMajor() {
        return modelMatrix.toColumnMajorArray();
    }
}
