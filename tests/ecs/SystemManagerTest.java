package ecs;

import org.junit.jupiter.api.*;

import ecs.systems.SystemManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended test for SystemManager including deferred command buffer.
 */
public class SystemManagerTest {

    private SystemManager manager;
    private ECSManager ecs;

    @BeforeEach
    void setup() {
        manager = new SystemManager(4);
        ecs = new ECSManager();
    }

    @AfterEach
    void cleanup() {
        manager.shutdown();
    }

    @Test
    void testDeferredCommandExecutesAfterUpdate() {
        AtomicBoolean executed = new AtomicBoolean(false);

        manager.enqueueCommand(() -> executed.set(true));

        // Command should not execute immediately
        assertFalse(executed.get());

        manager.updateAll(ecs, 0.016f);

        // Should execute after update
        assertTrue(executed.get());
    }

    @Test
    void testMultipleDeferredCommands() {
        AtomicInteger counter = new AtomicInteger();

        manager.enqueueCommand(counter::incrementAndGet);
        manager.enqueueCommand(counter::incrementAndGet);
        manager.enqueueCommand(counter::incrementAndGet);

        manager.updateAll(ecs, 0.016f);

        assertEquals(3, counter.get(), "All deferred commands should have executed");
        assertEquals(0, manager.getPendingCommandCount(), "No commands should remain pending");
    }

    @Test
    void testCommandsRunEvenIfSystemsFail() {
        manager.register(new SystemBase() {
            @Override
            public void update(ECSManager ecs, float deltaTime) {
                throw new RuntimeException("Intentional failure");
            }
        });

        AtomicBoolean executed = new AtomicBoolean(false);
        manager.enqueueCommand(() -> executed.set(true));

        manager.updateAll(ecs, 0.016f);

        assertTrue(executed.get(), "Deferred commands should still execute even if systems crash");
    }
}

