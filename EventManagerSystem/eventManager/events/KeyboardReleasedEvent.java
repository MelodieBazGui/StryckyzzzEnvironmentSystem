package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

public class KeyboardReleasedEvent extends StryckEvent {
    private final int keyCode;
    private static final Logger logger = new Logger(KeyboardReleasedEvent.class);

    public KeyboardReleasedEvent(int keyCode) {
        this.keyCode = keyCode;
        logger.info("Created with key=" + keyCode);
    }

    public int getKeyCode() { return keyCode; }

    @Override
    public String getName() { return "KeyboardReleasedEvent"; }
    @Override
    public StryckEventType getEventType() { return StryckEventType.KeyboardReleased; }
    @Override
    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.Keyboard.getBit(); }
}