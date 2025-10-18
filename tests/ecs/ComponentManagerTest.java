package ecs;

import ecs.components.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComponentManagerTest {

    @Test
    void testAddAndRetrieveComponent() {
        ComponentManager cm = new ComponentManager();
        TransformComponent tf = new TransformComponent();
        tf.position.set(1, 2, 3);

        cm.addComponent(1, tf);
        TransformComponent fetched = cm.getComponent(1, TransformComponent.class);

        assertNotNull(fetched);
        assertEquals(1, fetched.position.getX());
        assertEquals(3, fetched.position.getZ());
    }

    @Test
    void testRemoveComponent() {
        ComponentManager cm = new ComponentManager();
        RigidbodyComponent rb = new RigidbodyComponent();
        cm.addComponent(42, rb);

        assertTrue(cm.hasComponent(42, RigidbodyComponent.class));
        cm.removeComponent(42, RigidbodyComponent.class);
        assertFalse(cm.hasComponent(42, RigidbodyComponent.class));
    }

    @Test
    void testEntriesForType() {
        ComponentManager cm = new ComponentManager();
        cm.addComponent(1, new TransformComponent());
        cm.addComponent(2, new TransformComponent());

        var entries = cm.entriesForType(TransformComponent.class);
        assertEquals(2, entries.size());
    }
}

