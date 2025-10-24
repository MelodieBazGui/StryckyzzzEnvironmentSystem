package graphics;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lightweight JOGL renderer.
 * 
 * Does NOT rely on FPSAnimator or external GLUtils.
 * Provides minimal utilities for shaders, program linking, and VAO drawing.
 */
public final class Renderer implements GLEventListener {
    private final AtomicReference<FrameData> frameRef;

    private GLCanvas canvas;
    private GLProfile profile;
    private GLCapabilities caps;

    // OpenGL program
    private int program;
    private int uProj, uView, uModel, uColor;

    public Renderer(AtomicReference<FrameData> frameRef) {
        this.frameRef = frameRef;
    }

    // -------------------------------------------------------------------------
    // Create Canvas (you can manually call display() yourself)
    // -------------------------------------------------------------------------
    public GLCanvas createCanvas(int width, int height) {
        profile = GLProfile.get(GLProfile.GL4);
        caps = new GLCapabilities(profile);
        caps.setHardwareAccelerated(true);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);
        caps.setSampleBuffers(true);
        caps.setNumSamples(4); // MSAA

        canvas = new GLCanvas(caps);
        canvas.setSize(width, height);
        canvas.addGLEventListener(this);
        return canvas;
    }

    // -------------------------------------------------------------------------
    // Core OpenGL Lifecycle
    // -------------------------------------------------------------------------
    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_MULTISAMPLE);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glClearColor(0f, 0f, 0f, 1f);

        // --- Compile and link shaders ---
        int vs = compileShader(gl, GL4.GL_VERTEX_SHADER, ShaderSources.VS);
        int fs = compileShader(gl, GL4.GL_FRAGMENT_SHADER, ShaderSources.FS);
        program = linkProgram(gl, vs, fs);

        uProj = gl.glGetUniformLocation(program, "uProj");
        uView = gl.glGetUniformLocation(program, "uView");
        uModel = gl.glGetUniformLocation(program, "uModel");
        uColor = gl.glGetUniformLocation(program, "uColor");
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        FrameData fd = frameRef.get();
        if (fd == null) return;

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glUseProgram(program);

        // Upload camera matrices
        gl.glUniformMatrix4fv(uProj, 1, false, fd.camera.projection, 0);
        gl.glUniformMatrix4fv(uView, 1, false, fd.camera.view, 0);

        // Draw all objects
        for (DrawItem item : fd.drawList) {
            if (item == null || item.model == null) continue;

            int mode = toGLMode(item.mode);
            gl.glUniformMatrix4fv(uModel, 1, false, item.transform.model, 0);
            gl.glUniform4f(uColor, 1f, 1f, 1f, 1f);

            gl.glBindVertexArray(item.model.vao);
            if (item.model.indexed)
                gl.glDrawElements(mode, item.model.indexCount, GL.GL_UNSIGNED_INT, 0);
            else
                gl.glDrawArrays(mode, 0, item.model.vertexCount);
        }

        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        drawable.getGL().glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        if (program > 0) {
            gl.glDeleteProgram(program);
            program = 0;
        }
    }

    // -------------------------------------------------------------------------
    // Internal OpenGL Utilities
    // -------------------------------------------------------------------------
    private static int compileShader(GL4 gl, int type, String source) {
        int shader = gl.glCreateShader(type);
        String[] srcArray = new String[]{source};
        int[] length = new int[]{source.length()};
        gl.glShaderSource(shader, 1, srcArray, length, 0);
        gl.glCompileShader(shader);

        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL4.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            System.err.println("Shader compile failed:");
            printShaderLog(gl, shader);
            gl.glDeleteShader(shader);
            throw new RuntimeException("Shader compilation failed.");
        }
        return shader;
    }

    private static int linkProgram(GL4 gl, int vs, int fs) {
        int program = gl.glCreateProgram();
        gl.glAttachShader(program, vs);
        gl.glAttachShader(program, fs);
        gl.glLinkProgram(program);

        int[] linked = new int[1];
        gl.glGetProgramiv(program, GL4.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            System.err.println("Program link failed:");
            printProgramLog(gl, program);
            gl.glDeleteProgram(program);
            throw new RuntimeException("Shader linking failed.");
        }

        gl.glDeleteShader(vs);
        gl.glDeleteShader(fs);
        return program;
    }

    private static void printShaderLog(GL4 gl, int shader) {
        int[] len = new int[1];
        gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, len, 0);
        if (len[0] > 1) {
            byte[] log = new byte[len[0]];
            gl.glGetShaderInfoLog(shader, len[0], null, 0, log, 0);
            System.err.println(new String(log));
        }
    }

    private static void printProgramLog(GL4 gl, int program) {
        int[] len = new int[1];
        gl.glGetProgramiv(program, GL4.GL_INFO_LOG_LENGTH, len, 0);
        if (len[0] > 1) {
            byte[] log = new byte[len[0]];
            gl.glGetProgramInfoLog(program, len[0], null, 0, log, 0);
            System.err.println(new String(log));
        }
    }

    private static int toGLMode(RenderMode m) {
        return switch (m) {
            case TRIANGLES -> GL4.GL_TRIANGLES;
            case LINES     -> GL4.GL_LINES;
            case POINTS    -> GL4.GL_POINTS;
        };
    }
}
