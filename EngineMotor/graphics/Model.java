package graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * GPU-side VAO/VBO/IBO created on the GL thread.
 * Uses LWJGL or raw OpenGL bindings (no JOGL dependency).
 */
public final class Model {
    public final int vao;
    public final int vbo;
    public final int ibo;        // 0 if non-indexed
    public final int vertexCount;
    public final int indexCount;
    public final boolean indexed;

    private Model(int vao, int vbo, int ibo, int vertexCount, int indexCount, boolean indexed) {
        this.vao = vao;
        this.vbo = vbo;
        this.ibo = ibo;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
        this.indexed = indexed;
    }

    /**
     * Uploads interleaved (pos, normal, uv) layout: 3/3/2 floats per vertex.
     */
    public static Model upload(RenderSource src) {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        int vCount = src.vertexCount();
        FloatBuffer inter = newDirectFloatBuffer(vCount * 8);

        for (int i = 0; i < vCount; i++) {
            // position
            inter.put(src.positions, i * 3, 3);

            // normal (default 0,0,1)
            if (src.normals != null) inter.put(src.normals, i * 3, 3);
            else inter.put(0f).put(0f).put(1f);

            // uv (default 0,0)
            if (src.uvs != null) inter.put(src.uvs, i * 2, 2);
            else inter.put(0f).put(0f);
        }

        inter.flip();
        glBufferData(GL_ARRAY_BUFFER, inter, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        glEnableVertexAttribArray(0); // pos
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(1); // normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(2); // uv
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);

        int ibo = 0, indexCount = 0;
        boolean indexed = src.isIndexed();

        if (indexed) {
            ibo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

            IntBuffer idx = newDirectIntBuffer(src.indices);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idx, GL_STATIC_DRAW);
            indexCount = src.indices.length;
        }

        glBindVertexArray(0);
        return new Model(vao, vbo, ibo, vCount, indexCount, indexed);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------
    private static FloatBuffer newDirectFloatBuffer(int size) {
        return ByteBuffer.allocateDirect(size * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    private static IntBuffer newDirectIntBuffer(int[] data) {
        IntBuffer buffer = ByteBuffer.allocateDirect(data.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private static FloatBuffer newDirectFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
}
