package ecs.components;

import ecs.Component;
import math.Mat4;
import math.Quat;
import math.Vec3;

/** ECS transform with TRS data and ready-to-upload model matrix builders. */
public final class TransformComponent implements Component {
    public final Vec3 position = new Vec3();
    public final Vec3 scale    = new Vec3(1,1,1);
    public final Quat rotation = Quat.identity(); // (w=1,x=y=z=0)

    /** Row-major model matrix (engine side). */
    public Mat4 model() {
        return Mat4.fromTRS(position, rotation, scale);
    }

    /** Column-major float[16] for JOGL glUniformMatrix4fv(..., transpose=false). */
    public float[] modelColumnMajor() {
        return model().toColumnMajorArray();
    }
}
