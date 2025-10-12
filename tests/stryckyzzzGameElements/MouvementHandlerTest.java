package gameHandlers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eventManager.events.*;
import eventManager.managing.StryckEventManager;
import math.Vec3;
import parametres.MouvementParametres;

public class MouvementHandlerTest {
    private StryckEventManager eventManager;
    private MouvementParametres mouvementParams;
    private MouvementHandler handler;

    @BeforeEach
    public void setup() {
        eventManager = new StryckEventManager();
        mouvementParams = new MouvementParametres();
        handler = new MouvementHandler(eventManager, mouvementParams);
    }

    @Test
    public void testForwardMovement() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        Vec3 vec = handler.getMovementVector();
        assertEquals(0, vec.x, 0.001);
        assertEquals(0, vec.y, 0.001);
        assertEquals(1, vec.z, 0.001);
    }

    @Test
    public void testSprintIncreasesSpeed() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeySprint(), 0));
        Vec3 vec = handler.getMovementVector();
        assertEquals(1.5, vec.z, 0.001);
    }

    @Test
    public void testCrouchDecreasesSpeed() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyCrouch(), 0));
        Vec3 vec = handler.getMovementVector();
        assertEquals(0.77, vec.z, 0.001);
    }

    @Test
    public void testJumpAddsVerticalComponent() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyJump(), 0));
        Vec3 vec = handler.getMovementVector();
        assertEquals(1.0, vec.y, 0.001);
    }

    @Test
    public void testLostFocusResetsInputs() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new WindowFocusEvent().new WindowLostFocusEvent());
        Vec3 vec = handler.getMovementVector();
        assertEquals(0, vec.x, 0.001);
        assertEquals(0, vec.y, 0.001);
        assertEquals(0, vec.z, 0.001);
    }

    @Test
    public void testRegainFocusRestoresInputDetection() {
        eventManager.dispatch(new WindowFocusEvent().new WindowLostFocusEvent());
        eventManager.dispatch(new WindowFocusEvent().new WindowGainFocusEvent());
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        Vec3 vec = handler.getMovementVector();
        assertEquals(1, vec.z, 0.001);
    }

    @Test
    public void testDiagonalMovementIsNormalized() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyRight(), 0));
        Vec3 vec = handler.getMovementVector();
        double length = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        assertEquals(1.0, length, 0.001, "Diagonal movement should be normalized");
    }
}
