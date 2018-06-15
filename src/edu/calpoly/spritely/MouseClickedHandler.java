
package edu.calpoly.spritely;

import java.awt.event.MouseEvent;
/**
 * A functional interface for receiving a notification when the mouse
 * is clicked.
 *
 * @see SpriteWindow#setMouseClickedHandler(MouseClickedHandler)
 */

@FunctionalInterface
public interface MouseClickedHandler {

    void mouseClicked(int gridX, int gridY);

}
