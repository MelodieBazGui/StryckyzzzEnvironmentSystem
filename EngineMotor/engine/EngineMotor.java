package engine;

import ecs.*;
import ecs.components.GraphicComponent;
import graphics.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class EngineMotor {
    private final AtomicReference<FrameData> frameRef = new AtomicReference<>();
    private final Renderer renderer = new Renderer(frameRef);

    public void start() {
        // init window
        var canvas = renderer.createCanvas(1280, 720);
        // add to JFrame, start loop
    }

    public void update(List<Entity> entities, CameraData cam) {
        List<DrawItem> drawList = new ArrayList<>();
        for (Entity e : entities) {
            GraphicComponent gfx = e.get(GraphicComponent.class);
            if (gfx == null || gfx.model == null) continue;
            TransformComponent t = e.get(TransformComponent.class);
            drawList.add(new DrawItem(
                gfx.model,
                new TransformData(t.worldMatrix()),
                gfx.mode,
                gfx.lineWidth,
                gfx.pointSize
            ));
        }
        frameRef.set(new FrameData(drawList, cam));
    }
}
