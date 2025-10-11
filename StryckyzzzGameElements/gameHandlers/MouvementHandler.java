package gameHandlers;

import eventManager.events.*;
import eventManager.enumerations.StryckEventType;
import eventManager.managing.StryckEventManager;
import math.Vec3;
import parametres.MouvementParametres;
import utils.Logger;

/**
 * Handles directional input and speed modifiers based on keyboard + focus events.
 */
public class MouvementHandler {
    private static final Logger logger = new Logger(MouvementHandler.class);

    private final MouvementParametres params;
    private final StryckEventManager eventManager;

    private boolean focused = true;

    // Input states
    private boolean forward, backward, left, right;
    private boolean sprint, crouch, jump;

    // Movement speeds
    private static final float BASE_SPEED = 1.0f;
    private static final float SPRINT_MULT = 1.5f;
    private static final float CROUCH_MULT = 0.77f;

    public MouvementHandler(StryckEventManager eventManager, MouvementParametres params) {
        this.eventManager = eventManager;
        this.params = params;
        registerListeners();
        logger.info("MouvementHandler initialized and listening for events");
    }

    private void registerListeners() {
        // Focus events
        eventManager.subscribe(StryckEventType.WindowFocus, e -> {
            if (e instanceof WindowFocusEvent.WindowGainFocusEvent) {
                focused = true;
                logger.info("Window gained focus");
                return true;
            }
            return false;
        });

        eventManager.subscribe(StryckEventType.WindowLostFocus, e -> {
            if (e instanceof WindowFocusEvent.WindowLostFocusEvent) {
                focused = false;
                resetInputs();
                logger.info("Window lost focus");
                return true;
            }
            return false;
        });

        // Keyboard pressed
        eventManager.subscribe(StryckEventType.KeyboardPressed, e -> {
            if (!focused || !(e instanceof KeyboardPressedEvent k)) return false;
            int key = k.getKeyCode();
            if (key == params.getKeyForward()) forward = true;
            else if (key == params.getKeyBackward()) backward = true;
            else if (key == params.getKeyLeft()) left = true;
            else if (key == params.getKeyRight()) right = true;
            else if (key == params.getKeySprint()) sprint = true;
            else if (key == params.getKeyCrouch()) crouch = true;
            else if (key == params.getKeyJump()) jump = true;
            return true;
        });

        // Keyboard released
        eventManager.subscribe(StryckEventType.KeyboardReleased, e -> {
            if (!focused || !(e instanceof KeyboardReleasedEvent k)) return false;
            int key = k.getKeyCode();
            if (key == params.getKeyForward()) forward = false;
            else if (key == params.getKeyBackward()) backward = false;
            else if (key == params.getKeyLeft()) left = false;
            else if (key == params.getKeyRight()) right = false;
            else if (key == params.getKeySprint()) sprint = false;
            else if (key == params.getKeyCrouch()) crouch = false;
            else if (key == params.getKeyJump()) jump = false;
            return true;
        });
    }

    private void resetInputs() {
        forward = backward = left = right = sprint = crouch = jump = false;
    }

    public Vec3 getMovementVector() {
        if (!focused) return new Vec3(0, 0, 0);

        float x = 0, z = 0;
        if (forward) z += 1;
        if (backward) z -= 1;
        if (left) x -= 1;
        if (right) x += 1;

        float magnitude = (float) Math.sqrt(x * x + z * z);
        if (magnitude > 0) {
            x /= magnitude;
            z /= magnitude;
        }

        float speed = BASE_SPEED;
        if (sprint) speed *= SPRINT_MULT;
        else if (crouch) speed *= CROUCH_MULT;

        return new Vec3(x * speed, jump ? 1.0f : 0.0f, z * speed);
    }
}
