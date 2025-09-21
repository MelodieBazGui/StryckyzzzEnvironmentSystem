package math.algorithm;

import bodies.Shape;
import math.Quat;
import math.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class EPA {
    private static final int MAX_ITER = 256;
    private static final float TOL = 1e-6f;
    private static final float VISIBLE_EPS = 1e-6f;
    private static final float EPS_VOL = 1e-9f; // volume tolerance

    private EPA(){}

    public static final class PenetrationInfo {
        private final Vec3 normal;
        private final float depth;
        public PenetrationInfo(Vec3 normal, float depth){ this.normal = normal; this.depth = depth; }
        public Vec3 getNormal(){ return normal; }
        //This is a little fucked up and doesnt work
        public float getDepth(){ return depth; }
        @Override public String toString(){ return "PenetrationInfo{normal=" + normal + ", depth=" + depth + "}"; }
    }

    public static PenetrationInfo penetrationInfo(
            Vec3[] simplex,
            Shape A, Shape B,
            Quat qA, Vec3 pA,
            Quat qB, Vec3 pB
    ) {
        System.out.println("EPA: start penetrationInfo");
        if (simplex == null || simplex.length == 0) {
            System.out.println("EPA: empty simplex -> returning zero");
            return new PenetrationInfo(new Vec3(0f,1f,0f), 0f);
        }

        // collect unique copies
        List<Vec3> verts = new ArrayList<>();
        for (Vec3 v : simplex) {
            if (!containsApprox(verts, v)) verts.add(v.cpy());
        }

        System.out.println("EPA: initial unique verts from GJK (" + verts.size() + "):");
        for (int i = 0; i < verts.size(); i++) System.out.println("  v[" + i + "] = " + verts.get(i));

        // seed directions
        Vec3[] seedDirs = new Vec3[] {
            new Vec3(1f,0f,0f), new Vec3(-1f,0f,0f),
            new Vec3(0f,1f,0f), new Vec3(0f,-1f,0f),
            new Vec3(0f,0f,1f), new Vec3(0f,0f,-1f)
        };
        int sd = 0;
        while (verts.size() < 4 && sd < seedDirs.length) {
            Vec3 s = support(A,B,qA,pA,qB,pB, seedDirs[sd++]);
            if (!containsApprox(verts, s)) {
                verts.add(s);
                System.out.println("EPA: added seed support " + s);
            }
        }

        int safety = 0;
        while (verts.size() < 4 && safety++ < 200) {
            for (Vec3 v : new ArrayList<>(verts)) {
                Vec3 dir = Vec3.neg(v).cpy();
                if (dir.len2() < 1e-12f) dir = new Vec3(1f,0f,0f);
                Vec3 s = support(A,B,qA,pA,qB,pB, dir);
                if (!containsApprox(verts, s)) {
                    verts.add(s);
                    System.out.println("EPA: added support toward origin " + s);
                    if (verts.size() >= 4) break;
                }
            }
            if (verts.size() < 4) {
                for (Vec3 d : seedDirs) {
                    Vec3 jitter = d.cpy().scl(1f + 1e-3f * verts.size());
                    Vec3 s = support(A,B,qA,pA,qB,pB, jitter);
                    if (!containsApprox(verts, s)) {
                        verts.add(s);
                        System.out.println("EPA: added jitter seed support " + s);
                        break;
                    }
                }
            }
        }

        System.out.println("EPA: final verts count = " + verts.size());
        for (int i = 0; i < verts.size(); i++) System.out.println("  final v[" + i + "] = " + verts.get(i));

        // check non-coplanarity of first 4
        if (verts.size() >= 4) {
            Vec3 a = verts.get(0), b = verts.get(1), c = verts.get(2), d = verts.get(3);
            float vol = Math.abs(Vec3.sub(b,a).cross(Vec3.sub(c,a)).dot(Vec3.sub(d,a)));
            System.out.println("EPA: initial tetra volume check = " + vol);
        } else {
            System.out.println("EPA: fewer than 4 verts after expansion.");
        }

        // pad if necessary (tiny jitter) so we can build faces
        while (verts.size() < 4) {
            Vec3 base = verts.isEmpty() ? new Vec3(1e-3f,0,0) : verts.get(verts.size()-1);
            Vec3 pad = base.cpy().add(new Vec3(1e-3f * verts.size(), 0f, 0f));
            verts.add(pad);
            System.out.println("EPA: padded with " + pad);
        }

        List<Face> faces = new ArrayList<>();
        makeFace(faces, verts.get(0), verts.get(1), verts.get(2));
        makeFace(faces, verts.get(0), verts.get(3), verts.get(1));
        makeFace(faces, verts.get(0), verts.get(2), verts.get(3));
        makeFace(faces, verts.get(1), verts.get(3), verts.get(2));

        System.out.println("EPA: built initial faces:");
        for (Face f : faces) {
            System.out.println("  Face dist=" + f.distance + " normal=" + f.normal + " a=" + f.a + " b=" + f.b + " c=" + f.c);
        }

        for (int iter = 0; iter < MAX_ITER; iter++) {
            Face closest = null;
            float minDist = Float.POSITIVE_INFINITY;
            for (Face f : faces) {
                if (f.distance < minDist) { minDist = f.distance; closest = f; }
            }
            if (closest == null) {
                System.out.println("EPA: no closest face -> break");
                break;
            }
            System.out.println("EPA iter " + iter + ": closest.dist=" + minDist + " closest.normal=" + closest.normal);

            Vec3 dir = closest.normal.cpy();
            Vec3 p = support(A,B,qA,pA,qB,pB, dir);
            float pd = dir.dot(p);
            System.out.println("EPA iter " + iter + ": support p=" + p + " pd=" + pd);

            float progTol = 1e-6f + 1e-6f * Math.max(1f, Math.abs(pd));
            if (pd <= minDist + progTol) {
                System.out.println("EPA: converged (pd <= minDist + progTol). minDist=" + minDist + " pd=" + pd);
                Vec3 outN = closest.normal.cpy().normalize();
                return new PenetrationInfo(outN, minDist);
            }

            List<Face> visible = new ArrayList<>();
            for (Face f : faces) {
                if (f.normal.dot(Vec3.sub(p, f.a)) > VISIBLE_EPS) visible.add(f);
            }
            System.out.println("EPA iter " + iter + ": visible faces = " + visible.size());
            if (visible.isEmpty()) {
                System.out.println("EPA: no visible faces -> numeric fallback");
                return new PenetrationInfo(closest.normal.cpy().normalize(), minDist);
            }

            List<Edge> horizon = new ArrayList<>();
            for (Face f : visible) {
                addEdgeOrCancel(horizon, f.a, f.b);
                addEdgeOrCancel(horizon, f.b, f.c);
                addEdgeOrCancel(horizon, f.c, f.a);
            }
            System.out.println("EPA iter " + iter + ": horizon edges = " + horizon.size());

            faces.removeAll(visible);

            for (Edge e : horizon) {
                makeFace(faces, e.a, e.b, p);
            }
        }

        System.out.println("EPA: reached MAX_ITER -> fallback depth 0");
        return new PenetrationInfo(new Vec3(0f,1f,0f), 0f);
    }

    private static void makeFace(List<Face> faces, Vec3 a, Vec3 b, Vec3 c) {
        Face f = new Face(a,b,c);
        faces.add(f);
    }

    private static Vec3 support(Shape A, Shape B, Quat qA, Vec3 pA, Quat qB, Vec3 pB, Vec3 dirWorld) {
        Vec3 dirA = dirWorld.cpy();
        Vec3 dirB = Vec3.neg(dirWorld).cpy();
        Vec3 pAW = A.support(dirA, qA, pA);
        Vec3 pBW = B.support(dirB, qB, pB);
        return Vec3.sub(pAW, pBW);
    }

    private static final class Edge { final Vec3 a,b; Edge(Vec3 a, Vec3 b){this.a=a;this.b=b;} }
    private static void addEdgeOrCancel(List<Edge> edges, Vec3 a, Vec3 b) {
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (approxEqual(e.b, a) && approxEqual(e.a, b)) { edges.remove(i); return; }
        }
        edges.add(new Edge(a,b));
    }

    private static final class Face {
        final Vec3 a,b,c;
        final Vec3 normal;
        final float distance;
        Face(Vec3 a, Vec3 b, Vec3 c) {
            this.a=a; this.b=b; this.c=c;
            Vec3 ab = Vec3.sub(b,a);
            Vec3 ac = Vec3.sub(c,a);
            Vec3 n = ab.cross(ac);
            float l2 = n.len2();
            if (l2 > 1e-12f) n = n.cpy().normalize();
            else n = new Vec3(0f,1f,0f);

            float d = n.dot(a);
            if (d < 0f) { n = n.cpy().scl(-1f); d = -d; }
            this.normal = n;
            this.distance = d;
        }
    }

    private static boolean approxEqual(Vec3 p, Vec3 q) {
        final float EPS = 1e-6f;
        return Math.abs(p.getX()-q.getX()) < EPS &&
               Math.abs(p.getY()-q.getY()) < EPS &&
               Math.abs(p.getZ()-q.getZ()) < EPS;
    }
    private static boolean containsApprox(List<Vec3> list, Vec3 v) {
        for (Vec3 p : list) if (approxEqual(p, v)) return true;
        return false;
    }
}