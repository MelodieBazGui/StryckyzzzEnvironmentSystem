package math;

import engine.Mat3;

public final class Quat {
 private float w, x, y, z;
 public Quat(){ setIdentity(); }
 public Quat(float w,float x,float y,float z){ this.w=w; this.x=x; this.y=y; this.z=z; normalize(); }

 public void setIdentity(){ w=1; x=y=z=0; }

 public float getW(){ return w; }
 public float getX(){ return x; }
 public float getY(){ return y; }
 public float getZ(){ return z; }

 // integrate angular velocity (in-place)
 public void integrateAngular(Vec3 omega, float dt){
     float ow=0f, ox=omega.getX(), oy=omega.getY(), oz=omega.getZ();
     // Omega * q
     float rw = ow*w - ox*x - oy*y - oz*z;
     float rx = ow*x + ox*w + oy*z - oz*y;
     float ry = ow*y - ox*z + oy*w + oz*x;
     float rz = ow*z + ox*y - oy*x + oz*w;
     w += 0.5f * dt * rw;
     x += 0.5f * dt * rx;
     y += 0.5f * dt * ry;
     z += 0.5f * dt * rz;
     normalize();
 }

 public void normalize(){
     float mag = (float)Math.sqrt(w*w + x*x + y*y + z*z);
     if(mag > 1e-8f){ w/=mag; x/=mag; y/=mag; z/=mag; } else setIdentity();
 }

 // rotate vector: v' = q * (0,v) * q^{-1}
 public Vec3 transform(Vec3 v){
     // compute q * v
     float rw = - x*v.getX() - y*v.getY() - z*v.getZ();
     float rx =  w*v.getX() + y*v.getZ() - z*v.getY();
     float ry =  w*v.getY() + z*v.getX() - x*v.getZ();
     float rz =  w*v.getZ() + x*v.getY() - y*v.getX();
     // multiply by q^-1 (conjugate for unit quat)
     float outx = rw * -x + rx * w + ry * -z - rz * -y;
     float outy = rw * -y - rx * -z + ry * w + rz * -x;
     float outz = rw * -z + rx * -y - ry * -x + rz * w;
     return new Vec3(outx, outy, outz);
 }

 // to rotation matrix (for inertia transform)
 public Mat3 toRotationMatrix(){ return Mat3.fromQuat(this); }
}
