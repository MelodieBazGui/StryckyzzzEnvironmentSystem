package ecs;

import org.junit.jupiter.api.*;
import ecs.components.*;
import ecs.systems.SoundSystem;
import audio.*;


import static org.junit.jupiter.api.Assertions.*;

public class SoundSystemTest {

    private ECSManager ecs;
    private SoundManager manager;
    private SoundSystem system;

    @BeforeEach
    void setup() {
        ecs = new ECSManager();
        manager = new SoundManager();
        system = new SoundSystem(manager);
    }

    @Test
    void testPlayOnAddTriggersPlayback() {
        Entity entity = ecs.createEntity();
        SoundComponent sc = new SoundComponent("test_tone.wav");
        sc.playOnAdd = true;

        ecs.getComponentManager().addComponent(entity.getId(), sc);

        assertDoesNotThrow(() -> system.update(ecs, 0.016f));
    }

    @AfterEach
    void teardown() {
        manager.shutdown();
    }
}

