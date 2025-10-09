package noise;

import java.util.Random;

/**
 * Minimal OpenSimplex (patent-free) implementation for 2D/3D/4D.
 *
 * Memory: a single short[256] permutation array and a small constant table.
 * No large gradient tables duplicated across 512 entries.
 *
 * Public-domain style usage is permitted; this code is intended to be small and
 * easy to drop into JVM projects (adapted for clarity & low-memory).
 * 
 * @author EmeJay, Stryckoeurzzz
 */
public final class OpenSimplexNoise {
    private final short[] perm = new short[256];

    private static final double STRETCH_2D = -0.211324865405187; // (1/Math.sqrt(2+1)-1)/2
    private static final double SQUISH_2D  =  0.366025403784439; // (Math.sqrt(2+1)-1)/2
    private static final double STRETCH_3D = -1.0 / 6.0;
    private static final double SQUISH_3D  = 1.0 / 3.0;
    private static final double STRETCH_4D = -0.138196601125011; // (1/Math.sqrt(4+1)-1)/4
    private static final double SQUISH_4D  = 0.309016994374947;  // (Math.sqrt(4+1)-1)/4

    private static final double NORM_2D = 47.0;
    private static final double NORM_3D = 103.0;
    private static final double NORM_4D = 30.0;

    // Gradients (unit-ish) - small set reused
    private static final byte[] GRADIENTS_2D = new byte[] {
        5,  2,    2,  5,
       -5,  2,   -2,  5,
        5, -2,    2, -5,
       -5, -2,   -2, -5
    };

    private static final byte[] GRADIENTS_3D = new byte[] {
         -11,  4,  4,   -4,  11,  4,   -4,  4,  11,
          11,  4,  4,    4,  11,  4,    4,  4,  11,
         -11, -4,  4,   -4, -11,  4,   -4, -4,  11,
          11, -4,  4,    4, -11,  4,    4, -4,  11,
         -11,  4, -4,   -4,  11, -4,   -4,  4, -11,
          11,  4, -4,    4,  11, -4,    4,  4, -11,
         -11, -4, -4,   -4, -11, -4,   -4, -4, -11,
          11, -4, -4,    4, -11, -4,    4, -4, -11
    };

    private static final byte[] GRADIENTS_4D = new byte[] {
        3,3,0,0,   -3,3,0,0,    3,-3,0,0,    -3,-3,0,0,
        3,0,3,0,   -3,0,3,0,    3,0,-3,0,    -3,0,-3,0,
        3,0,0,3,   -3,0,0,3,    3,0,0,-3,    -3,0,0,-3,
        0,3,3,0,   0,-3,3,0,    0,3,-3,0,    0,-3,-3,0,
        0,3,0,3,   0,-3,0,3,    0,3,0,-3,    0,-3,0,-3,
        0,0,3,3,   0,0,-3,3,    0,0,3,-3,    0,0,-3,-3
    };

    /**
     * Construct with given seed (deterministic).
     * Uses a small Fisher-Yates shuffle into short[256] for minimal memory.
     */
    public OpenSimplexNoise(long seed) {
        short[] source = new short[256];
        for (short i = 0; i < 256; i++) source[i] = i;
        Random rand = new Random(seed);
        for (int i = 255; i >= 0; i--) {
            int r = rand.nextInt(i + 1);
            perm[i] = source[r];
            source[r] = source[i];
        }
    }

