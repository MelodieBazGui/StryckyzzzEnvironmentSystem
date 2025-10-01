package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

public class WindowResizeEvent extends StryckEvent {
    private final int width, height;
    private static final Logger logger = new Logger(WindowResizeEvent.class);

    public WindowResizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
        logger.info("Created with size: " + width + "x" + height);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public String getName() { return "WindowResizeEvent"; }
    @Override
    public StryckEventType getEventType() { return StryckEventType.WindowResize; }
    @Override
    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
}
