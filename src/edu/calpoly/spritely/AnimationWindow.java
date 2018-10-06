
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

/**
 * An AnimationWindow controls the animation of a screen.  
 * {@link SpriteWindow} provides an animation window for a grid of tiles,
 * and offers a text mode. {@link GraphicsWindow} gives unconstrained
 * animation.
 */
public abstract class AnimationWindow {

    /**
     * The default number of frames per second.
     */
    public final static double DEFAULT_FPS = 30.0;

    final String name;
    private final AnimationController controller = new AnimationController();
    private KeyTypedHandler keyHandler = null;
    private MouseClickedHandler mouseHandler = null;

    AnimationWindow(String name) {
	this.name = name;
    }

    abstract Display getDisplay();

    void checkStarted(boolean expected) {
	controller.checkStarted(expected);
    }

    /**
     * Sets the number of frames/second that are displayed.
     *
     * @param   fps     The desired number of frames per second
     * @throws IllegalStateException if start() has been called.
     * @see DEFAULT_FPS
     */
    public void setFps(double fps) {
	controller.setFps(fps);
    }

    double getFps() {
	return controller.getFps();
    }

    /**
     * Sets a flag to silence warnings that are sent to the
     * terminal, such as when animation falls behind.  This
     * flag defaults to false.  The flag can be changed at
     * any time.
     *
     * @param silent	true if you don't want to get warnings.
     */
    public void setSilent(boolean silent) {
	controller.setSilent(silent);
    }

    boolean getSilent() {
	return controller.getSilent();
    }

    /**
     * Sets a key typed handler.  If a key is typed, the key typed event
     * will be be sent to the handler during a call to waitForNextFrame().
     * It is forbidden to call waitForNextFrame() or showNextFrame() from
     * within the body of a handler.
     *
     * @param  handler  The handler.  
     * @throws IllegalStateException if start() has been called, or if
     *                               a handler was previously set.
     * @see SpriteWindow#waitForNextFrame()
     * @see GraphicsWindow#waitForNextFrame()
     */
    public void setKeyTypedHandler(KeyTypedHandler handler) {
        controller.checkStarted(false);
        if (this.keyHandler != null) {
            throw new IllegalStateException();
        }
        this.keyHandler = handler;
    }

    /**
     * Sets a mouse handler.  If the mouse is clicked, the mouse event will
     * be sent to the handler during a call to waitForNextFrame(). 
     * It is forbidden to call waitForNextFrame() or showNextFrame() from
     * within the body of a handler.
     * Note that SpriteWindow scales the x an y coordinate so that they refer
     * to the row and column of a tile square, not pixels.
     *
     * @param handler   The handler.
     * @throws IllegalStateException if start() has been called.
     * @see SpriteWindow#waitForNextFrame()
     * @see GraphicsWindow#waitForNextFrame()
     */
    public void setMouseClickedHandler(MouseClickedHandler handler) {
        controller.checkStarted(false);
        if (this.mouseHandler != null) {
            throw new IllegalStateException();
        }
        this.mouseHandler = handler;
    }

    void keyTyped(char ch) {
        if (controller.isRunning() && keyHandler != null) {
            controller.queueEvent(() -> keyHandler.keyTyped(ch));
        }
    }

    //
    // Input are x, y pixel positions, scaled by zoom factor.  SpriteWindow
    // further scales by tile size.
    //
    void mouseClicked(double sx, double sy) {
        if (controller.isRunning() && mouseHandler != null) {
            final int x = (int) sx;
            final int y = (int) sy;
            controller.queueEvent(() -> mouseHandler.mouseClicked(x, y));
        }
    }
   
    void setOpened() {
	controller.setOpened();
    }


    /**
     * Returns true if this animation window is running .  An animation
     * window can be stopped by calling stop().  Additionally, it
     * is stopped if the current thread is interrupted, e.g. while waiting 
     * for when the next animation frame is ready.
     *
     * @return true iff we're running
     * @see Thread#interrupt()
     * @see #stop()
     */
    public boolean isRunning() {
	return controller.isRunning();
    }

    /**
     * Give the time since the start of the animation of the current animation
     * frame, in milliseconds.  This value can drift off of wall clock time,
     * if the animation is too slow.  This can also happen if the program
     * is suspended for a time, e.g. for debugging.  It is therefore 
     * recommended that all time-based events in an animation be based off 
     * the time value returned by this method, rather than e.g.
     * System.nanoTime().
     * <p>
     * If the system can't keep up with the frame rate, it will drop up
     * to four frames.  Past that limit, it will print a diagnostic
     * message to stdout, and "pause" the animation (that is, it will
     * not advance getTimeSinceStart() even though the wall clock time
     * indicates that it "should").
     *
     * @return  The total elapsed time, adjusted for pauses, in milliseconds.
     *
     * @see #pauseAnimation(int)
     * @see #setSilent(boolean)
     * @see System#nanoTime()
     */
    public double getTimeSinceStart() {
        return controller.getTimeSinceStart();
    }


    /**
     * Start this window.  This may only be called once per window.
     *
     * @throws IllegalStateException  if we were already started
     */
    public void start() {
	controller.start();
	Display display = getDisplay();
	assert display  != null;	// our subclass must create a display
        display.start();
    }

    /**
     * Stop this AnimationWindow's animation, and close the window, if it's
     * visible.
     *
     * @throws IllegalStateException    if we were never started
     */
    public void stop() {
	controller.stop();
	getDisplay().closeFrame();
    }

    //
    // Wait until it's time to produce the next frame of animation.  Return
    // true if we're still running and all is good, and false if we're
    // stopped.  Subclasses implement a public waitForNextFrame() that
    // calls this
    //
    boolean waitForNextFrameImpl() {
	return controller.waitForNextFrame(getDisplay(), mouseHandler != null);
    }

    /**
     * Pause the animation for the given time, and reset the animation
     * clock.  Resetting the animation animation clock makes the framework's
     * idea of when the animation started agree with the current frame number.
     * This avoids having the framework skip frames or print out a warning
     * message if a frame has to wait for something lengthy, like user
     * input.  After the long delay, a call to <code>pauseAnimation(0)</code>
     * will reset the animation clock.
     * <p>
     * Pausing the program might also be useful for  debugging.  This
     * is particularly true in text mode, since the screen's cursor is 
     * constantly being sent to the home position, which tends to
     * scramble debut output.
     * <p>
     * Returns immediately if the animation stops, e.g. because the
     * window is closed.
     *
     * @param pauseMS   The number of milliseconds to pause.  A value
     *			of 0 potentially resets the animation clock, 
     *			with no pause.  A value less than zero causes
     *			an immediate return, with no effect.
     * 
     * @throws IllegalStateException  if we haven't been started
     */
     public void pauseAnimation(int pauseMS) {
        if (getDisplay() == null) {
            throw new IllegalStateException();
        }
	controller.pauseAnimation(pauseMS);
    }
}
