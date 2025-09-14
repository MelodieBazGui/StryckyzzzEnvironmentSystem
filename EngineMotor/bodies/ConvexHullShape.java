package bodies;

import java.util.List;
import math.*;

public final class ConvexHullShape implements Shape {
    private final List<Vec3> points; // local-space vertices

    public ConvexHullShape(List<Vec3> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("ConvexHullShape requires at least one point");
        }
        this.points = List.copyOf(points); // immutable copy
    }

    @Override
    public Vec3 support(Vec3 dir, Quat rot, Vec3 pos) {
        // Transform search direction into local space
        Vec3 dirLocal = rot.conjugate().transform(dir);

        // Avoid zero-length direction
        if (dirLocal.len2() < 1e-12f) {
            dirLocal = new Vec3(1, 0, 0);
        }

        // Find furthest local vertex
        float bestDot = Float.NEGATIVE_INFINITY;
        Vec3 best = points.get(0);
        for (Vec3 p : points) {
            float d = p.dot(dirLocal);
            if (d > bestDot) {
                bestDot = d;
                best = p;
            }
        }

        // Transform chosen vertex back to world space
        return rot.transform(best).add(pos);
    }

    @Override
    public AABB computeAABB(Quat orientation, Vec3 position) {
        Vec3 min = new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vec3 max = new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for (Vec3 p : points) {
            Vec3 world = orientation.transform(p).add(position);
            min.set(
                Math.min(min.getX(), world.getX()),
                Math.min(min.getY(), world.getY()),
                Math.min(min.getZ(), world.getZ())
            );
            max.set(
                Math.max(max.getX(), world.getX()),
                Math.max(max.getY(), world.getY()),
                Math.max(max.getZ(), world.getZ())
            );
        }

        return new AABB(min, max);
    }
}
