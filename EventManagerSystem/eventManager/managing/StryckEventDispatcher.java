package eventManager.managing;

import java.util.function.Function;
import eventManager.events.StryckEvent;
import utils.Logger;

public class StryckEventDispatcher {
    private final StryckEvent event;
    private static final Logger logger = new Logger(StryckEventDispatcher.class);

    public StryckEventDispatcher(StryckEvent event) {
        this.event = event;
        logger.info("Created dispatcher for event", event);
    }

    public <T extends StryckEvent> boolean dispatch(Class<T> type, Function<T, Boolean> handler) {
        if (type.isInstance(event)) {
            logger.info("Dispatching handler for " + type.getSimpleName(), event);
            if (handler.apply(type.cast(event))) event.setHandled(true);
            return true;
        }
        return false;
    }
}
