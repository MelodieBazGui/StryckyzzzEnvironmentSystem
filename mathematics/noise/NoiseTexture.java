package noise;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

/**
 * Helper: create grayscale/color textures from OpenSimplexNoise for Swing (BufferedImage)
 * and upload a BufferedImage to JOGL GL2 as an RGBA texture.
 *
 * Minimal allocations in hot loops: reuses a single BufferedImage and raw int[] if needed.
 */
public final class NoiseTexture {

    private NoiseTexture() {}

    /**
     * Generate a grayscale BufferedImage (TYPE_INT_ARGB) using the noise generator.
     * - width/height: pixel size
     * - scale: noise frequency scale (e.g. 0.01)
     * - octaves: fractal sum of octaves (1 = single octave)
     * - seedOffset: add to x/y to vary seeds between textures
     */
    public static BufferedImage generateGrayscale(OpenSimplexNoise noise, int width, int height, double scale, int octaves, double lacunarity, double gain, long seedOffset) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            double py = (y + seedOffset) * scale;
            for (int x = 0; x < width; x++) {
                double px = (x + seedOffset) * scale;
                double amp = 1.0;
                double freq = 1.0;
                double sum = 0.0;
                for (int o = 0; o < octaves; o++) {
                    double v = noise.noise2D(px * freq, py * freq);
                    sum += v * amp;
                    amp *= gain;
                    freq *= lacunarity;
                }
                // Map from approx [-1,1] to [0,255]
                int c = (int) Math.round((sum * 0.5 + 0.5) * 255.0);
                if (c < 0) c = 0;
                if (c > 255) c = 255;
                int argb = (0xFF << 24) | (c << 16) | (c << 8) | c;
                pixels[y * width + x] = argb;
            }
        }
        img.setRGB(0, 0, width, height, pixels, 0, width);
        return img;
    }

    /**
     * Colorize a grayscale image with a simple palette (fast). If you want
     * more control, pass your own color mapping.
     */
    public static BufferedImage colorize(BufferedImage gray, int[] palette) {
        int w = gray.getWidth(), h = gray.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[w * h];
        gray.getRGB(0, 0, w, h, pixels, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int g = pixels[i] & 0xFF;
            int idx = (g * (palette.length - 1)) / 255;
            pixels[i] = (0xFF << 24) | (palette[idx] & 0x00FFFFFF);
        }
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }

    /**
     * Upload a BufferedImage as an RGBA texture to JOGL GL2. This is a minimal helper:
     * - binds to provided textureId (must be generated via glGenTextures)
     * - sets basic parameters and uploads pixels
     *
     * Note: JOGL requires the proper GL context when calling this method.
     */
    public static void uploadToGL2(GL2 gl, int textureId, BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Convert BufferedImage to RGBA byte buffer (row-major, top-down)
        byte[] rgba = new byte[width * height * 4];
        int idx = 0;
        int[] pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            rgba[idx++] = (byte) ((p >> 16) & 0xFF); // R
            rgba[idx++] = (byte) ((p >> 8) & 0xFF);  // G
            rgba[idx++] = (byte) (p & 0xFF);         // B
            rgba[idx++] = (byte) ((p >> 24) & 0xFF); // A
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(rgba.length).order(ByteOrder.nativeOrder());
        bb.put(rgba);
        bb.rewind();

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        // Set texture params (you can change to LINEAR / MIPMAP as needed)
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

        // Upload (GL_RGBA, GL_UNSIGNED_BYTE)
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}

/**
In your GLEventListener.init(GLAutoDrawable d) or where context is current:
GL2 gl = d.getGL().getGL2();
int[] tex = new int[1];
gl.glGenTextures(1, tex, 0);
BufferedImage img = NoiseTexture.generateGrayscale(noise, 256, 256, 0.02, 3, 2.0, 0.5, 0);
NoiseTexture.uploadToGL2(gl, tex[0], img);
*/