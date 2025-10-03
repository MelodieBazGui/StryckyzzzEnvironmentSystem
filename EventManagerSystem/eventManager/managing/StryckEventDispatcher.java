package eventManager.managing;

import java.util.function.Function;

import eventManager.events.StryckEvent;
import utils.Logger;

public class StryckEventDispatcher {
    private final StryckEvent event;
    private final Logger logger = new Logger(StryckEventDispatcher.class);

    public StryckEventDispatcher(StryckEvent event) {
        this.event = event;
        logger.info("Created dispatcher for event", event);
    }

    /**
     * Dispatch the event if it matches the requested class.
     * Example usage:
     *   dispatcher.dispatch(WindowCloseEvent.class, (WindowCloseEvent e) -> { ... });
     */
    public <T extends StryckEvent> boolean dispatch(Class<T> type, Function<T, Boolean> handler) {
        if (type.isInstance(event)) {
            logger.info("Dispatching to handler for " + type.getSimpleName(), event);
            boolean handled = handler.apply(type.cast(event));
            if (handled) {
				event.setHandled(true);
			}
            return true;
        }
        return false;
    }
}
