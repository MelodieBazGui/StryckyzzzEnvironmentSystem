package constraintsTest;

import org.junit.jupiter.api.Test;

import bodies.RigidBodyFullInertia;
import bodies.SphereShape;
import constraints.Constraint;
import constraints.DistanceJoint;
import math.Quat;
import math.Vec3;
import registries.ConstraintRegistry;
import registries.ShapeRegistry;

class ConstraintRegistryTest {
    @Test
    void registerAndSolveConstraint() {
        ShapeRegistry shapeReg = new ShapeRegistry();
        SphereShape box = new SphereShape(new Vec3(1,1,1));
        shapeReg.register(box);

        RigidBodyFullInertia a = new RigidBodyFullInertia(box, new Vec3(0,0,0), new Quat(), 1f);
        RigidBodyFullInertia b = new RigidBodyFullInertia(box, new Vec3(2,0,0), new Quat(), 1f);

        Constraint c = new DistanceJoint(a, new Vec3(0,0,0), b, new Vec3(0,0,0));

        ConstraintRegistry reg = new ConstraintRegistry();
        int id = reg.register(c);
        assertTrue(reg.isActive(id));

        // solve should pull them back towards rest length
        float before = Vec3.sub(b.getPosition(), a.getPosition()).len();
        c.solve(1f);
        float after = Vec3.sub(b.getPosition(), a.getPosition()).len();

        assertEquals(before, after, 1e-5, "distance joint should enforce same rest length");
    }
}
