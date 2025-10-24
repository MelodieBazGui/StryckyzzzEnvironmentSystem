package math;

/**
 * Fast 4x4 matrix (row-major) for transforms & camera.
 * Provides TRS construction, perspective, lookAt, and conversions
 * to column-major for glUniformMatrix4fv(..., transpose=false).
 */
public final class Mat4 {

    // Row-major: m[row*4 + col]
    private final float[] m = new float[16];

    // ---------- ctors ----------
    public Mat4() { setIdentity(); }

    public Mat4(float[] values) {
        if (values.length != 16) throw new IllegalArgumentException("Mat4 needs 16 values");
        System.arraycopy(values, 0, m, 0, 16);
    }

    public static Mat4 identity() { return new Mat4().setIdentity(); }

    public Mat4 setIdentity() {
        for (int i = 0; i < 16; i++) m[i] = 0f;
        m[0]=1; m[5]=1; m[10]=1; m[15]=1;
        return this;
    }

    // ---------- basic access ----------
    public float get(int row, int col) { return m[row*4 + col]; }
    public void set(int row, int col, float v){ m[row*4 + col] = v; }
    public float[] rawRowMajor() { return m; }

    /** Returns a column-major copy for OpenGL (transpose=false). */
    public float[] toColumnMajorArray() {
        return new float[] {
            m[0], m[4], m[8],  m[12],
            m[1], m[5], m[9],  m[13],
            m[2], m[6], m[10], m[14],
            m[3], m[7], m[11], m[15]
        };
    }

    // ---------- ops ----------
    /** this * other */
    public Mat4 mul(Mat4 o) {
        float[] a = this.m, b = o.m;
        float[] r = new float[16];

        r[0] = a[0]*b[0] + a[1]*b[4] + a[2]*b[8]  + a[3]*b[12];
        r[1] = a[0]*b[1] + a[1]*b[5] + a[2]*b[9]  + a[3]*b[13];
        r[2] = a[0]*b[2] + a[1]*b[6] + a[2]*b[10] + a[3]*b[14];
        r[3] = a[0]*b[3] + a[1]*b[7] + a[2]*b[11] + a[3]*b[15];

        r[4] = a[4]*b[0] + a[5]*b[4] + a[6]*b[8]  + a[7]*b[12];
        r[5] = a[4]*b[1] + a[5]*b[5] + a[6]*b[9]  + a[7]*b[13];
        r[6] = a[4]*b[2] + a[5]*b[6] + a[6]*b[10] + a[7]*b[14];
        r[7] = a[4]*b[3] + a[5]*b[7] + a[6]*b[11] + a[7]*b[15];

        r[8]  = a[8]*b[0] + a[9]*b[4] + a[10]*b[8]  + a[11]*b[12];
        r[9]  = a[8]*b[1] + a[9]*b[5] + a[10]*b[9]  + a[11]*b[13];
        r[10] = a[8]*b[2] + a[9]*b[6] + a[10]*b[10] + a[11]*b[14];
        r[11] = a[8]*b[3] + a[9]*b[7] + a[10]*b[11] + a[11]*b[15];

        r[12] = a[12]*b[0] + a[13]*b[4] + a[14]*b[8]  + a[15]*b[12];
        r[13] = a[12]*b[1] + a[13]*b[5] + a[14]*b[9]  + a[15]*b[13];
        r[14] = a[12]*b[2] + a[13]*b[6] + a[14]*b[10] + a[15]*b[14];
        r[15] = a[12]*b[3] + a[13]*b[7] + a[14]*b[11] + a[15]*b[15];

        return new Mat4(r);
    }

    // ---------- transforms ----------
    public static Mat4 fromTranslation(Vec3 t) {
        Mat4 M = Mat4.identity();
        M.m[12] = t.getX();
        M.m[13] = t.getY();
        M.m[14] = t.getZ();
        return M;
    }

