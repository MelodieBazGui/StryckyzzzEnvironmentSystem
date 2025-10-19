package ecs;

import ecs.*;
import ecs.components.AdaptiveMusicSystem;
import ecs.components.AudioLayerComponent;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveMusicSystemTest {

    private ECSManager ecs;
    private AdaptiveMusicSystem system;
    private Entity entity;
    private AudioLayerComponent comp;

    @BeforeEach
    void setup() {
        ecs = new ECSManager();
        system = new AdaptiveMusicSystem();

        entity = ecs.createEntity();
        comp = new AudioLayerComponent();
        comp.addLayer("ambient", "ambient.mid");
        comp.addLayer("combat", "combat.mid");

        ecs.getComponentManager().addComponent(entity.getId(), comp);
    }

    @Test
    void testLayerActivationAndCrossfade() throws Exception {
        system.activateLayer(comp, "ambient");
        system.update(ecs, 0.016f);
        Thread.sleep(100);

        assertTrue(comp.getLayer("ambient").active);
        assertFalse(comp.getLayer("combat").active);

        // Simulate transition to combat
        system.activateLayer(comp, "combat");
        system.update(ecs, 0.016f);
        Thread.sleep(100);

        assertTrue(comp.getLayer("combat").targetVolume > 0.5f);
        assertEquals(0.0f, comp.getLayer("ambient").targetVolume);
    }

    @AfterEach
    void cleanup() {
        system.shutdown();
    }
}
