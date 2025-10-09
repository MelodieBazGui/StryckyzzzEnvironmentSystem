package mathTest;

import org.junit.jupiter.api.Test;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

import noise.OpenSimplexNoise;
import noise.NoiseTexture;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoiseTexture helper utilities.
 * 
 * - Validates grayscale noise generation (range, variation, determinism)
 * - Validates colorization palette mapping
 */
public class NoiseTextureTest {

    @Test
    void testGenerateGrayscaleRangeAndVariation() {
        OpenSimplexNoise noise = new OpenSimplexNoise(1234L);
        BufferedImage img = NoiseTexture.generateGrayscale(
                noise, 64, 64, 0.05, 3, 2.0, 0.5, 0);

        assertNotNull(img, "Generated image should not be null");
        assertEquals(64, img.getWidth());
        assertEquals(64, img.getHeight());

        // Check pixel values are within grayscale range
        int[] rgb = img.getRGB(0, 0, 64, 64, null, 0, 64);
        boolean variation = false;
        int first = rgb[0];
        for (int c : rgb) {
            int gray = c & 0xFF;
            assertTrue(gray >= 0 && gray <= 255, "Gray value out of range");
            if (c != first) variation = true;
        }
        assertTrue(variation, "Noise texture should have variation");
    }

    @Test
    void testDeterministicGeneration() {
        OpenSimplexNoise n1 = new OpenSimplexNoise(999L);
        OpenSimplexNoise n2 = new OpenSimplexNoise(999L);

        BufferedImage img1 = NoiseTexture.generateGrayscale(n1, 32, 32, 0.05, 2, 2.0, 0.5, 0);
        BufferedImage img2 = NoiseTexture.generateGrayscale(n2, 32, 32, 0.05, 2, 2.0, 0.5, 0);

        int[] p1 = img1.getRGB(0, 0, 32, 32, null, 0, 32);
        int[] p2 = img2.getRGB(0, 0, 32, 32, null, 0, 32);
        assertArrayEquals(p1, p2, "Same seed → same noise texture");
    }

    @Test
    void testColorizeMapping() {
        OpenSimplexNoise noise = new OpenSimplexNoise(111L);
        BufferedImage gray = NoiseTexture.generateGrayscale(noise, 8, 8, 0.1, 1, 2.0, 0.5, 0);

        int[] palette = {
                0x000000, // black
                0xFF0000, // red
                0x00FF00, // green
                0x0000FF, // blue
                0xFFFFFF  // white
        };

        BufferedImage colorized = NoiseTexture.colorize(gray, palette);
        assertNotNull(colorized);
        assertEquals(gray.getWidth(), colorized.getWidth());
        assertEquals(gray.getHeight(), colorized.getHeight());

        // Verify output colors come from palette
        int[] px = colorized.getRGB(0, 0, 8, 8, null, 0, 8);
        boolean foundPaletteColor = false;
        for (int c : px) {
            int rgb = c & 0xFFFFFF;
            boolean inPalette = false;
            for (int p : palette) {
                if (rgb == p) {
                    inPalette = true;
                    break;
                }
            }
            assertTrue(inPalette, "Pixel color not in palette");
            if (rgb != palette[0]) foundPaletteColor = true;
        }
        assertTrue(foundPaletteColor, "Colorized image should vary in color");
    }
    
    /**@Test
    void testUploadToGL2WorksWithRealGLContext() {
    	GLProfile profile = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setPBuffer(true); // offscreen rendering
        GLDrawableFactory factory = GLDrawableFactory.getFactory(profile);

        // Create a 1x1 invisible drawable
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null, caps, null, 1, 1);
        drawable.display();
        drawable.getContext().makeCurrent();
        
        GL2 gl = drawable.getGL().getGL2();
        assertNotNull(gl, "GL2 context should be available");

        OpenSimplexNoise noise = new OpenSimplexNoise(1234L);
        BufferedImage img = NoiseTexture.generateGrayscale(noise, 8, 8, 0.05, 2, 2.0, 0.5, 0);

        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        assertDoesNotThrow(() ->
                NoiseTexture.uploadToGL2(gl, tex[0], img),
                "uploadToGL2 should not throw with a real GL2 context"
        );
    }
    */
}
