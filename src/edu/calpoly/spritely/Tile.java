
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
 * @see SolidColorTile
 * @see ImageTile
 */
public interface Tile {

    /**
     * Paints this tile into the given graphics context g.  g will be
     * set up so that tiles that are underneath this tile will show
     * through if this tile has transparent, or semi-transparent pixels.
     *
     * @see java.awt.AlphaComposite#SrcOver
     */
    public void paint(Graphics2D g, Size size);

    /**
     * Gives the character representation of this tile.
     *
     * @return the character representation of this tile in text mode.
     */
    public char getPrinted();

}
