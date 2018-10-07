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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.JFrame;
import java.util.LinkedList;

/**
 * A SpriteWindow is a frame for displaying a grid of equal-sized tiles
 * in an animation.  This is the main entry point for the Spritely
 * framework.
 * <p>
 * Tiles may overlap, that is, each position on the grid may have multiple
 * tiles.  Each tile has both a graphical representation and a text
 * representation; in text mode, only the topmost tile can be seen.  Programs
 * may contain multiple SpriteWindow instances.
 * <p>
 * The basic lifecycle of a SpriteWindow application is as follows:
 * <pre>
 *    SpriteWindow window = new SpriteWindow(...);
 *    window.setXXX() (frames/second, callbacks, etc.)
 *    window.start();
 *    while (window.isRunning()) {
 *        update any needed data structures
 *        AnimationFrame frame = window.waitForNextFrame();        
 *        if (frame == null) {   // Stopped
 *            break;
 *        }
 *        if (there might be a change to show in this animation frame) {
 *            call frame.addTile(x, y, Tile) for the displayed tiles;
 *            window.showNextFrame();
 *        }
 *        do anything else you want to do
 *        if (you're done with window) {
 *            window.stop();
 *        }
 *    }
 *    System.exit(0);
 * </pre>
 * You are not required to have a single "event loop" like this, however.
 * You can call window.waitForNextFrame() and window.showFrame() whenever 
 * you want to within your program, as long as you don't try to call it
 * from a mouse or keyboard handler.  It will
 * only call your callbacks during a call to showNextFrame(), so you
 * don't need to handle multi-threading.
 * <p>
 * This style of animation creates a new frame, some number of times per
 * second (controlled by the frames/second value you set).  This frees you
 * from having to track if things have changed; you just fill up a frame
 * object with the current state of the world every time the framework is
 * ready for you.  Don't worry too much about wasting CPU time.  This kind
 * of animation is usually done on a personal computer, where you have CPU
 * power to burn.  Also, in a real program that features motion, something
 * usually changes every frame anyway.  Besides, Spritely is pretty good at
 * figuring out similarities between two animation frames, and only painting
 * what has changed.
 * <p>
 * It's expected that you won't have multiple windows, but you can.  This 
 * would be questionable in text mode, though.  In graphics mode, if you
 * want multiple frames, you can run each window from its own thread, or you
 * can arrange to call showNextFrame on the multiple frame objects, one
 * after the other, from the same thread.
 * <p>
 * This framework can also be used when animation isn't continuous, e.g.
 * in turn-based games.  In this case, fps gives a maximum number of
 * frames/second, which might be useful for periods when there is no
 * user input.  In this case, calling <code>pauseAnimation(0)</code>
 * before <code>waitForNextFrame()</code> will reset the animation clock.
 *
 *      @author         Bill Foote, http://jovial.com
 */
public final class SpriteWindow extends AnimationWindow {

    /**
     * The default tile size:  32x32
     */
    public final static Size DEFAULT_TILE_SIZE = new Size(32, 32);

    Size gridSize;
    Size tileSize = DEFAULT_TILE_SIZE;

    private SpriteDisplay display;
    private AnimationFrame currentAnimationFrame = null;

    /**
     * Initialize a SpriteWindow to represent a grid gridSize.width columns wide
     * and gridSize.height rows high.
     *
     * @param   name    The name of this window.  This might be shown
     *                  in the title area of the screen.
     * @param   gridSize  The number of rows and columns of the grid that
     *                    is to be animated.
     *
     * @see #setFps(double)
     * @see #setTileSize(Size)
     * @see #setKeyTypedHandler(KeyTypedHandler)
     * @see #setMouseClickedHandler(MouseClickedHandler)
     */
    public SpriteWindow(String name, Size gridSize) {
	super(name);
        this.gridSize = gridSize;
    }

    @Override
    Display getDisplay() {
	return display;
    }

