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

/**
 * A tile that can be displayed in a SpriteWindow.  A tile must be
 * <em>immutable</em>, that is, a given Tile instance shall display
 * the identical contents each time it is displayed.  Tiles shall not
 * override <code>equals()</code> or <code>hashCode()</code>; instance
 * identity is used to determine if the tiles displayed in a given grid
 * square have changed from frame to frame.
 *
 *      @author         Bill Foote, http://jovial.com
 *
 * @see SolidColorTile
 * @see ImageTile
 */
public interface Tile {

    /**
     * Paints this tile into the given graphics context g.  g will be
     * set up so that tiles that are underneath this tile will show
     * through if this tile has transparent, or semi-transparent pixels.
     *
     * @param g         The Java graphics context to draw into, with an
     *                  appropriate translation and crop already set.
     * @param size      The size of this tile.  For a given window, the
     *                  value of size will never change.
     * @see java.awt.AlphaComposite#SrcOver
     */
    public void paint(Graphics2D g, Size size);

    /**
     * Gives the text representation of this tile.
     *
     * @return the character representation of this tile in text mode.
     */
    public char getText();

}
