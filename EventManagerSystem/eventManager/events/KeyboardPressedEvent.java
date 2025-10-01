package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

import utils.Logger;

public class KeyboardPressedEvent extends StryckEvent {
    private final int keyCode;
    private final int repeatCount;
    private static final Logger logger = new Logger(KeyboardPressedEvent.class);

    public KeyboardPressedEvent(int keyCode, int repeatCount) {
        this.keyCode = keyCode;
        this.repeatCount = repeatCount;
        logger.info("Created with key=" + keyCode + " repeat=" + repeatCount);
    }

    public int getKeyCode() { return keyCode; }
    public int getRepeatCount() { return repeatCount; }

    @Override
    public String getName() { return "KeyboardPressedEvent"; }
    @Override
    public StryckEventType getEventType() { return StryckEventType.KeyboardPressed; }
    @Override
    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.Keyboard.getBit(); }
}