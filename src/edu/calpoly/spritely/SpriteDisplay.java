
package edu.calpoly.spritely;

/**
 * Something that can display an animation frame.  This can either be
 * a text display, or a graphical display.
 */
interface SpriteDisplay {

    /**
     * Start the display, e.g. by opening a graphical window.
     */
    public void start();

    /**
     * Close the frame, if one exists, and free any needed resources.
     */
    public void closeFrame();

    /**
     * Show the given frame of animation
     *
     * @param f the frame to show
     */
    public void showFrame(AnimationFrame f);

    /**
     * Do any polling for input that might be needed.  Return
     * true if input was received.
     *
     * @param mouseEvents       true iff mouse events are wanted, too
     * @return true if input was processed
     */
    public boolean pollForInput(boolean mouseEvents);
}
