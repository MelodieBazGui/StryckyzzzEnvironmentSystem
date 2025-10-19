package ecs.systems;

import ecs.*;
import ecs.components.SoundComponent;
import audio.SoundManager;

import java.io.File;
import java.util.Map;

public class SoundSystem extends SystemBase {
    private final SoundManager soundManager;

    public SoundSystem(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public void update(ECSManager ecs, float dt) {
        for (Map.Entry<Integer, SoundComponent> entry : ecs.getComponentManager().entriesForType(SoundComponent.class)) {
            SoundComponent sc = entry.getValue();
            if (sc.playOnAdd) {
                sc.playOnAdd = false;
                if (sc.wavPath != null) {
                    soundManager.playWavFile(new File(sc.wavPath));
                }
            }
        }
    }
}
