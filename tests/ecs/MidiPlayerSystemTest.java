package ecs;

import ecs.components.MidiComponent;
import ecs.systems.MidiPlayerSystem;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class MidiPlayerSystemTest {

    private ECSManager ecs;
    private MidiPlayerSystem system;
	private Entity entity2, entity1;

    @BeforeEach
    void setup() {
        ecs = new ECSManager();
        system = new MidiPlayerSystem();

        entity1 = ecs.createEntity();
        entity2 = ecs.createEntity();

        ecs.getComponentManager().addComponent(entity1.getId(), new MidiComponent("test1.mid", true));
        ecs.getComponentManager().addComponent(entity2.getId(), new MidiComponent("test2.mid", true));
    }

    @Test
    void testParallelMidiPlayback() throws Exception {
        system.update(ecs, 0.016f);
        Thread.sleep(100); // let threads start

        assertTrue(ecs.getComponentManager().getComponent(entity1.getId(), MidiComponent.class).autoPlay);
        assertTrue(ecs.getComponentManager().getComponent(entity2.getId(), MidiComponent.class).autoPlay);
    }

    @AfterEach
    void cleanup() {
        system.shutdown();
    }
}
