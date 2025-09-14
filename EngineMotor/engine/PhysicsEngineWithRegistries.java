package engine;

import math.*;
import bodies.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Physics engine with registries for tracking RigidBodies and their IDs.
 */
public final class PhysicsEngineWithRegistries {
    private final IdRegistry<RigidBodyFullInertia> bodyRegistry = new IdRegistry<>();
    private final DynamicAABBTree tree = new DynamicAABBTree();

    private final ExecutorService workers;
    private final int workerCount;
    private final boolean useTree;

    private final Vec3 gravity = new Vec3(0f, -9.81f, 0f);

    public PhysicsEngineWithRegistries(boolean useTree) {
        this.useTree = useTree;
        workerCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        workers = Executors.newFixedThreadPool(workerCount);
    }

    /** Add a body to the registry and broadphase */
    public int addBody(RigidBodyFullInertia b) {
        int id = bodyRegistry.register(b);
        if (useTree) {
            AABB aabb = b.getShape().computeAABB(b.getOrientation(), b.getPosition());
            tree.insert(id, aabb.expand(0.1f));
        }
        return id;
    }

    /** Remove a body (nulls registry + broadphase) */
    public void removeBody(int id) {
        bodyRegistry.unregister(id);
        if (useTree) tree.remove(id);
    }

    public RigidBodyFullInertia getBody(int id) {
        return bodyRegistry.get(id);
    }

    public Set<Integer> activeBodyIds() {
        return bodyRegistry.activeIds();
    }

    /** Step simulation */
    public void step(float dt) throws InterruptedException {
        // 1. Broadphase
        List<int[]> candidatePairs;
        if (useTree) {
            // update tree aabbs
            for (int id : activeBodyIds()) {
                RigidBodyFullInertia b = getBody(id);
                if (b == null) continue;
                AABB aabb = b.getShape().computeAABB(b.getOrientation(), b.getPosition()).expand(0.05f);
                tree.update(id, aabb);
            }
            candidatePairs = tree.queryAllPairs();
        } else {
            List<Integer> ids = new ArrayList<>(activeBodyIds());
            candidatePairs = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    candidatePairs.add(new int[]{ids.get(i), ids.get(j)});
                }
            }
        }

        // 2. Narrowphase (GJK + EPA)
        final ConcurrentLinkedQueue<Contact> contacts = new ConcurrentLinkedQueue<>();
        List<int[]> pairs = new ArrayList<>(candidatePairs);
        int chunk = Math.max(1, pairs.size() / Math.max(1, workerCount));
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i += chunk) {
            final int s = i, e = Math.min(pairs.size(), i + chunk);
            tasks.add(() -> {
                for (int k = s; k < e; k++) {
                    int[] p = pairs.get(k);
                    RigidBodyFullInertia A = getBody(p[0]);
                    RigidBodyFullInertia B = getBody(p[1]);
                    if (A == null || B == null) continue;

                    GJK.Result result = GJK.intersect(
                        A.getShape(), B.getShape(),
                        A.getOrientation(), A.getPosition(),
                        B.getOrientation(), B.getPosition()
                    );
                    if (result.intersect) {
                        EPA.PenetrationInfo pen = EPA.penetrationInfo(
                            result.getSimplex(), A.getShape(), B.getShape(),
                            A.getOrientation(), A.getPosition(),
                            B.getOrientation(), B.getPosition()
                        );
                        Vec3 contactPoint = Vec3.scl(Vec3.add(A.getPosition(), B.getPosition()), 0.5f);
                        contacts.add(new Contact(A.getId(), B.getId(), contactPoint, pen.getNormal(), pen.getDepth()));
                    }
                }
            });
        }
        invokeAll(tasks);

        // 3. Simple solver (impulses)
        for (Contact c : contacts) {
            RigidBodyFullInertia A = getBody(c.a);
            RigidBodyFullInertia B = getBody(c.b);
            if (A == null || B == null) continue;

            Vec3 rA = Vec3.sub(c.point, A.getPosition());
            Vec3 rB = Vec3.sub(c.point, B.getPosition());
            Vec3 vA = Vec3.add(A.getVelocity(), A.getOmega().cross(rA));
            Vec3 vB = Vec3.add(B.getVelocity(), B.getOmega().cross(rB));
            Vec3 vRel = Vec3.sub(vB, vA);
            float vn = vRel.dot(c.normal);

            float restitution = 0f;
            float desiredVN = -restitution * vn;

            float invMassSum = A.getInvMass() + B.getInvMass();
            Vec3 rAxn = rA.cross(c.normal);
            Vec3 rBxn = rB.cross(c.normal);

            float rot = c.normal.dot(A.getInertiaWorldInv().mul(rAxn).cross(rA))
                       + c.normal.dot(B.getInertiaWorldInv().mul(rBxn).cross(rB));
            float denom = invMassSum + rot;
            if (denom < 1e-6f) continue;

            float j = desiredVN / denom;
            Vec3 impulse = Vec3.scl(c.normal, j);
            A.applyImpulse(Vec3.scl(impulse, -1f), rA);
            B.applyImpulse(impulse, rB);
        }

        // 4. Integrate
        List<RigidBodyFullInertia> allBodies = new ArrayList<>(bodyRegistry.values());
        int ichunk = Math.max(1, allBodies.size() / Math.max(1, workerCount));
        List<Runnable> integrateTasks = new ArrayList<>();
        for (int i = 0; i < allBodies.size(); i += ichunk) {
            final int s = i, e = Math.min(allBodies.size(), i + ichunk);
            integrateTasks.add(() -> {
                for (int j = s; j < e; j++) allBodies.get(j).integrate(dt, gravity);
            });
        }
        invokeAll(integrateTasks);
    }

    private void invokeAll(List<Runnable> tasks) throws InterruptedException {
        if (tasks.isEmpty()) return;
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable r : tasks) workers.submit(() -> { try { r.run(); } finally { latch.countDown(); } });
        latch.await();
    }

    public void shutdown() { workers.shutdown(); }
}
