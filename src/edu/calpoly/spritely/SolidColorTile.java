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

/**
 * A tile that is represented graphically as a solid color.  The color
 * may be semi-transparent.
 *
 *      @author         Bill Foote, http://jovial.com
 */
public class SolidColorTile implements Tile {

    private final Color color;
    private final char text;

    /**
     * Create a SolidColorTile with the given color, and that is represented
     * by the given character when in text mode.  The color may be 
     * semi-transparent.
     *
     * @param color     The color of the tile
     * @param  text             The character representation of this tile
     *                          in text mode
     */
    public SolidColorTile(Color color, char text) {
        this.color = color;
        this.text = text;
    }

    @Override
    public void paint(Graphics2D g, Size size) {
        g.setColor(color);
        g.fillRect(0, 0, size.width, size.height);
    }

    @Override
    public char getText() {
        return text;
    }
}
