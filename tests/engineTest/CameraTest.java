package engineTest;

import math.Vec3;
import org.junit.jupiter.api.Test;

import graphics.Camera;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    @Test
    void testDefaultOrientation() {
        Camera cam = new Camera();
        assertEquals(new Vec3(0, 0, -1), cam.getForward(), "Default forward should be -Z");
        assertEquals(new Vec3(1, 0, 0), cam.getRight(), "Default right should be +X");
        assertEquals(new Vec3(0, 1, 0), cam.getUp(), "Default up should be +Y");
    }

    @Test
    void testSetPositionAndMove() {
        Camera cam = new Camera();
        cam.setPosition(new Vec3(1, 2, 3));
        assertEquals(new Vec3(1, 2, 3), cam.getPosition());
        cam.move(new Vec3(-1, 1, 0));
        assertEquals(new Vec3(0, 3, 3), cam.getPosition());
    }

    @Test
    void testRotationAffectsForwardVector() {
        Camera cam = new Camera();
        cam.setRotation(new Vec3(0, 90, 0));
        Vec3 forward = cam.getForward();
        assertTrue(forward.getX() > 0.9f, "Yaw 90° should make forward point along +X");
        assertTrue(Math.abs(forward.getZ()) < 0.1f, "Z should be near 0 at yaw 90°");
    }

    @Test
    void testPitchRotation() {
        Camera cam = new Camera();
        cam.setRotation(new Vec3(45, 0, 0));
        Vec3 forward = cam.getForward();
        assertTrue(forward.getY() > 0.5f, "Pitch up should raise forward vector’s Y");
    }

    @Test
    void testResetCamera() {
        Camera cam = new Camera();
        cam.setPosition(new Vec3(5, 5, 5));
        cam.setRotation(new Vec3(30, 45, 0));
        cam.reset();

        assertEquals(new Vec3(0, 0, 0), cam.getPosition());
        assertEquals(new Vec3(0, 0, -1), cam.getForward());
    }
}
