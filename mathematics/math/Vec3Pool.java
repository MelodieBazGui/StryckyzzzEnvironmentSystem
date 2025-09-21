package math;

import java.util.ArrayDeque;

/**
 * Small thread-local Vec3 pool for temporary allocations inside hot loops.
 * Use obtain() and free(vec) to help reduce GC from short-lived Vec3 objects.
 *
 * This pool is advisory: your code can still use Vec3.cpy() etc. Use sparingly.
 */
public final class Vec3Pool {
    private static final int MAX_STACK = 64;
    private static final ThreadLocal<ArrayDeque<Vec3>> TL = ThreadLocal.withInitial(ArrayDeque::new);

    private Vec3Pool() {}

    public static Vec3 obtain() {
        ArrayDeque<Vec3> q = TL.get();
        Vec3 v = q.pollFirst();
        if (v == null) return new Vec3();
        	v.set(0f,0f,0f);
        return v; 
    }

    public static Vec3 obtain(float x, float y, float z) {
        Vec3 v = obtain();
        v.set(x,y,z);
        return v;
    }

    public static void free(Vec3 v) {
        ArrayDeque<Vec3> q = TL.get();
        if (q.size() < MAX_STACK) {
        	v.set(0f,0f,0f);
        	q.addFirst(v);}
    }
}
