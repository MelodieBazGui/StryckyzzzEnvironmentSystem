package math.algorithm;

import java.util.ArrayList;
import java.util.List;

import bodies.Shape;
import math.Quat;
import math.Vec3;

/**
 * Robust GJK using Shape.support(dir, quat, pos) API.
 *
 * - Tolerates small numerical projections so touching is detected.
 * - Keeps allocations modest (simple ArrayList for simplex).
 * - Exposes Result with boolean intersect and simplex Vec3[] (newest first).
 */
public final class GJK {
    private static final int MAX_IT = 64;
    private static final float EPS = 1e-6f;           // geometric eps
    private static final float PROJ_EPS = 1e-7f;      // projection advance tolerance
    private static final float DIR_FALLBACK_EPS = 1e-12f;

    private GJK(){}

    public static final class Result {
        public final boolean intersect;
        // simplex: newest point at index 0
        public final Vec3[] simplex;
        public Result(boolean intersect, Vec3[] simplex){
            this.intersect = intersect;
            this.simplex = simplex;
        }
        public boolean intersects() {return intersect;}
        public Vec3[] getSimplex() {return simplex;}
    }

    /**
     * Main GJK intersection test.
     *
     * A, B : shapes
     * qA, pA : orientation + position of A (world)
     * qB, pB : orientation + position of B (world)
     */
    public static Result intersect(
            Shape A, Shape B,
            Quat qA, Vec3 pA,
            Quat qB, Vec3 pB
    ){
        // initial direction A -> B
        Vec3 dir = Vec3.sub(pB, pA);
        if (dir.len2() < EPS) {
			dir = new Vec3(1f, 0f, 0f);
		}

        List<Vec3> simplex = new ArrayList<>(4);
        Vec3 newDir = new Vec3(); // next search direction
        float lastProj = Float.NEGATIVE_INFINITY;

        for (int iter = 0; iter < MAX_IT; iter++) {
            Vec3 support = support(A, B, qA, pA, qB, pB, dir);
            float proj = support.dot(dir);

            // Accept touching and overlapping as collision.
            // If support doesn't pass the origin in direction dir, shapes are separated.
            if (proj < -EPS) {
                return new Result(false, simplexToArray(simplex));
            }

            // If support doesn't advance enough along the search direction, treat as separated.
            if (proj - lastProj <= PROJ_EPS && lastProj != Float.NEGATIVE_INFINITY) {
                // no meaningful progression; assume separated
                return new Result(true, simplexToArray(simplex));
            }
            lastProj = Math.max(lastProj, proj);

            // add newest support at front
            simplex.add(0, support.cpy());

            // compute new search direction based on simplex
            boolean containsOrigin = handleSimplex(simplex, newDir);
            if (containsOrigin) {
                return new Result(true, simplexToArray(simplex));
            }

            // fallback if degenerate
            if (newDir.len2() < DIR_FALLBACK_EPS) {
                newDir.set(fallbackDirection(simplex));
            }
            dir.set(newDir);
        }

        // If we exhausted iterations without separation, assume intersecting
        return new Result(false, simplexToArray(simplex));
    }

    // Convert simplex list (newest first) to array copies.
    private static Vec3[] simplexToArray(List<Vec3> simplex){
        if (simplex == null || simplex.isEmpty()) {
			return new Vec3[0];
		}
        Vec3[] arr = new Vec3[simplex.size()];
        for (int i = 0; i < simplex.size(); i++) {
			arr[i] = simplex.get(i).cpy();
		}
        return arr;
    }

    // Minkowski support using Shape API: world-space direction passed to shapes.
    private static Vec3 support(
            Shape A, Shape B,
            Quat qA, Vec3 pA,
            Quat qB, Vec3 pB,
            Vec3 dirWorld
    ){
        // shapes expect world-space dir + their pose
        Vec3 pAW = A.support(dirWorld.cpy(), qA, pA);
        Vec3 pBW = B.support(dirWorld.cpy().scl(-1f), qB, pB);
        return Vec3.sub(pAW, pBW);
    }

