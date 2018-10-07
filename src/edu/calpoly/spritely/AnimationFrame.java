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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

/**
 * Representation of one frame of animation in a SpriteWindow.  
 * An AnimationFrame is
 * given to client code by a SpriteWindow for each frame of animation.
 * The client works by adding tiles to a grid maintained by the
 * AnimationFrame.
 *
 *      @author         Bill Foote, http://jovial.com
 *
 * @see SpriteWindow#waitForNextFrame()
 * @see SpriteWindow#showNextFrame()
 */
public final class AnimationFrame {

    private final Object grid[][];     // Really array of List<Tile>.  Sigh.
    private final Size gridSize;
    private final Size tileSize;
    private boolean shown = false;

    AnimationFrame(Size gridSize, Size tileSize) {
        grid = new Object[gridSize.height][gridSize.width];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = new ArrayList<Tile>(4);
		// An underlying array of 4 is small enough to be
		// negligible, and hopefully large enough for a pretty
		// typical case of up to 4 overlapping tiles.
            }
        }
        this.tileSize = tileSize;
	this.gridSize = gridSize;
    }

    /**
     * Add the given tile to the grid being shown in this animation frame.
     * Each grid square may contain any number of overlapping tiles.  The
     * first tile added is placed on the bottom, and subsequent tiles are
     * drawn on top of that, in order.  In text mode, only the topmost
     * tile can be seen.
     *
     * @param   x  The column of the tile, 0-based, counting from the left
     * @param   y  The row of the tile, 0-based counting from the top
     * @param   tile  The tile to show at x,y.
     *
     * @throws IllegalArgumentException if x, y are out of range.
     * @throws IllegalStateException    if this frame has been shown
     * @throws NullPointerException	if tile is null
     * @see SpriteWindow#waitForNextFrame()
     * @see SpriteWindow#showNextFrame()
     */
    public void addTile(int x, int y, Tile tile) {
        if (shown) {
            throw new IllegalStateException();
        }
        if (x < 0 || y < 0 && y >= grid.length || x >= grid[0].length) {
            throw new IllegalArgumentException();
        }
	if (tile == null) {
	    throw new NullPointerException();
	}
        @SuppressWarnings("unchecked")
        List<Tile> cell = (List<Tile>) grid[y][x];	// sigh
        cell.add(tile);
    }

    Rectangle calculateDamage(AnimationFrame lastFrame) {
	Rectangle damage = new Rectangle(0, 0, -1, -1);
	assert !shown;
        shown = true;
	if (lastFrame == null) {
	    damage.add(0, 0);
	    damage.add(gridSize.width, gridSize.height);
	    // java.awt.Rectangle is a little counter-intuitive, in that
	    // damage.contains(w, h) would return false at this point:
	    assert damage.width == gridSize.width;
	    assert damage.height == gridSize.height;
	    return damage;
	}
	int minX = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
		if (x < minX || x > maxX || y < minY || y > maxY) {
		    @SuppressWarnings("unchecked")
		    List<Tile> cell = (List<Tile>) grid[y][x];
		    @SuppressWarnings("unchecked")
		    List<Tile> old = (List<Tile>) lastFrame.grid[y][x];
		    if (!(cell.equals(old))) {	// cf. Tile class documentation
			minX = Math.min(x, minX);
			maxX = Math.max(x, maxX);
			minY = Math.min(y, minY);
			maxY = Math.max(y, maxY);
			x = maxX;
		    }
		}
	    }
	}
	if (minX != Integer.MAX_VALUE) {	// Something added
	    damage.add(minX, minY);
	    damage.add(maxX + 1, maxY + 1);
	}
	return damage;
    }

    void paint(BufferedImage buffer, BufferedImage last, Rectangle damage) {
	assert shown;
	Graphics2D g = buffer.createGraphics();
	g.setColor(Color.black);
	if (damage.contains(0, 0) 
	     && damage.contains(gridSize.width-1, gridSize.height-1))
	{
	    g.fillRect(0, 0, tileSize.width*gridSize.width, 
			     tileSize.height*gridSize.height);
	} else {
	    assert last != null;
	    // Blitting one big image is faster than blitting a bunch of
	    // small images:
	    g.drawImage(last, 0, 0, null);
	    g.fillRect(damage.x * tileSize.width,
	               damage.y * tileSize.height,
		       damage.width * tileSize.width,
		       damage.height * tileSize.height);
	}
        for (int y = damage.y; y < damage.y + damage.height; y++) {
            for (int x = damage.x; x < damage.x + damage.width; x++) {
                @SuppressWarnings("unchecked")
                List<Tile> cell = (List<Tile>) grid[y][x];
                if (!cell.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create(
                            x * tileSize.width, y * tileSize.height,
                            tileSize.width, tileSize.height);
                    for (Tile t : cell) {
                        t.paint(g2, tileSize);
                    }
		    g2.dispose();
                }
            }
        }
	g.dispose();
    }

    void print() {
        shown = true;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                @SuppressWarnings("unchecked")
                List<Tile> cell = (List<Tile>) grid[y][x];
                if (cell.isEmpty()) {
                    System.out.print("  ");
                } else {
                    System.out.print(' ');
                    Tile t = cell.get(cell.size() - 1);
                    System.out.print(t.getText());
                }
            }
            System.out.println("        ");
        }
    }

    //
    // Grading support
    //
    Object[][] gradingGetGrid() {
	return grid;
    }
}
