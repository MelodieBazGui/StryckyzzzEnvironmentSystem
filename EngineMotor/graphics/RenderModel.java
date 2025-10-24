package graphics;

import com.jogamp.opengl.GL4;

/**
 * Wraps a CPU-side RenderSource and lazily creates/owns the GPU Model.
 * Ensures upload on the GL thread.
 */
public final class RenderModel {
    private final RenderSource source;
    private Model gpu;

    public RenderModel(RenderSource source) {
        this.source = source;
    }

    public Model getOrUpload(GL4 gl) {
        if (gpu == null) gpu = Model.upload(gl, source);
        return gpu;
    }

    public boolean isUploaded() { return gpu != null; }
}
