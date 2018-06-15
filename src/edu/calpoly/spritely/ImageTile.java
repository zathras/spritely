/*
 * Copyright © 2018, Bill Foote, Cal Poly, San Luis Obispo, CA
 * 
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the “Software”), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.calpoly.spritely;


import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A tile that shows an image.  The image may be any format supported
 * by Java's ImageIO class, which should include PNG and JPEG.  Alpha
 * blending is supported:  A pixel in a tile may be semi-transparent.
 *
 *      @author         Bill Foote, http://jovial.com
 *
 * @see ImageIO#read(File)
 */
public class ImageTile implements Tile {

    private final BufferedImage image;
    private final char text;

    /**
     * Create a ImageTile with the given image, at the given size,
     * and that is represented by the given character when in text 
     * mode.  The image may contain transparent and semi-transparent pixels.
     *
     * @param  imageFile        A file containing a valid image file
     * @param  size             The size of a tile, in pixels
     * @param  text             The character representation of this tile
     *                          in text mode
     * @throws IOException if there is an error reading the image
     * @see ImageIO#read(File)
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