    // Fallback direction if degenerate: choose any vector roughly perpendicular to newest simplex point.
    private static Vec3 fallbackDirection(List<Vec3> simplex){
        if (simplex == null || simplex.isEmpty()) {
			return new Vec3(1f, 0f, 0f);
		}
        Vec3 a = simplex.get(0);
        // try simple perpendiculars
        if (Math.abs(a.getX()) > Math.abs(a.getY())) {
			return new Vec3(-a.getZ(), 0f, a.getX()).normalize();
		} else {
			return new Vec3(0f, a.getZ(), -a.getY()).normalize();
		}
    }

    // handleSimplex operates on list with newest at index 0, writes search dir into dirOut.
    // returns true if simplex contains (or encloses) the origin.
    private static boolean handleSimplex(List<Vec3> simplex, Vec3 dirOut){
        if (simplex.size() == 0){
            dirOut.set(1f,0f,0f);
            return false;
        }
        if (simplex.size() == 1){
            // direction towards origin from A
            Vec3 A = simplex.get(0);
            dirOut.set(A.cpy().scl(-1f));
            return false;
        }
        if (simplex.size() == 2){
            Vec3 A = simplex.get(0), B = simplex.get(1);
            Vec3 AB = Vec3.sub(B, A);
            Vec3 AO = Vec3.neg(A);
            // direction perpendicular to AB towards origin
            if (AB.dot(AO) > 0f){
                Vec3 abXao = AB.cross(AO);
                dirOut.set(abXao.cross(AB));
            } else {
                // drop B
                simplex.remove(1);
                dirOut.set(AO);
            }
            return false;
        }
        if (simplex.size() == 3){
            Vec3 A = simplex.get(0), B = simplex.get(1), C = simplex.get(2);
            Vec3 AB = Vec3.sub(B, A), AC = Vec3.sub(C, A);
            Vec3 AO = Vec3.neg(A);
            Vec3 ABC = AB.cross(AC);

            // region AB
            Vec3 abPerp = ABC.cross(AB);
            if (abPerp.dot(AO) > 0f){
                simplex.remove(2); // drop C
                // new direction: perpendicular to AB towards origin
                dirOut.set(AB.cross(AO).cross(AB));
                return false;
            }
            // region AC
            Vec3 acPerp = AC.cross(ABC);
            if (acPerp.dot(AO) > 0f){
                simplex.remove(1); // drop B
                dirOut.set(AC.cross(AO).cross(AC));
                return false;
            }
            // origin is within triangle region (or above/below)
            if (ABC.dot(AO) > 0f){
                dirOut.set(ABC);
            } else {
                // flip winding so normal points towards origin
                simplex.set(1, C); simplex.set(2, B);
                dirOut.set(ABC.scl(-1f));
            }
            return false;
        }
        // tetrahedron
        if (simplex.size() >= 4){
            Vec3 A = simplex.get(0), B = simplex.get(1), C = simplex.get(2), D = simplex.get(3);
            Vec3 AO = Vec3.neg(A);
            Vec3 AB = Vec3.sub(B, A), AC = Vec3.sub(C, A), AD = Vec3.sub(D, A);
            Vec3 ABC = AB.cross(AC), ACD = AC.cross(AD), ADB = AD.cross(AB);

            if (ABC.dot(AO) > 0f){
                // origin is on the ABC side; drop D
                simplex.remove(3); // drop D
                return handleSimplex(simplex, dirOut);
            }
            if (ACD.dot(AO) > 0f){
                // origin is on the ACD side; drop B
                simplex.remove(1); // drop B
                return handleSimplex(simplex, dirOut);
            }
            if (ADB.dot(AO) > 0f){
                // origin is on the ADB side; drop C
                simplex.remove(2); // drop C
                return handleSimplex(simplex, dirOut);
            }
            // origin is inside tetrahedron
            return true;
        }
        return false;
    }
}
