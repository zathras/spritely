
package edu.calpoly.spritely;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;

public class AnimationFrame {

    private final Object grid[][];     // Really array of List<Tile>.  Sigh.
    private final Size tileSize;

    AnimationFrame(Size gridSize, Size tileSize, AnimationFrame lastFrame) {
        grid = new Object[gridSize.height][gridSize.width];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = new ArrayList<Tile>(4);
            }
        }
        this.tileSize = tileSize;
    }

    /**
     * Add the given tile to the grid being shown in this animation frame.
     * Each grid square may contain any number of overlapping tiles.
     *
     * @throws IllegalArgumentException if x, y are out of range.
     * @throws IllegalStateException    if 
     * @see SpriteWindow#waitForNextFrame()
     * @see SpriteWindow#showNextFrame()
     */
    public void addTile(int x, int y, Tile tile) {
        @SuppressWarnings("unchecked")
        List<Tile> cell = (List<Tile>) grid[y][x];
        cell.add(tile);
    }

    void paint(Graphics2D g) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                @SuppressWarnings("unchecked")
                List<Tile> cell = (List<Tile>) grid[y][x];
                if (!cell.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create(
                            x * tileSize.width, y * tileSize.height,
                            tileSize.width, tileSize.height);
                    for (Tile t : cell) {
                        t.paint(g2, tileSize);
                    }
                }
            }
        }
    }

    void print() {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                @SuppressWarnings("unchecked")
                List<Tile> cell = (List<Tile>) grid[y][x];
                if (cell.isEmpty()) {
                    System.out.print("  ");
                } else {
                    System.out.print(' ');
                    Tile t = cell.get(cell.size() - 1);
                    System.out.print(t.getPrinted());
                }
            }
            System.out.println("        ");
        }
    }
}