    // -------------------------------
    // 2D noise
    // -------------------------------
    public double noise2D(double x, double y) {
        // Place input onto stretched grid.
        double stretchOffset = (x + y) * STRETCH_2D;
        double xs = x + stretchOffset;
        double ys = y + stretchOffset;

        // Floor to get grid coordinates of rhombus (stretched square) super-cell origin.
        int xsb = fastFloor(xs);
        int ysb = fastFloor(ys);

        // Compute internal coordinates
        double squishOffset = (xsb + ysb) * SQUISH_2D;
        double dx0 = x - (xsb + squishOffset);
        double dy0 = y - (ysb + squishOffset);

        // Determine which simplex we're in
        int xins = (int)(xs - xsb);
        int yins = (int)(ys - ysb);
        double inSum = xins + yins;

        double value = 0.0;

        // Contribution (0,0)
        double dx = dx0;
        double dy = dy0;
        double attn = 2 - dx*dx - dy*dy;
        if (attn > 0) {
            int permIndex = perm[(xsb & 0xff)];
            permIndex = perm[(permIndex + ysb) & 0xff];
            int gradIndex = (permIndex & 0x0f) % (GRADIENTS_2D.length / 2);
            double gx = GRADIENTS_2D[gradIndex*2];
            double gy = GRADIENTS_2D[gradIndex*2 + 1];
            double extrap = gx * dx + gy * dy;
            attn *= attn;
            value += attn * attn * extrap;
        }

        // Determine second vertex offset depending on simplex
        double dx1 = dx0 - 1 - SQUISH_2D;
        double dy1 = dy0 - 0 - SQUISH_2D;
        int xsb1 = xsb + 1;
        int ysb1 = ysb;

        attn = 2 - dx1*dx1 - dy1*dy1;
        if (attn > 0) {
            int permIndex = perm[(xsb1 & 0xff)];
            permIndex = perm[(permIndex + ysb1) & 0xff];
            int gradIndex = (permIndex & 0x0f) % (GRADIENTS_2D.length / 2);
            double gx = GRADIENTS_2D[gradIndex*2];
            double gy = GRADIENTS_2D[gradIndex*2 + 1];
            double extrap = gx * dx1 + gy * dy1;
            attn *= attn;
            value += attn * attn * extrap;
        }

        double dx2 = dx0 - 0 - SQUISH_2D;
        double dy2 = dy0 - 1 - SQUISH_2D;
        xsb1 = xsb;
        ysb1 = ysb + 1;
        attn = 2 - dx2*dx2 - dy2*dy2;
        if (attn > 0) {
            int permIndex = perm[(xsb1 & 0xff)];
            permIndex = perm[(permIndex + ysb1) & 0xff];
            int gradIndex = (permIndex & 0x0f) % (GRADIENTS_2D.length / 2);
            double gx = GRADIENTS_2D[gradIndex*2];
            double gy = GRADIENTS_2D[gradIndex*2 + 1];
            double extrap = gx * dx2 + gy * dy2;
            attn *= attn;
            value += attn * attn * extrap;
        }

        return value / NORM_2D;
    }

    // -------------------------------
    // 3D noise
    // -------------------------------
    public double noise3D(double x, double y, double z) {
        // Place input onto stretched grid.
        double stretchOffset = (x + y + z) * STRETCH_3D;
        double xs = x + stretchOffset;
        double ys = y + stretchOffset;
        double zs = z + stretchOffset;

        int xsb = fastFloor(xs);
        int ysb = fastFloor(ys);
        int zsb = fastFloor(zs);

        double squishOffset = (xsb + ysb + zsb) * SQUISH_3D;
        double dx0 = x - (xsb + squishOffset);
        double dy0 = y - (ysb + squishOffset);
        double dz0 = z - (zsb + squishOffset);

        double value = 0.0;

        // Contribution at (0,0,0)
        double attn0 = 2 - dx0*dx0 - dy0*dy0 - dz0*dz0;
        if (attn0 > 0) {
            int permIndex = perm[(xsb & 0xff)];
            permIndex = perm[(permIndex + ysb) & 0xff];
            permIndex = perm[(permIndex + zsb) & 0xff];
            int gi = (permIndex & 0x1f) % (GRADIENTS_3D.length / 3);
            double gx = GRADIENTS_3D[gi*3];
            double gy = GRADIENTS_3D[gi*3 + 1];
            double gz = GRADIENTS_3D[gi*3 + 2];
            double extrap = gx * dx0 + gy * dy0 + gz * dz0;
            attn0 *= attn0;
            value += attn0 * attn0 * extrap;
        }

        // 7 other contributions are less straightforward; for simplicity and minimal memory
        // we evaluate the 7 neighboring grid points (this is not fully optimized but keeps memory low)
        // Offsets to check:
        int[][] neighbors = {
                {1,0,0}, {0,1,0}, {0,0,1},
                {1,1,0}, {1,0,1}, {0,1,1},
                {1,1,1}
        };
        for (int[] n : neighbors) {
            int xi = xsb + n[0];
            int yi = ysb + n[1];
            int zi = zsb + n[2];
            double dx = dx0 - n[0] - SQUISH_3D;
            double dy = dy0 - n[1] - SQUISH_3D;
            double dz = dz0 - n[2] - SQUISH_3D;
            double attn = 2 - dx*dx - dy*dy - dz*dz;
            if (attn > 0) {
                int permIndex = perm[(xi & 0xff)];
                permIndex = perm[(permIndex + yi) & 0xff];
                permIndex = perm[(permIndex + zi) & 0xff];
                int gi = (permIndex & 0x1f) % (GRADIENTS_3D.length / 3);
                double gx = GRADIENTS_3D[gi*3];
                double gy = GRADIENTS_3D[gi*3 + 1];
                double gz = GRADIENTS_3D[gi*3 + 2];
                double extrap = gx * dx + gy * dy + gz * dz;
                attn *= attn;
                value += attn * attn * extrap;
            }
        }

        return value / NORM_3D;
    }

