package eventManager.events;
import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

/**
 * Base event. Instance logger uses the concrete event class
 * and can be used to include the object in the logs.
 */
public abstract class StryckEvent {
    private boolean handled = false;
    // instance logger so log lines include correct class and we can log "this"
    protected final Logger logger = new Logger(this.getClass());

    public boolean isHandled() { return handled; }

    public void setHandled(boolean handled) {
        this.handled = handled;
        logger.info("setHandled=" + handled, this);
    }

    public abstract String getName();
    public abstract StryckEventType getEventType();
    public abstract int getCategoryFlags();

    public boolean isInCategory(StryckEventCategory category) {
        return (getCategoryFlags() & category.getBit()) != 0;
    }

    @Override
    public String toString() {
        return getName();
    }
}
