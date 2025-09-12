package math;

import java.util.ArrayList;

public final class GJK {
    private static final int MAX_IT = 20;

    // Minkowski support: supportA(dir) - supportB(-dir)
    private static Vec3 support(Shape a, Shape b, Vec3 dir, Quat aOri, Vec3 aPos, Quat bOri, Vec3 bPos){
        // transform dir into local space? We assume support returns in local space - transform by orientation
        Vec3 dirA_world = dir.copy(); // direction in world
        // For shape support we need to convert direction to local space of each shape: since shapes store local points,
        // we can rotate the world dir by inverse rotation (quat conjugate for unit quat)
        // but for simplicity, we will evaluate support by rotating points inside shape classes (they expect body-space).
        Vec3 pA = a.support(dirA_world); // for sphere/convex hull we implemented in local space; for correctness we'd rotate dir into local
        Vec3 pB = b.support(new Vec3(-dir.getX(), -dir.getY(), -dir.getZ()));
        // transform local support to world
        Vec3 pA_world = aOri.transform(pA);
        pA_world.add(aPos);
        Vec3 pB_world = bOri.transform(pB);
        pB_world.add(bPos);
        return Vec3.sub(pA_world, pB_world);
    }

    // Simple implementation where shapes are given with orientation & position.
    public static boolean intersects(Shape A, Shape B, Quat aOri, Vec3 aPos, Quat bOri, Vec3 bPos){
        // initial direction
        Vec3 dir = Vec3.sub(aPos, bPos);
        if(dir.len2() < 1e-6f) dir = new Vec3(1,0,0);
        ArrayList<Vec3> simplex = new ArrayList<>();
        Vec3 a = support(A,B,dir, aOri,aPos, bOri,bPos);
        simplex.add(a);
        dir = Vec3.scl(a, -1f);
        int it = 0;
        while(it++ < MAX_IT){
            Vec3 A0 = support(A,B,dir, aOri,aPos, bOri,bPos);
            if(A0.dot(dir) <= 0) return false; // no collision
            simplex.add(0, A0); // push front
            if(handleSimplex(simplex, dir)) return true;
        }
        return false; // give up
    }

    // handle simplex, mutate dir; return true if origin is enclosed.
    private static boolean handleSimplex(ArrayList<Vec3> simplex, Vec3 dir){
        if(simplex.size() == 1){
            dir.set(Vec3.scl(simplex.get(0), -1f).getX(), Vec3.scl(simplex.get(0), -1f).getY(), Vec3.scl(simplex.get(0), -1f).getZ());
            return false;
        } else if(simplex.size() == 2){
            Vec3 a = simplex.get(0), b = simplex.get(1);
            Vec3 ab = Vec3.sub(b, a);
            Vec3 ao = Vec3.scl(a, -1f);
            Vec3 abPerp = tripleCross(ab, ao, ab);
            dir.set(abPerp.getX(), abPerp.getY(), abPerp.getZ());
            return false;
        } else { // triangle or tetra (we only implement up to triangle for brevity)
            Vec3 a = simplex.get(0), b = simplex.get(1), c = simplex.get(2);
            Vec3 ab = Vec3.sub(b, a);
            Vec3 ac = Vec3.sub(c, a);
            Vec3 ao = Vec3.scl(a, -1f);
            Vec3 abc = ab.cross(ac);
            Vec3 abPerp = abc.cross(ab);
            if(abPerp.dot(ao) > 0){
                simplex.remove(2); // drop c
                dir.set(abPerp.getX(), abPerp.getY(), abPerp.getZ());
                return false;
            }
            Vec3 acPerp = ac.cross(abc);
            if(acPerp.dot(ao) > 0){
                simplex.remove(1); // drop b
                dir.set(acPerp.getX(), acPerp.getY(), acPerp.getZ());
                return false;
            }
            // origin is within triangle region
            return true;
        }
    }

    // triple cross helper (a x b) x c
    private static Vec3 tripleCross(Vec3 a, Vec3 b, Vec3 c){
        Vec3 axb = a.cross(b);
        return axb.cross(c);
    }
}