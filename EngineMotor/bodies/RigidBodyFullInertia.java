package bodies;

import engine.IdGenerator;
import math.Mat3;
import math.Quat;
import math.Vec3;

public final class RigidBodyFullInertia {
    private final int id;
    private float invMass; // private
    private final Vec3 position = new Vec3();
    private final Quat orientation = new Quat();
    private final Vec3 velocity = new Vec3();
    private final Vec3 omega = new Vec3();

    private final Shape shape;

    private Mat3 inertiaBody;
    private Mat3 inertiaBodyInv;
    private Mat3 inertiaWorldInv;

    public RigidBodyFullInertia(Shape shape, Vec3 pos, Quat ori, float mass) {
        this.id = IdGenerator.nextId();
        this.shape = shape;
        this.position.set(pos);
        this.orientation.set(ori);

        if (mass <= 0f) {
            this.invMass = 0f;
            this.inertiaBody = Mat3.identity();     // or Mat3.zero()
            this.inertiaBodyInv = Mat3.identity();
        } else {
            this.invMass = 1.0f / mass;
            this.inertiaBody = shape.computeInertia(mass);
            this.inertiaBodyInv = (inertiaBody != null) ? inertiaBody.inverse() : Mat3.identity();
        }

        updateInertiaWorld();
    }

    public RigidBodyFullInertia(int id, float mass, Vec3 pos, Quat ori, Shape shape, Mat3 inertiaBody){
        this.id = id;
        this.shape = shape;
        this.position.set(pos);
        this.orientation.setIdentity();
        if(ori!=null){ new Quat(); }
        if(mass <= 0f){ this.invMass = 0f; } else {
			this.invMass = 1.0f/mass;
		}
        this.inertiaBody = inertiaBody;
        this.inertiaBodyInv = (inertiaBody != null) ? inertiaBody.inverse() : Mat3.identity();

        updateInertiaWorld();
    }

    public int getId(){ return id; }
    public float getInvMass(){ return invMass; }
    public Vec3 getPosition(){ return position.cpy(); }
    public Quat getOrientation(){ return orientation; }
    public Vec3 getVelocity(){ return velocity.cpy(); }
    public Vec3 getOmega(){ return omega.cpy(); }
    public Shape getShape(){ return shape; }

    // Apply impulse (linear + angular), using full inertia world inverse
    public void applyImpulse(Vec3 impulse, Vec3 rel){
        if(invMass == 0f) {
			return;
		}
        // linear
        velocity.add(Vec3.scl(impulse, invMass));
        // angular: Δω = I_world_inv * (r × J)
        Vec3 rCrossJ = rel.cross(impulse);
        Vec3 deltaOmega = inertiaWorldInv.mul(rCrossJ);
        omega.add(deltaOmega);
    }

    // Integrate (semi-implicit Euler)
    public void integrate(float dt, Vec3 gravity){
        if(invMass == 0f) {
			return;
		}
        velocity.add(Vec3.scl(gravity, dt));
        position.add(Vec3.scl(velocity, dt));
        orientation.integrateAngular(omega, dt);
        updateInertiaWorld();
    }

    // recompute I_world_inv = R * I_body_inv * R^T
    public void updateInertiaWorld(){
        Mat3 R = orientation.toRotationMatrix();
        inertiaWorldInv = R.mul(inertiaBodyInv).mul(R.transpose());
    }

    public Mat3 getInertiaBody(){ return inertiaBody; }
    public Mat3 getInertiaBodyInv(){ return inertiaBodyInv; }
    public Mat3 getInertiaWorldInv(){ return inertiaWorldInv; }
}
