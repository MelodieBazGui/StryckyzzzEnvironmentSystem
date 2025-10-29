package ecs.demo;

import ecs.*;
import ecs.systems.*;
import utils.Logger;

/**
 * Simple demo for ECSManager functionality.
 * Creates entities with Position/Velocity components and updates them using MovementSystem.
 */
public class ECSManagerDemo {
    public static void main(String[] args) {
        Logger log = new Logger(ECSManagerDemo.class);
        log.info("=== ECS Manager Demo Starting ===");

        // Create ECS world
        ECSManager ecs = new ECSManager();

        // Add our MovementSystem
        ecs.addSystem(new MovementSystem());

        // Create a few entities
        for (int i = 0; i < 3; i++) {
            Entity e = ecs.createEntity();
            ecs.addComponentNow(e.getId(), new PositionComponent(i * 10, 0));
            ecs.addComponentNow(e.getId(), new VelocityComponent(1 + i, 0.5f * i));
        }

        // Run a few update frames
        for (int frame = 0; frame < 5; frame++) {
            log.info("--- Frame " + frame + " ---");
            ecs.update(0.016f); // ~60 FPS delta time
            printPositions(ecs);
        }

        // Demonstrate deferred command buffer
        log.info("Queueing deferred commands...");
        for (Entity e : ecs.getAllEntities()) {
            ecs.commands().add(() ->
                ecs.addComponentNow(e.getId(), new TagComponent("UpdatedEntity")));
        }

        ecs.update(0.016f); // flushes deferred commands
        log.info("After deferred commands:");
        printTags(ecs);

        ecs.getAllEntities().forEach(i -> log.info(i + "with id :" +i.getId()));
        ecs.getEntityManager().getAllEntities().forEach(i -> log.info(i + "with id :" +i.getId()));
        
        ecs.shutdown();
        log.info("=== ECS Manager Demo Finished ===");
    }

    private static void printPositions(ECSManager ecs) {
        for (Entity e : ecs.getAllEntities()) {
            PositionComponent pos = ecs.getComponent(e.getId(), PositionComponent.class);
            System.out.printf("Entity %d -> (%.2f, %.2f)%n", e.getId(), pos.x, pos.y);
        }
    }

    private static void printTags(ECSManager ecs) {
        for (Entity e : ecs.getAllEntities()) {
            TagComponent tag = ecs.getComponent(e.getId(), TagComponent.class);
            if (tag != null)
                System.out.printf("Entity %d tagged as '%s'%n", e.getId(), tag.label);
        }
    }

    // -------------------------------------------------------------------------
    // Example components
    // -------------------------------------------------------------------------

    public static class PositionComponent implements Component {
        public float x, y;
        public PositionComponent(float x, float y) { this.x = x; this.y = y; }
    }

    public static class VelocityComponent implements Component {
        public float vx, vy;
        public VelocityComponent(float vx, float vy) { this.vx = vx; this.vy = vy; }
    }

    public static class TagComponent implements Component {
        public final String label;
        public TagComponent(String label) { this.label = label; }
    }

    // -------------------------------------------------------------------------
    // Example system: Moves entities by velocity * deltaTime
    // -------------------------------------------------------------------------
    public static class MovementSystem extends SystemBase {
        @Override
        protected void onInit(ECSManager ecs) {
            log.info("MovementSystem initialized");
        }

        @Override
        public void update(ECSManager ecs, float dt) {
            for (Entity e : ecs.getEntitiesWith(PositionComponent.class, VelocityComponent.class)) {
                PositionComponent pos = ecs.getComponent(e.getId(), PositionComponent.class);
                VelocityComponent vel = ecs.getComponent(e.getId(), VelocityComponent.class);
                pos.x += vel.vx * dt * 60; // scale up for visibility
                pos.y += vel.vy * dt * 60;
            }
        }

        @Override
        protected void onShutdown() {
            log.info("MovementSystem shut down");
        }
    }
}
