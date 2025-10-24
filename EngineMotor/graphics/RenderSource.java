package graphics;

/**
 * CPU-side mesh data before GPU upload.
 * positions: xyz packed (len = 3 * vertexCount)
 * normals:   xyz packed (optional)
 * uvs:       uv  packed (optional)
 * indices:   int indices (optional)
 */
public final class RenderSource {
    public final float[] positions;
    public final float[] normals;
    public final float[] uvs;
    public final int[] indices;

    public RenderSource(float[] positions, float[] normals, float[] uvs, int[] indices) {
        this.positions = positions;
        this.normals   = normals;
        this.uvs       = uvs;
        this.indices   = indices;
    }

    public boolean isIndexed() { return indices != null && indices.length > 0; }
    public int vertexCount()   { return positions != null ? positions.length / 3 : 0; }
}
