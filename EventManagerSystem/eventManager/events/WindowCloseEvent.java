package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

public class WindowCloseEvent extends StryckEvent {
    private static final Logger logger = new Logger(WindowCloseEvent.class);

    public WindowCloseEvent() {
        logger.info("Created");
    }

    @Override
    public String getName() { return "WindowCloseEvent"; }
    @Override
    public StryckEventType getEventType() { return StryckEventType.WindowClose; }
    @Override
    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
}
