package stryckyzzzGameElements;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eventManager.events.KeyboardPressedEvent;
import eventManager.events.KeyboardReleasedEvent;
import eventManager.events.WindowFocusEvent;
import eventManager.managing.StryckEventManager;
import gameHandlers.MouvementHandler;
import math.Vec3;
import parametres.MouvementParametres;

/**
 * Unit tests for MouvementHandler using real event dispatching.
 */
public class MouvementHandlerTest {

    private StryckEventManager eventManager;
    private MouvementParametres mouvementParams;
    private MouvementHandler mouvementHandler;

    @BeforeEach
    void setup() {
        eventManager = new StryckEventManager();
        mouvementParams = new MouvementParametres();
        mouvementHandler = new MouvementHandler(eventManager, mouvementParams);
    }
    
    @AfterEach
    void tearDown() {
    	eventManager = null;
    	mouvementParams = null;
    	mouvementHandler = null;
    }

    @Test
    void testForwardMovement() {
        // Simulate pressing the forward key
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));

        Vec3 movement = mouvementHandler.getMovementVector();

        assertEquals(0f, movement.getX(), 1e-6f);
        assertEquals(0f, movement.getY(), 1e-6f);
        assertTrue(movement.getZ() > 0f, "Should move forward");

        // Release key
        eventManager.dispatch(new KeyboardReleasedEvent(mouvementParams.getKeyForward()));
        assertEquals(new Vec3(0, 0, 0), mouvementHandler.getMovementVector());
    }

    @Test
    void testSprintModifier() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeySprint(), 0));

        Vec3 sprintMovement = mouvementHandler.getMovementVector();
        float expectedSpeed = 1.5f; // sprint multiplier
        assertEquals(expectedSpeed, sprintMovement.getZ(), 1e-6f, "Sprint should increase speed");

        // Release sprint
        eventManager.dispatch(new KeyboardReleasedEvent(mouvementParams.getKeySprint()));
        Vec3 normalMovement = mouvementHandler.getMovementVector();
        assertEquals(1.0f, normalMovement.getZ(), 1e-6f, "Back to normal speed");
    }

    @Test
    void testCrouchModifier() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyCrouch(), 0));

        Vec3 crouchMovement = mouvementHandler.getMovementVector();
        float expectedSpeed = 0.77f;
        assertEquals(expectedSpeed, crouchMovement.getZ(), 1e-6f, "Crouch should reduce speed");
    }

    @Test
    void testJumpFlag() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyJump(), 0));

        Vec3 jumpMovement = mouvementHandler.getMovementVector();
        assertEquals(1.0f, jumpMovement.getY(), 1e-6f, "Should apply jump");

        eventManager.dispatch(new KeyboardReleasedEvent(mouvementParams.getKeyJump()));
        Vec3 noJumpMovement = mouvementHandler.getMovementVector();
        assertEquals(0.0f, noJumpMovement.getY(), 1e-6f, "Jump should stop after release");
    }

    @Test
    void testFocusLossStopsMovement() {
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        eventManager.dispatch(new WindowFocusEvent.WindowLostFocusEvent());

        Vec3 movement = mouvementHandler.getMovementVector();
        assertEquals(new Vec3(0, 0, 0), movement, "Should not move when window is unfocused");

        // Regain focus
        eventManager.dispatch(new WindowFocusEvent.WindowGainFocusEvent());
        eventManager.dispatch(new KeyboardPressedEvent(mouvementParams.getKeyForward(), 0));
        Vec3 afterFocus = mouvementHandler.getMovementVector();
        assertTrue(afterFocus.getZ() > 0f, "Movement should resume after focus regained");
    }
}
