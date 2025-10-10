package eventManager.managing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import eventManager.enumerations.StryckEventType;
import eventManager.events.StryckEvent;
import utils.Logger;

public class StryckEventManager {
    private final Map<StryckEventType, List<Function<StryckEvent, Boolean>>> listeners = new ConcurrentHashMap<>();
    private static final Logger logger = new Logger(StryckEventManager.class);

    public <T extends StryckEvent> void subscribe(StryckEventType type, Function<T, Boolean> listener) {
        listeners.computeIfAbsent(type, k -> Collections.synchronizedList(new ArrayList<>()))
                 .add((Function<StryckEvent, Boolean>) listener);
        logger.info("Listener subscribed for " + type);
    }

    public void dispatch(StryckEvent event) {
        List<Function<StryckEvent, Boolean>> funcs = listeners.get(event.getEventType());
        if (funcs == null || funcs.isEmpty()) {
            logger.info("No listeners for event: " + event.getName());
            return;
        }

        for (Function<StryckEvent, Boolean> func : funcs) {
            if (event.isHandled()) break;
            if (Boolean.TRUE.equals(func.apply(event))) event.setHandled(true);
        }

        logger.info("Event dispatched: " + event.getName() + ", handled=" + event.isHandled());
    }
}
