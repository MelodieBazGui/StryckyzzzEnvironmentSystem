package Engine;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import math.*;
import bodies.*;

/*
 This engine is similar to the previous PhysicsEngine but:
 - offers a DynamicAABBTree broadphase (toggle via useTree flag)
 - uses GJK for convex narrowphase
 - demonstrates usage of RigidBodyFullInertia for inertia tensor handling
 - fields that mustn't be mutated directly are private with getters
*/
public final class PhysicsEngineWithTree {
    private final Map<Integer, RigidBodyFullInertia> bodies = new ConcurrentHashMap<>();
    private final DynamicAABBTree tree = new DynamicAABBTree();

    private final ExecutorService workers;
    private final int workerCount;
    private final boolean useTree;

    private final int solverIterations = 8;
    private final Vec3 gravity = new Vec3(0f,-9.81f,0f);

    public PhysicsEngineWithTree(boolean useTree){
        this.useTree = useTree;
        workerCount = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
        workers = Executors.newFixedThreadPool(workerCount);
    }

    public void addBody(RigidBodyFullInertia b){
        bodies.put(b.getId(), b);
        AABB aabb = b.getShape().computeAABB(b.getOrientation(), b.getPosition());
        if(useTree) tree.insert(b.getId(), aabb.expand(0.1f));
    }

    public void removeBody(int id){
        bodies.remove(id);
        if(useTree) tree.remove(id);
    }

    public void step(float dt) throws InterruptedException {
        // 1) broadphase: get candidate pairs
        List<int[]> candidatePairs;
        if(useTree){
            // update tree aabbs for moved bodies (naive: update all)
            for(RigidBodyFullInertia b: bodies.values()){
                AABB aabb = b.getShape().computeAABB(b.getOrientation(), b.getPosition()).expand(0.05f);
                tree.update(b.getId(), aabb);
            }
            candidatePairs = tree.queryAllPairs();
        } else {
            // fallback naive O(n^2)
            List<RigidBodyFullInertia> all = new ArrayList<>(bodies.values());
            candidatePairs = new ArrayList<>();
            for(int i=0;i<all.size();i++) for(int j=i+1;j<all.size();j++) candidatePairs.add(new int[]{all.get(i).getId(), all.get(j).getId()});
        }

        // 2) narrowphase -> contacts using GJK
        final ConcurrentLinkedQueue<Contact> contacts = new ConcurrentLinkedQueue<>();
        List<int[]> pairs = new ArrayList<>(candidatePairs);
        int chunk = Math.max(1, pairs.size() / Math.max(1, workerCount));
        List<Runnable> tasks = new ArrayList<>();
        for(int i=0;i<pairs.size(); i+=chunk){
            final int s=i, e=Math.min(pairs.size(), i+chunk);
            tasks.add(() -> {
                for(int k=s;k<e;k++){
                    int[] p = pairs.get(k);
                    RigidBodyFullInertia A = bodies.get(p[0]), B = bodies.get(p[1]);
                    if(A==null || B==null) continue;
                    boolean collide = GJK.intersects(A.getShape(), B.getShape(), A.getOrientation(), A.getPosition(), B.getOrientation(), B.getPosition());
                    if(collide){
                        // create a simple contact with approximate parameters (for demo)
                        Vec3 mid = Vec3.scl(Vec3.add(A.getPosition(), B.getPosition()), 0.5f);
                        Vec3 n = Vec3.sub(B.getPosition(), A.getPosition()); if(n.len2() < 1e-6f) n = new Vec3(1,0,0); else n.normalize();
                        contacts.add(new Contact(A.getId(), B.getId(), mid, n, 0.01f));
                    }
                }
            });
        }
        invokeAll(tasks);

        // 3) build islands (similar to previous engine) and solve (omitted many details)
        // For brevity, we'll just apply a simple impulse correction per contact sequentially
        for(Contact c : contacts){
            RigidBodyFullInertia A = bodies.get(c.a), B = bodies.get(c.b);
            if(A==null || B==null) continue;
            // compute relative velocity at contact (approx)
            Vec3 rA = Vec3.sub(c.point, A.getPosition());
            Vec3 rB = Vec3.sub(c.point, B.getPosition());
            // v + ω × r
            Vec3 vA = Vec3.add(A.getVelocity(), A.getOmega().cross(rA));
            Vec3 vB = Vec3.add(B.getVelocity(), B.getOmega().cross(rB));
            Vec3 vRel = Vec3.sub(vB, vA);
            float vn = vRel.dot(c.normal);
            float restitution = 0f;
            float desiredVN = -restitution * vn;
            float invMassSum = A.getInvMass() + B.getInvMass();
            Mat3 IinvA = A.getInertiaWorldInv(), IinvB = B.getInertiaWorldInv();
            Vec3 rAxn = rA.cross(c.normal);
            Vec3 rBxn = rB.cross(c.normal);
            // rotational scalar denom = n·(Iinv * (r × n) × r)
            Vec3 tmpA = IinvA.mul(rAxn).cross(rA);
            Vec3 tmpB = IinvB.mul(rBxn).cross(rB);
            float rot = c.normal.dot(tmpA) + c.normal.dot(tmpB);
            float denom = invMassSum + rot;
            if(denom < 1e-6f) continue;
            float j = desiredVN / denom;
            Vec3 impulse = Vec3.scl(c.normal, j);
            A.applyImpulse(Vec3.scl(impulse, -1f), rA);
            B.applyImpulse(impulse, rB);
        }

        // 4) integrate
        List<Runnable> integrateTasks = new ArrayList<>();
        List<RigidBodyFullInertia> allBodies = new ArrayList<>(bodies.values());
        int ichunk = Math.max(1, allBodies.size() / Math.max(1, workerCount));
        for(int i=0;i<allBodies.size(); i+=ichunk){
            final int s=i, e=Math.min(allBodies.size(), i+ichunk);
            integrateTasks.add(() -> {
                for(int j=s;j<e;j++) allBodies.get(j).integrate(dt, gravity);
            });
        }
        invokeAll(integrateTasks);

        // 5) update tree with new aabbs
        if(useTree){
            for(RigidBodyFullInertia b: bodies.values()){
                AABB aabb = b.getShape().computeAABB(b.getOrientation(), b.getPosition()).expand(0.05f);
                tree.update(b.getId(), aabb);
            }
        }
    }

    private void invokeAll(List<Runnable> tasks) throws InterruptedException {
        if(tasks.isEmpty()) return;
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for(Runnable r : tasks) workers.submit(() -> { try { r.run(); } finally { latch.countDown(); }});
        latch.await();
    }

    public void shutdown(){ workers.shutdown(); }
}
