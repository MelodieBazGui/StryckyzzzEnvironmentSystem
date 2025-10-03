package Handlers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import basicHandler.DataHandler;

public class IMGHandler extends DataHandler {

	private final String IMGLOC = super.DATALOC + "img" + File.separator;

    /**
     * Load a PNG image from the filesystem.
     *
     * @param filePath Path to the PNG file (relative or absolute)
     * @return BufferedImage or null if loading fails
     */
    public BufferedImage loadImage(String type, String imgName) {
    	String ressourcePath = getRessourcePath(type, imgName);
        try {
        	BufferedImage image = ImageIO.read(new File(ressourcePath));
        	ressourcePath = null;
            return image;
        } catch (IOException e) {
            System.err.println("[ERROR][PNGHandler]: Failed to load image from path: " + ressourcePath);
        	ressourcePath = null;
            return null;
        }
    }

    /**
     * Load a PNG image embedded in the JAR/resources folder.
     *
     * @param resourcePath Resource path, e.g., "/icons/icon.png"
     * @return BufferedImage or null
     */
    public BufferedImage loadFromResources(String pathName) {
        try {
            URL url = IMGHandler.class.getResource(pathName);
            if (url == null) {
                System.err.println("[ERROR][PNGHandler]: Resource not found: " + pathName);
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("[ERROR][PNGHandler]: Failed to load resource image: " + pathName);
            return null;
        }

    }

    /**
     * Converts BufferedImage to ImageIcon for Swing components.
     *
     * @param image BufferedImage instance
     * @return ImageIcon or null
     */
    public static ImageIcon toIcon(BufferedImage image) {
        return image != null ? new ImageIcon(image) : null;
    }

    private String getRessourcePath(String type, String imgName) {
			return IMGLOC + type + File.separator + imgName + "." + type;
    }
}