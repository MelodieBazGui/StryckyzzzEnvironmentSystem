package handlers;

import java.util.HashSet;
import java.util.Set;
import eventManager.enumerations.StryckEventType;
import eventManager.managing.StryckEventManager;
import utils.Logger;
import eventManager.events.KeyboardPressedEvent;
import eventManager.events.KeyboardReleasedEvent;

public class KeyboardEventHandler {
    private static final Logger logger = new Logger(KeyboardEventHandler.class);
    private final StryckEventManager eventManager;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public KeyboardEventHandler(StryckEventManager eventManager) {
        this.eventManager = eventManager;
        registerListeners();
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    private void registerListeners() {
        eventManager.subscribe(StryckEventType.KeyboardPressed, (KeyboardPressedEvent e) -> {
            pressedKeys.add(e.getKeyCode());
            logger.info("Key pressed: " + e.getKeyCode());
            return true;
        });

        eventManager.subscribe(StryckEventType.KeyboardReleased, (KeyboardReleasedEvent e) -> {
            pressedKeys.remove(e.getKeyCode());
            logger.info("Key released: " + e.getKeyCode());
            return true;
        });
    }
}