    // -------------------------------
    // 4D noise (basic, compact)
    // -------------------------------
    public double noise4D(double x, double y, double z, double w) {
        // Stretch input
        double stretchOffset = (x + y + z + w) * STRETCH_4D;
        double xs = x + stretchOffset;
        double ys = y + stretchOffset;
        double zs = z + stretchOffset;
        double ws = w + stretchOffset;

        int xsb = fastFloor(xs);
        int ysb = fastFloor(ys);
        int zsb = fastFloor(zs);
        int wsb = fastFloor(ws);

        double squishOffset = (xsb + ysb + zsb + wsb) * SQUISH_4D;
        double dx0 = x - (xsb + squishOffset);
        double dy0 = y - (ysb + squishOffset);
        double dz0 = z - (zsb + squishOffset);
        double dw0 = w - (wsb + squishOffset);

        double value = 0.0;

        // Evaluate the 16 nearest lattice points (compact but not hyper-optimized)
        int[][] offs = {
            {0,0,0,0},{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1},
            {1,1,0,0},{1,0,1,0},{1,0,0,1},{0,1,1,0},{0,1,0,1},
            {0,0,1,1},{1,1,1,0},{1,1,0,1},{1,0,1,1},{0,1,1,1},{1,1,1,1}
        };

        for (int[] o : offs) {
            int xi = xsb + o[0];
            int yi = ysb + o[1];
            int zi = zsb + o[2];
            int wi = wsb + o[3];

            double dx = dx0 - o[0] - SQUISH_4D;
            double dy = dy0 - o[1] - SQUISH_4D;
            double dz = dz0 - o[2] - SQUISH_4D;
            double dw = dw0 - o[3] - SQUISH_4D;
            double attn = 2 - dx*dx - dy*dy - dz*dz - dw*dw;
            if (attn > 0) {
                int permIndex = perm[(xi & 0xff)];
                permIndex = perm[(permIndex + yi) & 0xff];
                permIndex = perm[(permIndex + zi) & 0xff];
                permIndex = perm[(permIndex + wi) & 0xff];
                int gi = (permIndex & 0x1f) % (GRADIENTS_4D.length / 4);
                double gx = GRADIENTS_4D[gi*4];
                double gy = GRADIENTS_4D[gi*4 + 1];
                double gz = GRADIENTS_4D[gi*4 + 2];
                double gw = GRADIENTS_4D[gi*4 + 3];
                double extrap = gx * dx + gy * dy + gz * dz + gw * dw;
                attn *= attn;
                value += attn * attn * extrap;
            }
        }

        return value / NORM_4D;
    }

    // -------------
    // Utility
    // -------------
    private static int fastFloor(double x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }
}

/**
OpenSimplexNoise noise = new OpenSimplexNoise(12345L);
BufferedImage img = NoiseTexture.generateGrayscale(noise, 512, 512, 0.01, 4, 2.0, 0.5, 0);
JLabel label = new JLabel(new ImageIcon(img));
JFrame f = new JFrame("Noise");
f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
f.add(label);
f.pack();
f.setVisible(true);
 */

