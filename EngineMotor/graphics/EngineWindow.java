package graphics;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

public final class EngineWindow {

    public static GLWindow createWindow(int width, int height, String title) {
        GLProfile profile = GLProfile.get(GLProfile.GL4); // or GL3 if you prefer
        GLCapabilities caps = new GLCapabilities(profile);

        // === Enable Multisample Anti-Aliasing ===
        caps.setSampleBuffers(true);   // request MSAA buffer
        caps.setNumSamples(4);         // typical values: 2, 4, 8, or 16
        caps.setHardwareAccelerated(true);

        // === Create window ===
        GLWindow window = GLWindow.create(caps);
        window.setSize(width, height);
        window.setTitle(title);
        window.setVisible(true);

        // Enable VSync (optional)
        window.getGL().getGL4().glEnable(GL.GL_MULTISAMPLE);

        return window;
    }
}
