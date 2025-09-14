package mathTest;

import math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DynamicAABBTreeTest {

    @Test
    void testInsertAndQuery() {
        DynamicAABBTree tree = new DynamicAABBTree();
        int idA = 1;
        int idB = 2;

        tree.insert(idA, new AABB(new Vec3(0,0,0), new Vec3(1,1,1)));
        tree.insert(idB, new AABB(new Vec3(0.5f,0.5f,0.5f), new Vec3(1.5f,1.5f,1.5f)));

        var pairs = tree.queryAllPairs();
        assertEquals(1, pairs.size());
        assertTrue((pairs.get(0)[0] == idA && pairs.get(0)[1] == idB) ||
                   (pairs.get(0)[0] == idB && pairs.get(0)[1] == idA));
    }
}
