package component;

import org.junit.jupiter.api.Test;

import ecs.components.SoundComponent;

import static org.junit.jupiter.api.Assertions.*;

public class SoundComponentTest {

    @Test
    void testDefaultValues() {
        SoundComponent sc = new SoundComponent();
        assertNull(sc.wavPath);
        assertFalse(sc.loop);
        assertTrue(sc.volume > 0);
    }

    @Test
    void testConstructorAssignsPath() {
        SoundComponent sc = new SoundComponent("example.wav");
        assertEquals("example.wav", sc.wavPath);
    }
}
