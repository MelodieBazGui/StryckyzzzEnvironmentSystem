package ecs.components;

import ecs.Component;
import math.Vec3;

public class TransformComponent implements Component {
    public Vec3 position = new Vec3();
    public Vec3 rotation = new Vec3();
}
