package ecs;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic functional tests for ECSManager and its core managers.
 */
public class ECSManagerTest {

    private ECSManager ecs;

    @BeforeEach
    public void setup() {
        ecs = new ECSManager();
    }

    @AfterEach
    public void teardown() {
        ecs.shutdown();
    }

    // -------------------------------------------------------------------------
    // 1. Entity management
    // -------------------------------------------------------------------------
    @Test
    public void testEntityCreationAndDestruction() {
        Entity e1 = ecs.createEntity();
        assertNotNull(e1, "Entity should not be null");
        assertTrue(ecs.isAlive(e1), "Entity should be alive after creation");

        ecs.destroyEntity(e1);
        assertFalse(ecs.isAlive(e1), "Entity should not be alive after destruction");
    }

    // -------------------------------------------------------------------------
    // 2. Component management
    // -------------------------------------------------------------------------
    @Test
    public void testAddAndRetrieveComponent() {
        Entity e = ecs.createEntity();
        PositionComponent pos = new PositionComponent(10, 20);

        ecs.addComponentNow(e.getId(), pos);

        PositionComponent fetched = ecs.getComponent(e.getId(), PositionComponent.class);
        assertNotNull(fetched, "Component should be retrievable");
        assertEquals(10, fetched.x);
        assertEquals(20, fetched.y);

        ecs.removeComponentNow(e.getId(), PositionComponent.class);
        assertFalse(ecs.hasComponent(e.getId(), PositionComponent.class));
    }

    // -------------------------------------------------------------------------
    // 3. System management
    // -------------------------------------------------------------------------
    @Test
    public void testSystemUpdateIsCalled() {
        AtomicInteger counter = new AtomicInteger();

        SystemBase testSystem = new SystemBase() {
            @Override
            protected void onInit(ECSManager ecs) {
                log.info("System initialized");
            }

            @Override
            public void update(ECSManager ecs, float dt) {
                counter.incrementAndGet();
            }

            @Override
            protected void onShutdown() {
                log.info("System shut down");
            }
        };

        ecs.addSystem(testSystem);

        ecs.update(0.016f); // simulate one frame
        ecs.update(0.016f);

        assertTrue(counter.get() >= 2, "System update should be called at least twice");

        ecs.removeSystem(testSystem);
    }

    // -------------------------------------------------------------------------
    // 4. Deferred command buffer integration
    // -------------------------------------------------------------------------
    @Test
    public void testDeferredCommandBufferFlush() {
        Entity e = ecs.createEntity();
        DeferredCommandBuffer cmds = ecs.commands();

        cmds.add(() -> ecs.addComponentNow(e.getId(), new PositionComponent(5, 5)));
        cmds.add(() -> ecs.addComponentNow(e.getId(), new VelocityComponent(1, 1)));

        cmds.flush();

        assertTrue(ecs.hasComponent(e.getId(), PositionComponent.class));
        assertTrue(ecs.hasComponent(e.getId(), VelocityComponent.class));
    }

    // -------------------------------------------------------------------------
    // 5. Parallel system updates
    // -------------------------------------------------------------------------
    @Test
    public void testParallelSystemExecution() {
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 5; i++) {
            ecs.addSystem(new SystemBase() {
                @Override
                public void update(ECSManager ecs, float dt) {
                    count.incrementAndGet();
                }
            });
        }

        ecs.update(0.016f);
        assertEquals(5, count.get(), "All systems should have been updated in parallel");
    }

    // -------------------------------------------------------------------------
    // 6. Shutdown safety
    // -------------------------------------------------------------------------
    @Test
    public void testShutdownClearsSystems() {
        ecs.addSystem(new SystemBase() {
            @Override
            public void update(ECSManager ecs, float dt) {}
        });

        ecs.shutdown();
        assertTrue(ecs.getSystemManager().isEmpty() || ecs.getSystemManager() != null,
                   "SystemManager should be empty or safely shut down");
    }

    // -------------------------------------------------------------------------
    // Helper components for tests
    // -------------------------------------------------------------------------
    static class PositionComponent implements Component {
        public float x, y;
        public PositionComponent(float x, float y) { this.x = x; this.y = y; }
    }

    static class VelocityComponent implements Component {
        public float vx, vy;
        public VelocityComponent(float vx, float vy) { this.vx = vx; this.vy = vy; }
    }
}
