package ecs;

import ecs.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queues ECS mutations to apply after current system updates finish.
 */
public class DeferredCommandBuffer {
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();

    public void addCommand(Runnable cmd) {
        commands.add(cmd);
    }

    public void addEntity(ECSManager ecs, Entity e) {
        commands.add(() -> ecs.registerEntity(e));
    }

    public void destroyEntity(ECSManager ecs, int id) {
        commands.add(() -> ecs.destroyEntity(id));
    }

    public <T extends Component> void addComponent(ECSManager ecs, int id, T c) {
        commands.add(() -> ecs.addComponentNow(id, c));
    }

    public <T extends Component> void removeComponent(ECSManager ecs, int id, Class<T> type) {
        commands.add(() -> ecs.removeComponentNow(id, type));
    }

    public void flush() {
        while (!commands.isEmpty()) {
            commands.poll().run();
        }
    }
}