    public static Mat4 fromScale(Vec3 s) {
        Mat4 M = new Mat4();
        M.m[0] = s.getX();
        M.m[5] = s.getY();
        M.m[10] = s.getZ();
        M.m[15] = 1f;
        return M;
    }

    /** Rotation (row-major) from quaternion. */
    public static Mat4 fromQuat(Quat q) {
        // normalized assumed (your Quat.normalize() handles it)
        float w = q.getW(), x = q.getX(), y = q.getY(), z = q.getZ();
        float xx = x*x, yy = y*y, zz = z*z;
        float xy = x*y, xz = x*z, yz = y*z;
        float wx = w*x, wy = w*y, wz = w*z;

        Mat4 M = Mat4.identity();
        M.m[0] = 1 - 2*(yy + zz);
        M.m[1] = 2*(xy - wz);
        M.m[2] = 2*(xz + wy);

        M.m[4] = 2*(xy + wz);
        M.m[5] = 1 - 2*(xx + zz);
        M.m[6] = 2*(yz - wx);

        M.m[8]  = 2*(xz - wy);
        M.m[9]  = 2*(yz + wx);
        M.m[10] = 1 - 2*(xx + yy);
        return M;
    }

    /** TRS = Translation * Rotation * Scale (row-major, column vector on the right). */
    public static Mat4 fromTRS(Vec3 t, Quat r, Vec3 s) {
        Mat4 R = fromQuat(r);
        // bake scale into rotation 3x3:
        R.m[0] *= s.getX(); R.m[1] *= s.getY(); R.m[2]  *= s.getZ();
        R.m[4] *= s.getX(); R.m[5] *= s.getY(); R.m[6]  *= s.getZ();
        R.m[8] *= s.getX(); R.m[9] *= s.getY(); R.m[10] *= s.getZ();
        // translation
        R.m[12] = t.getX();
        R.m[13] = t.getY();
        R.m[14] = t.getZ();
        return R;
    }

    // ---------- cameras ----------
    public static Mat4 perspective(float fovYRadians, float aspect, float near, float far) {
        float f = 1f / (float)Math.tan(fovYRadians * 0.5f);
        float rangeInv = 1f / (near - far);

        float[] r = new float[16];
        r[0] = f / aspect; r[1]=0; r[2]=0;                        r[3]=0;
        r[4] = 0;           r[5]=f; r[6]=0;                        r[7]=0;
        r[8] = 0;           r[9]=0; r[10]=(far + near)*rangeInv;   r[11]=-1;
        r[12]=0;            r[13]=0; r[14]=2f*far*near*rangeInv;   r[15]=0;
        return new Mat4(r);
    }

    public static Mat4 lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        // forward (z-), right (x), up' (y), classic right-handed
        Vec3 f = new Vec3(target.getX()-eye.getX(), target.getY()-eye.getY(), target.getZ()-eye.getZ()).normalize();
        Vec3 s = Vec3.cross(f, up).normalize();     // right
        Vec3 u = Vec3.cross(s, f);                  // corrected up

        Mat4 M = Mat4.identity();
        // rotation part (rows are basis vectors)
        M.m[0] =  s.getX(); M.m[1] =  s.getY(); M.m[2] =  s.getZ();
        M.m[4] =  u.getX(); M.m[5] =  u.getY(); M.m[6] =  u.getZ();
        M.m[8] = -f.getX(); M.m[9] = -f.getY(); M.m[10] = -f.getZ();
        // translation
        M.m[12] = - (s.getX()*eye.getX() + s.getY()*eye.getY() + s.getZ()*eye.getZ());
        M.m[13] = - (u.getX()*eye.getX() + u.getY()*eye.getY() + u.getZ()*eye.getZ());
        M.m[14] =   (f.getX()*eye.getX() + f.getY()*eye.getY() + f.getZ()*eye.getZ()) * -1f;
        return M;
    }
}
