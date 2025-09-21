package bodies;

import java.util.List;
import math.*;
import math.algorithm.AABB;
import utils.Logger;

public final class ConvexHullShape implements Shape {
    private final List<Vec3> points; // local-space vertices

    private Logger log;
    
    public ConvexHullShape(List<Vec3> points) {
    	this.log = new Logger(this.getClass());
        if (points == null || points.isEmpty()) {
            log.logWarn(null,new IllegalArgumentException("ConvexHullShape requires at least one point"));
        }
        this.points = List.copyOf(points); // immutable copy
		log.logInfo("Copied list of Vec3 to internal list");
    }

    @Override
    public Vec3 support(Vec3 dir, Quat rot, Vec3 pos) {
        // Transform search direction into local space
        Vec3 dirLocal = rot.conjugate().transform(dir);
        log.logInfo("Rotation quaternion conjugated then transformed by directional vector");
        // Avoid zero-length direction
        if (dirLocal.len2() < 1e-12f) {
            dirLocal = new Vec3(1, 0, 0);
            log.logInfo("Distance was next to nothing (1e-12f)");
        }

        float bestDot = Float.NEGATIVE_INFINITY;
        log.startTimer();
        log.logDuration("Finding right local vertex...");
        Vec3 best = points.get(0);
        for (Vec3 p : points) {
            float d = p.dot(dirLocal);
            if (d > bestDot) {
                bestDot = d;
                best = p;
            }
        }
        log.endTimer("Found best vertex in :");
        log.logInfo("Transform chosen vertex back to world space");
        return rot.transform(best).add(pos);

    }

    @Override
    public AABB computeAABB(Quat orientation, Vec3 position) {
        Vec3 min = new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vec3 max = new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        log.startTimer();
        log.logDuration("Completing AABB collision algorithm...");
        Vec3 world;
        for (Vec3 p : points) {
            world = orientation.transform(p).add(position);
            log.logInfo("changed Vec3 world, set it's orientation to the transformation of p and adding positionnal data");
            min.set(
                Math.min(min.getX(), world.getX()),
                Math.min(min.getY(), world.getY()),
                Math.min(min.getZ(), world.getZ())
            );
            log.logInfo("Found min for AABB");
            max.set(
                Math.max(max.getX(), world.getX()),
                Math.max(max.getY(), world.getY()),
                Math.max(max.getZ(), world.getZ())
            );
            log.logInfo("Found max for AABB");
        }
        log.endTimer("Computing AABB");
        return new AABB(min, max);
    }

    @Override
    public Mat3 computeInertia(float mass) {
        log.logInfo("Compute local-space AABB of the points");
        Vec3 min = new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vec3 max = new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        log.startTimer();
        for (Vec3 p : points) {
            min.set(
                Math.min(min.getX(), p.getX()),
                Math.min(min.getY(), p.getY()),
                Math.min(min.getZ(), p.getZ())
            );
            log.logInfo("Found min for inertia computation");
            max.set(
                Math.max(max.getX(), p.getX()),
                Math.max(max.getY(), p.getY()),
                Math.max(max.getZ(), p.getZ())
            );
            log.logInfo("Found max for inertia computation");
        }
        log.endTimer("Loop throughout ConvexHullShape");
        
        float width  = max.getX() - min.getX();
        float height = max.getY() - min.getY();
        float depth  = max.getZ() - min.getZ();
        log.logInfo("Calculated width, height and depth");
        
        // Inertia tensor of a solid box aligned with principal axes:
        // Ixx = 1/12 * m * (h² + d²)
        // Iyy = 1/12 * m * (w² + d²)
        // Izz = 1/12 * m * (w² + h²)
        float coeff = mass / 12.0f;
        float Ixx = coeff * (height * height + depth * depth);
        float Iyy = coeff * (width * width + depth * depth);
        float Izz = coeff * (width * width + height * height);
        log.logInfo("Calculated InertiaTensor, returning its diagonal matrix");
        log = null;
        return Mat3.diag(Ixx, Iyy, Izz);
    }
    
}
