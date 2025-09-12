package bodies;

import java.util.List;
import math.*;


//simple convex hull defined by point list in local space
public final class ConvexHullShape implements Shape {
 private final List<Vec3> points; // in body space, immutable usage expected

 public ConvexHullShape(List<Vec3> points){
     this.points = List.copyOf(points);
 }
 @Override public Vec3 support(Vec3 dir){
     float best = Float.NEGATIVE_INFINITY;
     Vec3 bestP = new Vec3(0,0,0);
     for(Vec3 p : points){
         float val = p.dot(dir);
         if(val > best){ best = val; bestP = p; }
     }
     return bestP.cpy();
 }
 @Override public AABB computeAABB(Quat orientation, Vec3 position){
     // rotate all points, collect min/max
     Vec3 min = new Vec3(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
     Vec3 max = new Vec3(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
     for(Vec3 p : points){
         Vec3 world = orientation.transform(p);
         world.add(position);
         min.set(Math.min(min.getX(), world.getX()), Math.min(min.getY(), world.getY()), Math.min(min.getZ(), world.getZ()));
         max.set(Math.max(max.getX(), world.getX()), Math.max(max.getY(), world.getY()), Math.max(max.getZ(), world.getZ()));
     }
     return new AABB(min, max);
 }
}