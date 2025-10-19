package ecs.components;

import ecs.Component;
import math.Vec3;

/** Circular zone that applies audio modifiers when listener is inside. */
public class SoundZoneComponent implements Component {
    public Vec3 center = new Vec3(0,0,0);
    public float radius = 10f;
    public float zoneReverb = 0.6f;
    public float zoneLowPass = 4000f;
}

