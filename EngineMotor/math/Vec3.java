package math;

public class Vec3 {
	
    private float x, y, z;
    
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}
    
    public Vec3() { this(0,0,0); }
    
    public Vec3(float x, float y, float z) {
    	this.x=x; this.y=y; this.z=z; 
    	}
    
    public Vec3 set(Vec3 o){
    	x=o.x; y=o.y; z=o.z; return this; 
    	}
    
    public Vec3 add(Vec3 o){ 
    	x+=o.x; y+=o.y; z+=o.z; return this; 
    	}
    
    public Vec3 sub(Vec3 o){ 
    	x-=o.x; y-=o.y; z-=o.z; return this; 
    	}
    
    public Vec3 scl(float s){ 
    	x*=s; y*=s; z*=s; return this; 
    	}
    
    public Vec3 cpy(){ 
    	return new Vec3(x,y,z); }
    
    public static Vec3 add(Vec3 a, Vec3 b){ 
    	return new Vec3(a.x+b.x, a.y+b.y, a.z+b.z); 
    	}
    
    public static Vec3 sub(Vec3 a, Vec3 b){ 
    	return new Vec3(a.x-b.x, a.y-b.y, a.z-b.z); 
    	}
    
    public static Vec3 scl(Vec3 a, float s){ 
    	return new Vec3(a.x*s, a.y*s, a.z*s); 
    	}
    
    public float dot(Vec3 o){ 
    	return x*o.x + y*o.y + z*o.z; 
    	}
    
    public Vec3 cross(Vec3 o){ 
    	return new Vec3(y*o.z - z*o.y, z*o.x - x*o.z, x*o.y - y*o.x); 
    	}
    
    public float len2(){ 
    	return x*x + y*y + z*z; 
    	}
    
    public float len(){ 
    	return (float)Math.sqrt(len2()); 
    	}
    
    public Vec3 normalize(){
    	float l = len(); if(l>1e-8f) scl(1.0f/l); return this; 
    	}
    
    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f)", x, y, z);
    }

}
