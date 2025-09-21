package bodies;

import math.*;
import math.algorithm.AABB;
import utils.Logger;

/**
 * Axis-aligned box in local space, centered at origin.
 * Half extents define its size.
 */
public class BoxShape implements Shape {
    private final Vec3 halfExtents;

    private Logger log;
    
    public BoxShape(Vec3 v) { halfExtents = v;
    log.logInfo("Instanciated BoxShape from a vector");};
    
    public BoxShape(float hx, float hy, float hz) {
        this.halfExtents = new Vec3(hx, hy, hz);
        log.logInfo("Instanciated BoxShape from floating point values");
    }

    @Override
    public Vec3 support(Vec3 dir, Quat rot, Vec3 pos) {
        // rotate dir into local space
        Vec3 dirLocal = rot.conjugate().transform(dir).normalize();

        // pick corner in local space
        float x = dirLocal.getX() >= 0 ? halfExtents.getX() : -halfExtents.getX();
        float y = dirLocal.getY() >= 0 ? halfExtents.getY() : -halfExtents.getY();
        float z = dirLocal.getZ() >= 0 ? halfExtents.getZ() : -halfExtents.getZ();

        Vec3 cornerLocal = new Vec3(x, y, z);

        // transform back to world
        Vec3 cornerWorld = rot.transform(cornerLocal);
        cornerWorld.add(pos);
        return cornerWorld;
    }

    @Override
    public AABB computeAABB(Quat orientation, Vec3 position) {
        // 8 corners of box in local space
        Vec3[] corners = new Vec3[8];
        int i = 0;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    corners[i++] = new Vec3(
                            sx * halfExtents.getX(),
                            sy * halfExtents.getY(),
                            sz * halfExtents.getZ()
                    );
                }
            }
        }

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (Vec3 c : corners) {
            Vec3 w = orientation.transform(c).add(position);
            minX = Math.min(minX, w.getX());
            minY = Math.min(minY, w.getY());
            minZ = Math.min(minZ, w.getZ());
            maxX = Math.max(maxX, w.getX());
            maxY = Math.max(maxY, w.getY());
            maxZ = Math.max(maxZ, w.getZ());
        }

        return new AABB(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
    }

    @Override
    public Mat3 computeInertia(float mass) {
        float x2 = 4 * halfExtents.getX() * halfExtents.getX();
        float y2 = 4 * halfExtents.getY() * halfExtents.getY();
        float z2 = 4 * halfExtents.getZ() * halfExtents.getZ();

        float ix = (1f / 12f) * mass * (y2 + z2);
        float iy = (1f / 12f) * mass * (x2 + z2);
        float iz = (1f / 12f) * mass * (x2 + y2);

        return Mat3.diag(ix, iy, iz);
    }

    public Vec3 getHalfExtents() {
        return halfExtents.cpy();
    }
}
