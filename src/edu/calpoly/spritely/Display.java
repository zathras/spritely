
package edu.calpoly.spritely;

/**
 * Something that displays either sprites, or graphics-based animation.
 */
public interface Display {
    /**
     * Start the display, e.g. by opening a graphical window.
     */
    public void start();

    /**
     * Close the frame, if one exists, and free any needed resources.
     */
    public void closeFrame();

    /**
     * Do any polling for input that might be needed.  Return
     * true if input was received.
     *
     * @param mouseEvents       true iff mouse events are wanted, too
     * @return true if input was processed
     * @throws IllegalStateException if start() has been called on the
     *				     corresponding SpriteWindow.
     */
    public boolean pollForInput(boolean mouseEvents);
}
