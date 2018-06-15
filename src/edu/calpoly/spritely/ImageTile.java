
package edu.calpoly.spritely;


import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageTile implements Tile {

    private final BufferedImage image;
    private final char text;

    /**
     * Create a ImageTile with the given image, at the given size,
     * and that is represented
     * by the given character when in text mode.  The image may contain
     * transparent and semi-transparent pixels.
     *
     * @throws IOException 
     */
    public ImageTile(File imageFile, Size size, char text) 
            throws IOException 
    {
        BufferedImage im = ImageIO.read(imageFile);
        if (im == null) {
            throw new IOException("Unable to read image in " + imageFile);
        }
        if (im.getWidth() != size.width || im.getHeight() != size.height) {
            double scaleX = ((double) size.width) / im.getWidth();
            double scaleY = ((double) size.height) / im.getHeight();
            BufferedImage after = 
                    new BufferedImage(size.width, size.height, im.getType());
            AffineTransform scale = 
                AffineTransform.getScaleInstance(scaleX, scaleY);
            AffineTransformOp scaleOp = 
                    new AffineTransformOp(scale, 
                                          AffineTransformOp.TYPE_BILINEAR);
            scaleOp.filter(im, after);
            im = after;
        }
        this.image = im;
        this.text = text;
    }

    @Override
    public void paint(Graphics2D g, Size size) {
        g.drawImage(image, 0, 0, null);
    }

    @Override
    public char getPrinted() {
        return text;
    }
}
