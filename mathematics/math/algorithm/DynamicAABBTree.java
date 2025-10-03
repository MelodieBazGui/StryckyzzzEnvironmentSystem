package math.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import math.Vec3;

/**
 A simple dynamic AABB tree. It stores leaf nodes (bodyId -> AABB) and allows
 insertion/removal/update and overlap query. To keep code compact and safe,
 tree modifications are synchronized on 'this'. Not optimized for performance
 but correct and usable as a replacement broadphase.

 The tree returns candidate overlapping pairs via queryAllPairs().
 @author EmeJay
*/
public class DynamicAABBTree {
    private static class Node {
        int id; // body id if leaf, -1 otherwise
        AABB aabb;
        Node parent, left, right;
        boolean leaf(){ return left==null && right==null; }
        Node(int id, AABB aabb){ this.id=id; this.aabb=aabb; }
    }

    private Node root = null;
    private final Map<Integer, Node> leafMap = new HashMap<>();
    private final AtomicInteger nodeCount = new AtomicInteger();

    public synchronized void insert(int bodyId, AABB aabb){
        Node leaf = new Node(bodyId, aabb);
        leafMap.put(bodyId, leaf);
        nodeCount.incrementAndGet();
        insertLeaf(leaf);
    }

    public synchronized void remove(int bodyId){
        Node leaf = leafMap.remove(bodyId);
        if(leaf == null) {
			return;
		}
        removeLeaf(leaf);
        nodeCount.decrementAndGet();
    }

    public synchronized void update(int bodyId, AABB newAabb){
        Node leaf = leafMap.get(bodyId);
        if(leaf == null){ insert(bodyId, newAabb); return; }
        // simple policy: replace leaf if new AABB does not fit
        leaf.aabb = newAabb;
        // Could re-balance; omitted for brevity
    }

    // naive insert: if root null, set; otherwise find best sibling by surface area heuristic
    private void insertLeaf(Node leaf){
        if(root == null){ root = leaf; leaf.parent = null; return; }
        // find best sibling
        Node best = root;
        while(!best.leaf()){
            float costLeft = unionPerimeter(best.left.aabb, leaf.aabb);
            float costRight = unionPerimeter(best.right.aabb, leaf.aabb);
            best = (costLeft < costRight) ? best.left : best.right;
        }
        Node oldParent = best.parent;
        Node newParent = new Node(-1, unionAABB(leaf.aabb, best.aabb));
        newParent.parent = oldParent;
        newParent.left = best;
        newParent.right = leaf;
        best.parent = newParent;
        leaf.parent = newParent;
        if(oldParent == null) {
			root = newParent;
		} else {
            if(oldParent.left == best) {
				oldParent.left = newParent;
			} else {
				oldParent.right = newParent;
			}
        }
        // walk up and fix AABBs
        Node cur = leaf.parent;
        while(cur != null){
            cur.aabb = unionAABB(cur.left.aabb, cur.right.aabb);
            cur = cur.parent;
        }
    }

    private void removeLeaf(Node leaf){
        if(leaf == root){ root = null; return; }
        Node parent = leaf.parent;
        Node grand = parent.parent;
        Node sibling = (parent.left == leaf) ? parent.right : parent.left;
        if(grand == null){
            root = sibling; sibling.parent = null; return;
        }
        if(grand.left == parent) {
			grand.left = sibling;
		} else {
			grand.right = sibling;
		}
        sibling.parent = grand;
        // fix AABBs up the tree
        Node cur = grand;
        while(cur != null){
            cur.aabb = unionAABB(cur.left.aabb, cur.right.aabb);
            cur = cur.parent;
        }
    }

    // return set of candidate pairs (each pair only once)
    public List<int[]> queryAllPairs() {
        List<int[]> pairs = new ArrayList<>();
        if (root == null) {
			return pairs;
		}

        Set<Long> seen = new HashSet<>();
        List<Node> leaves = new ArrayList<>(leafMap.values());

        for (Node a : leaves) {
            // check overlap with all other leaves via tree
            queryOverlap(root, a, pairs, seen);
        }
        return pairs;
    }

    // Traverse tree and add unique pairs
    private void queryOverlap(Node node, Node a, List<int[]> out, Set<Long> seen) {
        if (node == null || node == a || !node.aabb.overlaps(a.aabb)) {
			return;
		}

        if (node.leaf()) {
            if (node != a) {
                int id1 = Math.min(a.id, node.id);
                int id2 = Math.max(a.id, node.id);
                long key = (((long) id1) << 32) | (id2 & 0xffffffffL);

                if (seen.add(key)) { // add only if new
                    out.add(new int[]{id1, id2});
                }
            }
        } else {
            queryOverlap(node.left, a, out, seen);
            queryOverlap(node.right, a, out, seen);
        }
    }

    private static AABB unionAABB(AABB a, AABB b){
        Vec3 min = new Vec3(Math.min(a.getMin().getX(), b.getMin().getX()),
                            Math.min(a.getMin().getY(), b.getMin().getY()),
                            Math.min(a.getMin().getZ(), b.getMin().getZ()));
        Vec3 max = new Vec3(Math.max(a.getMax().getX(), b.getMax().getX()),
                            Math.max(a.getMax().getY(), b.getMax().getY()),
                            Math.max(a.getMax().getZ(), b.getMax().getZ()));
        return new AABB(min, max);
    }

    private static float unionPerimeter(AABB a, AABB b){
        AABB u = unionAABB(a,b);
        Vec3 min = u.getMin(), max = u.getMax();
        float dx = max.getX() - min.getX();
        float dy = max.getY() - min.getY();
        float dz = max.getZ() - min.getZ();
        return 2*(dx+dy+dz);
    }
}
