package gameHandlers;

import eventManager.enumerations.StryckEventType;
import eventManager.events.MouseEvent.MouseMouvedEvent;
import eventManager.events.WindowFocusEvent;
import eventManager.managing.StryckEventManager;
import graphics.Camera;
import math.Vec3;
import utils.Logger;

/**
 * Handles camera rotation purely via mouse input.
 * Works only when window has focus and camera mode is enabled.
 * @author EmeJay, Stryckoeurzzz
 */
public class CameraMouvementHandler {
    private static final Logger logger = new Logger(CameraMouvementHandler.class);

    private final StryckEventManager eventManager;
    private final Camera camera;

    private boolean focused = true;
    private boolean cameraMode = false; // Whether mouse look is active

    private float yaw = 0f;
    private float pitch = 0f;
    private float sensitivity = 0.1f;

    public CameraMouvementHandler(StryckEventManager eventManager, Camera camera) {
        this.eventManager = eventManager;
        this.camera = camera;
        registerListeners();
        logger.info("CameraMouvementHandler initialized");
    }

    private void registerListeners() {
        // Focus events
        eventManager.subscribe(StryckEventType.WindowFocus, e -> {
            if (e instanceof WindowFocusEvent.WindowGainFocusEvent) {
                focused = true;
                logger.info("Window gained focus for camera control");
                return true;
            }
            return false;
        });

        eventManager.subscribe(StryckEventType.WindowLostFocus, e -> {
            if (e instanceof WindowFocusEvent.WindowLostFocusEvent) {
                focused = false;
                logger.info("Window lost focus — camera control disabled");
                return true;
            }
            return false;
        });

        // Mouse movement events
        eventManager.subscribe(StryckEventType.MouseMouved, e -> {
            if (!focused) {
                logger.info("Ignoring mouse input (window unfocused)");
                return false;
            }
            if (!cameraMode) {
                logger.info("Ignoring mouse input (camera mode off)");
                return false;
            }

            if (e instanceof MouseMouvedEvent mouse) {
                onMouseMoved(mouse);
                return true;
            }
            return false;
        });
    }

    private void onMouseMoved(MouseMouvedEvent e) {
        if (!focused || !cameraMode) {
            // Defensive check for async dispatch scenarios
            logger.info("Ignoring movement while unfocused or camera mode off");
            return;
        }

        float dx = e.getX();
        float dy = e.getY();

        yaw += dx * sensitivity;
        pitch -= dy * sensitivity;

        // Clamp vertical rotation to avoid flipping
        pitch = Math.max(-89f, Math.min(89f, pitch));

        camera.setRotation(new Vec3(pitch, yaw, 0));
        logger.info("Camera rotated to yaw=" + yaw + ", pitch=" + pitch);
    }

    // -----------------------------
    // Public Controls
    // -----------------------------

    /** Enable or disable camera control mode */
    public void setCameraMode(boolean enabled) {
        this.cameraMode = enabled;
        logger.info("Camera mode " + (enabled ? "enabled" : "disabled"));
    }

    /** @return true if the camera mode (mouse look) is active */
    public boolean isCameraMode() {
        return cameraMode;
    }

    /** @return true if the window is currently focused */
    public boolean isFocused() {
        return focused;
    }

    /** Adjust rotation sensitivity */
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    /**
     * Used only for testing to simulate focus events.
     */
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
