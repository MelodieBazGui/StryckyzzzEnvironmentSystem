package stryckyzzzGameElements;

import eventManager.events.MouseEvent;
import eventManager.events.WindowFocusEvent;
import eventManager.managing.StryckEventManager;
import gameHandlers.CameraMouvementHandler;
import graphics.Camera;
import math.Vec3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;

/**
 * Test class for CameraMouvementHandler
 */
public class CameraMouvementHandlerTest {
    private StryckEventManager manager;
    private Camera camera;
    private CameraMouvementHandler handler;
    private WindowFocusEvent windowFocusEvent;

    @BeforeEach
    void setup() {
        manager = new StryckEventManager();
        camera = new Camera();
        handler = new CameraMouvementHandler(manager, camera);
        windowFocusEvent = new WindowFocusEvent(manager);
    }
    
    @AfterEach
    void tearDown() {
    	manager = null;
    	camera = null;
    	handler = null;
    	windowFocusEvent = null;
    }

    @Test
    void testCameraModeInactiveDoesNotRotate() {
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(10, 5));
        assertEquals(0f, camera.getRotation().getY(), 0.001f, "Yaw should remain unchanged");
    }

    @Test
    void testCameraModeActiveRespondsToMouseMovement() {
        handler.setCameraMode(true);
        handler.setFocused(true);
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(15, -10));

        float yaw = camera.getRotation().getY();
        float pitch = camera.getRotation().getX();

        assertTrue(Math.abs(yaw) > 0.001f, "Yaw should change with X movement");
        assertTrue(Math.abs(pitch) > 0.001f, "Pitch should change with Y movement");
    }

    @Test
    void testFocusLossStopsRotation() {
        handler.setCameraMode(true);
        handler.setFocused(false);
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(20, 20));

        Vec3 rotationBefore = camera.getRotation().cpy();
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(50, 50));
        assertEquals(rotationBefore, camera.getRotation(), "Camera rotation should not change when unfocused");
    }

    @Test
    void testWindowFocusEventIntegration() {
        new WindowFocusEvent(manager); // Registers focus listeners
        handler.setCameraMode(true);

        manager.dispatch(new WindowFocusEvent.WindowLostFocusEvent());
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(30, -15));

        assertEquals(new Vec3(0, 0, 0), camera.getRotation(), "Rotation should remain unchanged while unfocused");

        manager.dispatch(new WindowFocusEvent.WindowGainFocusEvent());
        manager.dispatch(new MouseEvent().new MouseMouvedEvent(30, -15));
        assertNotEquals(new Vec3(0, 0, 0), camera.getRotation(), "Rotation should change after regaining focus");
    }

    @Test
    void testScrollChangesZoomOrDistanceIfImplemented() {
        handler.setCameraMode(true);
        handler.setFocused(true);

        manager.dispatch(new MouseEvent().new MouseScrollEvent(0, 2));
        // If CameraMouvementHandler supports zoom, verify that behavior.
        // Otherwise, ensure no crash occurs.
        assertDoesNotThrow(() ->
                manager.dispatch(new MouseEvent().new MouseScrollEvent(0, -1)),
                "Scroll should not cause exceptions");
    }
}
