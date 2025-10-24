package graphics;

import math.Vec3;

/**
 * Generates simple procedural meshes as RenderSource objects.
 * All data are in right-handed coordinate space (Y up, Z forward).
 */
public final class MeshFactory {

    private MeshFactory() {}

    /** Creates a unit cube centered at origin. */
    public static RenderSource cube() {
        float[] positions = {
            // front face
            -0.5f,-0.5f, 0.5f,   0.5f,-0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   -0.5f, 0.5f, 0.5f,
            // back face
            -0.5f,-0.5f,-0.5f,  -0.5f, 0.5f,-0.5f,   0.5f, 0.5f,-0.5f,    0.5f,-0.5f,-0.5f,
            // left
            -0.5f,-0.5f,-0.5f,  -0.5f,-0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,   -0.5f, 0.5f,-0.5f,
            // right
             0.5f,-0.5f,-0.5f,   0.5f, 0.5f,-0.5f,   0.5f, 0.5f, 0.5f,    0.5f,-0.5f, 0.5f,
            // top
            -0.5f, 0.5f,-0.5f,  -0.5f, 0.5f, 0.5f,   0.5f, 0.5f, 0.5f,    0.5f, 0.5f,-0.5f,
            // bottom
            -0.5f,-0.5f,-0.5f,   0.5f,-0.5f,-0.5f,   0.5f,-0.5f, 0.5f,   -0.5f,-0.5f, 0.5f
        };

        float[] normals = {
            // front
            0,0,1, 0,0,1, 0,0,1, 0,0,1,
            // back
            0,0,-1,0,0,-1,0,0,-1,0,0,-1,
            // left
            -1,0,0,-1,0,0,-1,0,0,-1,0,0,
            // right
             1,0,0, 1,0,0, 1,0,0, 1,0,0,
            // top
             0,1,0, 0,1,0, 0,1,0, 0,1,0,
            // bottom
             0,-1,0,0,-1,0,0,-1,0,0,-1,0
        };

        float[] uvs = {
            0,0, 1,0, 1,1, 0,1,
            0,0, 1,0, 1,1, 0,1,
            0,0, 1,0, 1,1, 0,1,
            0,0, 1,0, 1,1, 0,1,
            0,0, 1,0, 1,1, 0,1,
            0,0, 1,0, 1,1, 0,1
        };

        int[] indices = {
            0,1,2, 0,2,3,        // front
            4,5,6, 4,6,7,        // back
            8,9,10, 8,10,11,     // left
            12,13,14, 12,14,15,  // right
            16,17,18, 16,18,19,  // top
            20,21,22, 20,22,23   // bottom
        };

        return new RenderSource(positions, normals, uvs, indices);
    }

    /** Simple XY quad (1x1) centered at origin, facing +Z. */
    public static RenderSource quad() {
        float[] positions = {
            -0.5f, -0.5f, 0f,
             0.5f, -0.5f, 0f,
             0.5f,  0.5f, 0f,
            -0.5f,  0.5f, 0f
        };
        float[] normals = {0,0,1, 0,0,1, 0,0,1, 0,0,1};
        float[] uvs = {0,0, 1,0, 1,1, 0,1};
        int[] indices = {0,1,2, 0,2,3};
        return new RenderSource(positions, normals, uvs, indices);
    }

    /** Returns a simple line segment from A to B. */
    public static RenderSource line(Vec3 a, Vec3 b) {
        float[] pos = {
            a.getX(), a.getY(), a.getZ(),
            b.getX(), b.getY(), b.getZ()
        };
        return new RenderSource(pos, null, null, null);
    }

    /** Generates a grid of lines in XZ plane centered at origin. */
    public static RenderSource grid(int lines, float spacing) {
        int count = lines * 4;
        float half = (lines - 1) * spacing * 0.5f;
        float[] pos = new float[count * 3];
        int i = 0;
        for (int n = 0; n < lines; n++) {
            float z = -half + n * spacing;
            pos[i++] = -half; pos[i++] = 0; pos[i++] = z;
            pos[i++] =  half; pos[i++] = 0; pos[i++] = z;
        }
        for (int n = 0; n < lines; n++) {
            float x = -half + n * spacing;
            pos[i++] = x; pos[i++] = 0; pos[i++] = -half;
            pos[i++] = x; pos[i++] = 0; pos[i++] =  half;
        }
        return new RenderSource(pos, null, null, null);
    }
}
