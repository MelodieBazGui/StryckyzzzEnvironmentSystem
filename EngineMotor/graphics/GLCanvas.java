package graphics;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Custom GLCanvas implementation inspired by JOGL.
 *
 * Provides:
 *  - Full GLEventListener lifecycle
 *  - Real OpenGL context (via LWJGL)
 *  - Resize and input events
 *  - Continuous rendering thread (like FPSAnimator)
 *
 * Drop-in API compatible with JOGL-style usage:
 *    canvas.addGLEventListener(listener);
 *    canvas.start();
 *    canvas.stop();
 */
public class GLCanvas {

    // --- Lifecycle & rendering ---
    private long window;
    private Thread renderThread;
    private boolean running = false;
    private boolean initialized = false;
    private int width = 800, height = 600;
    private String title = "GLCanvas";

    // --- Listener list ---
    private final List<GLEventListener> listeners = new ArrayList<>();

    // --- Config options ---
    private int targetFPS = 60;
    private boolean vsync = true;

    public GLCanvas() {}
    public GLCanvas(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    // -------------------------------------------------------------------------
    // Initialization and Lifecycle
    // -------------------------------------------------------------------------
    public void start() {
        if (running) return;
        running = true;

        renderThread = new Thread(() -> {
            try {
                initGL();
                initIfNeeded();

                long lastTime = System.nanoTime();
                double frameTime = 1.0 / targetFPS;

                while (running && !glfwWindowShouldClose(window)) {
                    long now = System.nanoTime();
                    double dt = (now - lastTime) / 1e9;
                    if (dt < frameTime) {
                        Thread.sleep((long) ((frameTime - dt) * 1000));
                        continue;
                    }
                    lastTime = now;

                    // --- Notify listeners ---
                    for (GLEventListener l : listeners) {
                        l.display(new LwjglDrawable());
                    }

                    glfwSwapBuffers(window);
                    glfwPollEvents();
                }

            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                fireDispose();
                glfwTerminate();
                GLFWErrorCallback.createPrint(System.err).set();
            }
        }, "GLCanvas-Thread");

        renderThread.start();
    }

    public void stop() {
        running = false;
        if (window != NULL) {
            glfwSetWindowShouldClose(window, true);
        }
        if (renderThread != null) {
            try { renderThread.join(); } catch (InterruptedException ignored) {}
            renderThread = null;
        }
    }

    // -------------------------------------------------------------------------
    // GL Setup
    // -------------------------------------------------------------------------
    private void initGL() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create GLFW window");

        // Center window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vid.width() - pWidth.get(0)) / 2,
                    (vid.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(vsync ? 1 : 0);
        glfwShowWindow(window);

        // Resize callback
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, width, height);
            for (GLEventListener l : listeners) {
                l.reshape(new LwjglDrawable(), 0, 0, width, height);
            }
        });

        // Enable MSAA
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
    }

    private void initIfNeeded() {
        if (!initialized) {
            for (GLEventListener l : listeners) {
                l.init(new LwjglDrawable());
            }
            initialized = true;
        }
    }

    private void fireDispose() {
        for (GLEventListener l : listeners) {
            l.dispose(new LwjglDrawable());
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------
    public void addGLEventListener(GLEventListener listener) {
        listeners.add(listener);
    }

    public void removeGLEventListener(GLEventListener listener) {
        listeners.remove(listener);
    }

    public void setTargetFPS(int fps) {
        this.targetFPS = Math.max(1, fps);
    }

    public void setVsync(boolean enabled) {
        this.vsync = enabled;
    }

    public boolean isRunning() {
        return running;
    }

    public long getWindowHandle() {
        return window;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // -------------------------------------------------------------------------
    // LWJGL-backed GLAutoDrawable
    // -------------------------------------------------------------------------
    private static class LwjglDrawable implements GLAutoDrawable {
        @Override public GL getGL() { return new LwjglGL(); }
    }

    // -------------------------------------------------------------------------
    // Real GL wrapper (for compatibility with JOGL API)
    // -------------------------------------------------------------------------
    private static class LwjglGL implements GL {
        @Override public void clearColor(float r, float g, float b, float a) { glClearColor(r,g,b,a); }
        @Override public void clear(int mask) { glClear(mask); }
        @Override public String getVersion() { return glGetString(GL_VERSION); }
        @Override public void enable(int cap) { glEnable(cap); }
        @Override public void disable(int cap) { glDisable(cap); }
        @Override public void drawArrays(int mode, int first, int count) { glDrawArrays(mode, first, count); }
        @Override public void drawElements(int mode, int count, int type, long indices) { glDrawElements(mode, count, type, indices); }
        @Override public void viewport(int x, int y, int w, int h) { glViewport(x,y,w,h); }
    }

    // -------------------------------------------------------------------------
    // JOGL-like Interfaces
    // -------------------------------------------------------------------------
    public interface GLEventListener {
        void init(GLAutoDrawable drawable);
        void display(GLAutoDrawable drawable);
        void reshape(GLAutoDrawable drawable, int x, int y, int width, int height);
        void dispose(GLAutoDrawable drawable);
    }

    public interface GLAutoDrawable {
        GL getGL();
    }

    public interface GL {
        void clearColor(float r, float g, float b, float a);
        void clear(int mask);
        String getVersion();

        // Additional core GL calls for compatibility
        void enable(int cap);
        void disable(int cap);
        void drawArrays(int mode, int first, int count);
        void drawElements(int mode, int count, int type, long indices);
        void viewport(int x, int y, int w, int h);
    }
}

