package graphics;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** GPU-side VAO/VBO/IBO created on the GL thread. */
public final class Model {
    public final int vao;
    public final int vbo;
    public final int ibo;        // 0 if non-indexed
    public final int vertexCount;
    public final int indexCount;
    public final boolean indexed;

    private Model(int vao, int vbo, int ibo, int vertexCount, int indexCount, boolean indexed) {
        this.vao = vao; this.vbo = vbo; this.ibo = ibo;
        this.vertexCount = vertexCount; this.indexCount = indexCount; this.indexed = indexed;
    }

    /** Uploads interleaved (pos, normal, uv) layout: 3/3/2 floats. */
    public static Model upload(GL4 gl, RenderSource src) {
        int[] ids = new int[1];

        gl.glGenVertexArrays(1, ids, 0);
        int vao = ids[0];
        gl.glBindVertexArray(vao);

        gl.glGenBuffers(1, ids, 0);
        int vbo = ids[0];
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo);

        int vCount = src.vertexCount();
        FloatBuffer inter = Buffers.newDirectFloatBuffer(vCount * 8);
        for (int i = 0; i < vCount; i++) {
            // pos
            inter.put(src.positions, i*3, 3);
            // normal (default 0,0,1)
            if (src.normals != null) inter.put(src.normals, i*3, 3);
            else { inter.put(0f).put(0f).put(1f); }
            // uv (default 0,0)
            if (src.uvs != null) inter.put(src.uvs, i*2, 2);
            else { inter.put(0f).put(0f); }
        }
        inter.flip();
        gl.glBufferData(GL.GL_ARRAY_BUFFER, inter.capacity() * Float.BYTES, inter, GL.GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        gl.glEnableVertexAttribArray(0); // pos
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, stride, 0L);
        gl.glEnableVertexAttribArray(1); // normal
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, stride, 3L * Float.BYTES);
        gl.glEnableVertexAttribArray(2); // uv
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, stride, 6L * Float.BYTES);

        int ibo = 0, indexCount = 0;
        boolean indexed = src.isIndexed();
        if (indexed) {
            gl.glGenBuffers(1, ids, 0);
            ibo = ids[0];
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ibo);
            IntBuffer idx = Buffers.newDirectIntBuffer(src.indices);
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, idx.capacity() * Integer.BYTES, idx, GL.GL_STATIC_DRAW);
            indexCount = src.indices.length;
        }

        gl.glBindVertexArray(0);
        return new Model(vao, vbo, ibo, vCount, indexCount, indexed);
    }
}
