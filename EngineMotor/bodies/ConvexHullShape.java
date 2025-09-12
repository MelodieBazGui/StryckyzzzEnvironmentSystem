package bodies;

import java.util.List;
import math.*;

/**
 * Simple convex hull defined by a point list in local space.
 * 
 * Provides support function for GJK/EPA and computes world AABB.
 */
public final class ConvexHullShape implements Shape {
    private final List<Vec3> points; // local space vertices, immutable

    public ConvexHullShape(List<Vec3> points){
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("ConvexHullShape requires non-empty point list");
        }
        this.points = List.copyOf(points);
    }

    @Override
    public Vec3 support(Vec3 dir){
        float best = points.get(0).dot(dir);
        Vec3 bestP = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Vec3 p = points.get(i);
            float val = p.dot(dir);
            if (val > best) {
                best = val;
                bestP = p;
            }
        }
        return bestP.cpy(); // safe return
    }

    @Override
    public AABB computeAABB(Quat orientation, Vec3 position){
        Vec3 min = new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vec3 max = new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for (Vec3 p : points){
            // To reduce allocations, transform into a temp instead of new object
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