package constraints;

import bodies.Shape;
import math.Vec3;

/**
 * A constraint ties two bodies together with a condition (e.g. fixed distance).
 * Implementations should resolve positional/velocity errors in `solve`.
 */
public interface Constraint {
    int getId();
    void setId(int id);

    Shape getBodyA();
    Shape getBodyB();

    /** Solve constraint for this timestep. */
    void solve(float dt);

    /** Optional debug draw point(s). */
    default Vec3[] getAnchorPoints() { return new Vec3[0]; }
}
