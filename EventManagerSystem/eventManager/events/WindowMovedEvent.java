package eventManager.events;

import utils.Logger;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;

public class WindowMovedEvent extends StryckEvent {
    private final int x, y;
    private static final Logger logger = new Logger(WindowMovedEvent.class);

    public WindowMovedEvent(int x, int y) {
        this.x = x;
        this.y = y;
        logger.info("Created at pos: (" + x + "," + y + ")");
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String getName() { return "WindowMovedEvent"; }
    @Override
    public StryckEventType getEventType() { return StryckEventType.WindowMoved; }
    @Override
    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
}