    //
    // Input are x, y pixel positions, scaled by zoom factor
    //
    @Override
    void mouseClicked(double sx, double sy) {
	super.mouseClicked(sx / tileSize.width, sy / tileSize.height);
    }

    /**
     * Forces the program into text mode.  This is done by setting
     * the Java system propery java.awt.headless to true.  Normally,
     * this isn't necessary -- if you're in an environment where
     * graphics don't work, Java's graphics environment will detect
     * that.  This method can be useful for testing text mode when you're
     * in an environment that supports graphics.  It must be called
     * before any operation that initializes the Java graphics subsystem.
     */
    public static void setTextMode() {
        System.setProperty("java.awt.headless", "true");
    }


    /**
     * Sets the tile size to the desired value.
     *
     * @param  tileSize The desired tile size, in pixels.
     * @throws IllegalStateException if start() has been called.
     * @see DEFAULT_TILE_SIZE
     */
    public void setTileSize(Size tileSize) {
        checkStarted(false);
        this.tileSize = tileSize;
    }

    /**
     * Get the size of the tiles this window is showing.
     *
     * @return  The tile size, in pixels
     */
    public Size getTileSize() {
        return tileSize;
    }

    /**
     * Get an AnimationFrame to set up the initial screen, before
     * start() is called.  This is entirely optional, but it might 
     * help to avoid a flashing effect when a window is created.
     * The window's tile size must not be changed after this is
     * called.
     *
     * @return  The frame where client code can add tiles to be drawn
     *
     * @throws IllegalStateException  if we've started, or this has been
     *				      called before.
     */
    public AnimationFrame getInitialFrame() {
        checkStarted(false);
	if (currentAnimationFrame != null) {
	    throw new IllegalStateException("getInitialFrame() already called");
	}
	currentAnimationFrame = new AnimationFrame(gridSize, tileSize);
	return currentAnimationFrame;
    }
     

    /**
     * Start this SpriteWindow.  If our environment is graphics-capable,
     * this will create a window where the graphics are displayed.  This
     * may only be called once per SpriteWindow.
     *
     * @throws IllegalStateException  if we were already started
     */
    @Override
    public void start() {
        if (display != null) {
            throw new IllegalStateException();
        }
        GraphicsEnvironment ge
            = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadless()) {
            display = new SpriteScreen(this);
        } else {
            display = new SpriteCanvas(this);
        }
	if (currentAnimationFrame != null) {
	    display.setInitialFrame(currentAnimationFrame);
	}
	super.start();
    }

    /**
     * Wait for the next frame of animation.  Returns null if this window
     * is stopped.  If the current thread is interrupted, the window will
     * be stopped.
     *
     * @return  The frame where client code can add tiles to be drawn, or null
     *		if this animation is not running.
     *
     * @see AnimationFrame#addTile(int, int, Tile)
     */
    public AnimationFrame waitForNextFrame() {
	if (waitForNextFrameImpl()) {
	    currentAnimationFrame = new AnimationFrame(gridSize, tileSize);
	    return currentAnimationFrame;
	} else {
	    return null;
	}
    }

    /**
     * Show the next frame of animation.  The AnimationFrame last returned
     * by waitForNextFrame is displayed to the screen.  If nothing has changed
     * since the last frame of animation, it's OK to not call this method.  If
     * showNextFrame() is not called, the previous animation frame will continue
     * to be shown.
     *
     * @throws IllegalStateException if waitForNextFrame() has not been called,
     *				    or if start has not been called, or if the
     *				    animation has been stopped.
     * @see #waitForNextFrame()
     */
    public void showNextFrame() {
        checkStarted(true);
	if (currentAnimationFrame == null) {
	    throw new IllegalStateException();
	}
	if (GradingSupport.ENABLED) {
	    GradingSupport.fromSpriteWindowShowNextFrame(
	    		this, currentAnimationFrame);
	}
        display.showFrame(currentAnimationFrame);
    }
}
