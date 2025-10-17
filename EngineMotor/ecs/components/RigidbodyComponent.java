package ecs.components;

import ecs.Component;
import math.Vec3;

public class RigidbodyComponent implements Component {
    public Vec3 velocity = new Vec3();
    public Vec3 acceleration = new Vec3();
    public float drag = 0.05f;
    public boolean useGravity = true;
}
