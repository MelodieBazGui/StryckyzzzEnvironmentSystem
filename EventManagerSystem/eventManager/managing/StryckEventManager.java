package eventManager.managing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import eventManager.enumerations.StryckEventType;
import eventManager.events.StryckEvent;
import utils.Logger;

public class StryckEventManager {
    private final Map<StryckEventType, List<Function<? extends StryckEvent, Boolean>>> listeners = new ConcurrentHashMap<>();
    private static final Logger logger = new Logger(StryckEventManager.class);

    public <T extends StryckEvent> void subscribe(StryckEventType type, Function<T, Boolean> listener) {
        listeners.computeIfAbsent(type, k -> Collections.synchronizedList(new ArrayList<>()))
                 .add(listener);
        logger.info("Listener subscribed for " + type);
    }

    public void dispatch(StryckEvent event) {
        List<Function<? extends StryckEvent, Boolean>> funcs = listeners.get(event.getEventType());
        if (funcs != null) {
            for (Function<? extends StryckEvent, Boolean> func : funcs) {
                if (event.isHandled()) {
					break;
				}
                boolean handled = ((Function<StryckEvent, Boolean>) func).apply(event);
                if (handled) {
					event.setHandled(true);
				}
            }
        }
        logger.info("Event dispatched: " + event.getName() + ", handled=" + event.isHandled());
    }
}
