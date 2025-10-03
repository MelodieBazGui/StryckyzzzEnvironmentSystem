package eventSystemTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import eventManager.enumerations.StryckEventType;
import eventManager.events.KeyboardPressedEvent;
import eventManager.events.StryckEvent;
import eventManager.events.WindowCloseEvent;
import eventManager.events.WindowResizeEvent;
import eventManager.managing.StryckEventDispatcher;
import eventManager.managing.StryckEventQueue;

class EventSystemTest {

    @Test
    void testWindowResizeEventFields() {
        WindowResizeEvent e = new WindowResizeEvent(1280, 720);
        assertEquals(1280, e.getWidth());
        assertEquals(720, e.getHeight());
        assertEquals(StryckEventType.WindowResize, e.getEventType());
    }

    @Test
    void testDispatcherCatchesCorrectEvent() {
        WindowCloseEvent closeEvent = new WindowCloseEvent();
        StryckEventDispatcher dispatcher = new StryckEventDispatcher(closeEvent);

        final boolean[] handled = {false};
        dispatcher.dispatch(WindowCloseEvent.class, (WindowCloseEvent ev) -> {
            handled[0] = true;
            return true; // mark handled
        });

        assertTrue(handled[0], "Dispatcher should call the handler");
        assertTrue(closeEvent.isHandled(), "Event should be marked handled");
    }

    @Test
    void testQueuePushPollOrder() {
        StryckEventQueue q = new StryckEventQueue();
        q.push(new KeyboardPressedEvent(32, 0)); // space
        q.push(new WindowResizeEvent(800, 600));
        assertFalse(q.isEmpty());
        StryckEvent first = q.poll();
        assertTrue(first instanceof KeyboardPressedEvent);
        StryckEvent second = q.poll();
        assertTrue(second instanceof WindowResizeEvent);
        assertTrue(q.isEmpty());
    }

    @Test
    void testQueueThreadSafety() throws InterruptedException {
        final int N = 1000;
        StryckEventQueue q = new StryckEventQueue();
        CountDownLatch pDone = new CountDownLatch(1);
        Thread producer = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                q.push(new KeyboardPressedEvent(i, 0));
            }
            pDone.countDown();
        });

        Thread consumer = new Thread(() -> {
            try {
                int taken = 0;
                while (taken < N) {
                    StryckEvent e = q.take(); // blocking
                    if (e != null) {
						taken++;
					}
                }
            } catch (InterruptedException ignored) {
            }
        });

        producer.start();
        consumer.start();
        pDone.await(); // wait until producer finished pushing
        producer.join();
        consumer.join();
        assertTrue(q.isEmpty(), "Queue should be empty after producer/consumer finished");
    }

    @Test
    void testLoggerOutputContainsObjectTag() {
        // capture console
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(out));

        WindowCloseEvent ev = new WindowCloseEvent();
        // creating event logs to console
        System.setOut(oldOut);
        String s = out.toString();
        // should include the class tag and timestamp, at least the class name
        assertTrue(s.contains("WindowCloseEvent"), "Console log should contain the event class name");
    }
}
