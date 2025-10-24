package graphics;

import com.jogamp.opengl.*;
import ecs.components.*;

import java.util.List;

/**
 * Minimal forward renderer that handles 3D DrawItems with camera matrices.
 * Requires shader with uniforms:
 *  - mat4 uModel
 *  - mat4 uView
 *  - mat4 uProjection
 */
public final class ForwardRenderer {

    private int program;

    public ForwardRenderer(GL4 gl, int program) {
        this.program = program;
        gl.glUseProgram(program);
    }

    public void render(GL4 gl, CameraComponent camera, List<DrawItem> drawList) {
        gl.glUseProgram(program);

        // === Set global uniforms (camera matrices) ===
        int uViewLoc = gl.glGetUniformLocation(program, "uView");
        int uProjLoc = gl.glGetUniformLocation(program, "uProjection");

        float[] view = camera.viewMatrix().toColumnMajorArray();
        float[] proj = camera.projectionMatrix().toColumnMajorArray();

        gl.glUniformMatrix4fv(uViewLoc, 1, false, view, 0);
        gl.glUniformMatrix4fv(uProjLoc, 1, false, proj, 0);

        // === Render each item ===
        for (DrawItem item : drawList) {
            if (item.model == null) continue;

            int uModelLoc = gl.glGetUniformLocation(program, "uModel");
            gl.glUniformMatrix4fv(uModelLoc, 1, false, item.modelMatrixColumnMajor(), 0);

            // Antialiasing options
            if (item.mode == RenderMode.LINES) {
                gl.glEnable(GL.GL_LINE_SMOOTH);
                gl.glLineWidth(item.lineWidth);
            } else if (item.mode == RenderMode.POINTS) {
            	gl.glPointSize(item.pointSize);
            } else {
            	gl.glLineWidth(item.lineWidth);
            }

            gl.glBindVertexArray(item.model.vao);

            if (item.model.indexed) {
                gl.glDrawElements(toGLMode(item.mode), item.model.indexCount, GL.GL_UNSIGNED_INT, 0);
            } else {
                gl.glDrawArrays(toGLMode(item.mode), 0, item.model.vertexCount);
            }

            gl.glBindVertexArray(0);
        }

        gl.glUseProgram(0);
    }

    private int toGLMode(RenderMode mode) {
        switch (mode) {
            case LINES: return GL.GL_LINES;
            case POINTS: return GL.GL_POINTS;
            default: return GL.GL_TRIANGLES;
        }
    }
}
