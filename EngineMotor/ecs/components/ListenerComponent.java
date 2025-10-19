package ecs.components;

import ecs.Component;
import math.Vec3;

/** Single audio listener (e.g., camera/player). */
public class ListenerComponent implements Component {
    public Vec3 position = new Vec3(0,0,0);
    public Vec3 forward = new Vec3(0,0,-1);
    public float maxDistance = 50f;
}
