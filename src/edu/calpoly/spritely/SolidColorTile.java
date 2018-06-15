
package edu.calpoly.spritely;


import java.awt.Graphics2D;
import java.awt.Color;

public class SolidColorTile implements Tile {

    private final Color color;
    private final char text;

    /**
     * Create a SolidColorTile with the given color, and that is represented
     * by the given character when in text mode.  The color may be 
     * semi-transparent.
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
    public char getPrinted() {
        return text;
    }
}
