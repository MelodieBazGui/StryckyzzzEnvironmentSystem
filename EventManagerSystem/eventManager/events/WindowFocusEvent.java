package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import eventManager.managing.StryckEventManager;
import utils.Logger;

public class WindowFocusEvent {
    private static final Logger logger = new Logger(WindowFocusEvent.class);
    private final StryckEventManager eventManager;
    private boolean focus;

    public WindowFocusEvent(StryckEventManager eventManager) {
        this.eventManager = eventManager;
        this.focus = false;
        registerListeners();
    }

    public boolean hasFocus() { return focus; }
    private void setFocus(boolean focus) { this.focus = focus; }

    private void registerListeners() {
        eventManager.subscribe(StryckEventType.WindowFocus, (WindowGainFocusEvent e) -> {
            logger.info("Handling WindowGainFocusEvent");
            setFocus(true);
            return true;
        });

        eventManager.subscribe(StryckEventType.WindowLostFocus, (WindowLostFocusEvent e) -> {
            logger.info("Handling WindowLostFocusEvent");
            setFocus(false);
            return true;
        });
    }

    public class WindowGainFocusEvent extends StryckEvent {
        public WindowGainFocusEvent() {
            logger.info("Created WindowGainFocusEvent");
        }
        @Override public String getName() { return "WindowGainFocusEvent"; }
        @Override public StryckEventType getEventType() { return StryckEventType.WindowFocus; }
        @Override public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
    }

    public class WindowLostFocusEvent extends StryckEvent {
        public WindowLostFocusEvent() {
            logger.info("Created WindowLostFocusEvent");
        }
        @Override public String getName() { return "WindowLostFocusEvent"; }
        @Override public StryckEventType getEventType() { return StryckEventType.WindowLostFocus; }
        @Override public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
    }
}
