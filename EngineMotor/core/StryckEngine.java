package core;

import eventManager.managing.StryckEventManager;
import gameHandlers.CameraMouvementHandler;
import gameHandlers.MouvementHandler;
import graphics.Camera;
import math.Vec3;
import parametres.MouvementParametres;
import utils.Logger;
import ecs.ECSManager;
import ecs.components.TransformComponent;
import ecs.components.VelocityComponent;
import ecs.systems.MovementSystem;

public class StryckEngine {
    private static final Logger logger = new Logger(StryckEngine.class);

    private boolean running = false;
    private final StryckEventManager eventManager = new StryckEventManager();
    private final DeltaTime deltaTime = new DeltaTime();

    private MouvementHandler movementHandler;
    private CameraMouvementHandler cameraHandler;
    private Camera camera;
    private ECSManager ecsManager;

    public void init() {
        logger.info("Initializing engine systems...");

        camera = new Camera();
        movementHandler = new MouvementHandler(eventManager, new MouvementParametres());
        cameraHandler = new CameraMouvementHandler(eventManager, camera);
        ecsManager = new ECSManager();
        ecsManager.addSystem(new MovementSystem());

        // Create a player
        var player = ecsManager.createEntity();
        player.addComponent(new TransformComponent());
        player.addComponent(new VelocityComponent());


        logger.info("Initialization complete.");
    }

    public void run() {
        logger.info("Starting main loop...");
        running = true;

        while (running) {
            deltaTime.update();
            update(deltaTime.getDelta());
            ecsManager.update(deltaTime.getDelta());
            render();
        }

        cleanup();
    }

    private void update(float dt) {
        // --- ECS Updates ---
        ecsManager.update(dt);

        // --- Player Movement ---
        Vec3 move = movementHandler.getMovementVector();
        if (!move.isZero()) {
            camera.move(move.cpy().scl(dt));
        }

        // --- (CameraMouvementHandler updates itself via events) ---
    }

    private void render() {
        // Later: integrate with renderer
        // For now, just debug
        logger.info("Camera position: " + camera.getPosition());
    }

    public void stop() {
        running = false;
    }

    private void cleanup() {
        logger.info("Cleaning up engine resources...");
    }

    public StryckEventManager getEventManager() {
        return eventManager;
    }

    public Camera getCamera() {
        return camera;
    }

    public ECSManager getEcsManager() {
        return ecsManager;
    }
}

