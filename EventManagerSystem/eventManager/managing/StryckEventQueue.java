package eventManager.managing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import eventManager.events.StryckEvent;
import utils.Logger;

public class StryckEventQueue {
    private final BlockingQueue<StryckEvent> queue = new LinkedBlockingQueue<>();
    private final Logger logger = new Logger(StryckEventQueue.class);

    /** push = non-blocking enqueue (returns immediately) */
    public void push(StryckEvent event) {
        if (event == null) {
			return;
		}
        queue.offer(event);
        logger.info("PUSH", event);
    }

    /** poll = non-blocking dequeue, returns null if empty */
    public StryckEvent poll() {
        StryckEvent e = queue.poll();
        if (e != null) {
			logger.info("POLL", e);
		}
        return e;
    }

    /** take = blocking dequeue; throws InterruptedException */
    public StryckEvent take() throws InterruptedException {
        StryckEvent e = queue.take();
        logger.info("TAKE", e);
        return e;
    }

    /** poll with timeout (ms) */
    public StryckEvent poll(long timeoutMillis) throws InterruptedException {
        StryckEvent e = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        if (e != null) {
			logger.info("POLL(timeout)", e);
		}
        return e;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
        logger.info("CLEARED QUEUE");
    }

    /** Convenience: process everything currently in the queue using the provided manager */
    public void processAll(StryckEventManager manager) {
        StryckEvent e;
        while ((e = poll()) != null) {
            manager.dispatch(e);
        }
    }
}
