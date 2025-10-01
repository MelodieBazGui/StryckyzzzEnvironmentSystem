package eventManager.interfaces;

import eventManager.events.StryckEvent;

@FunctionalInterface
public interface EventListener<T extends StryckEvent> {
    /**
     * @param event the event
     * @return true if the event is handled and should stop propagation, false to continue
     */
    boolean onEvent(T event);
}
