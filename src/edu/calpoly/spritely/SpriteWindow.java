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
 * Tiles may overlap, that is, each position on the grid may have overlapping 
 * tiles.  Each tile has both a graphical representation and a text
 * representation; in text mode, only the topmost tile can be seen.  Programs
 * may contain multiple SpriteWindow instances.
 * <p>
 * The basic lifecycle of a SpriteWindow application is as follows:
 * <pre>
 *    SpriteWindow window = new SpriteWindow(...);
 *    window.setXXX() (frames/second, callbacks, etc.)
 *    window.start();
 *    while (!window.getStopped()) {
 *        update any needed data structures
 *        AnimationFrame frame = window.waitForNextFrame();        
 *        if (frame == null) {   // Stopped
 *            break;
 *        }
 *        call frame.addTile(x, y, Tile) for the displayed tiles;
 *        window.showNextFrame();
 *        do anything else you want to do
 *        if (you're done with window) {
 *            window.stop();
 *        }
 *    }
 *    System.exit(0);
 * </pre>
 * You are not required to have a single "event loop" like this, however.
 * You can call frame.waitForNextFrame() and frame.showFrame() whenever 
 * you want to.  It will
 * only call your callbacks during a call to showNextFrame(), so you
 * don't need to handle multi-threading.
 * <p>
 * It's expected that you won't have multiple frames, but you can.  This 
 * would be questionable in text mode, though.  In graphics mode, if you
 * want multiple frames, you can run each frame from its own thread, or you
 * can arrange to call showNextFrame on the multiple frame objects, one
 * after the other, from the same thread.
 *
 *      @author         Bill Foote, http://jovial.com
 */
public class SpriteWindow {

    /**
     * The default number of frames per second.
     */
    public final static double DEFAULT_FPS = 30.0;

    /**
     * The default tile size:  32x32
     */
    public final static Size DEFAULT_TILE_SIZE = new Size(32, 32);

    final Object LOCK = new Object();
    final String name;
    Size gridSize;
    Size tileSize = DEFAULT_TILE_SIZE;

    private boolean started = false;
    private boolean running = false;
    private boolean opened = false;
    private SpriteDisplay display;
    private double fps = DEFAULT_FPS;
    private AnimationFrame currentAnimationFrame = null;
    private long startTime;
    private long currFrame;     // int buys < 2 years of animation :-)
    private LinkedList<Runnable> eventQueue = new LinkedList<>();
    private KeyTypedHandler keyHandler = null;
    private MouseClickedHandler mouseHandler = null;
    private boolean silent = false;

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
        this.gridSize = gridSize;
        this.name = name;
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

    //
    // Throw IllegalStateExcption if started isn't in the right state
    //
    private void checkStarted(boolean expected) {
        if (started != expected) {
            throw new IllegalStateException("SpriteWindow.start()");
        }
    }

    /**
     * Sets the number of frames/second that are displayed.
     *
     * @param   fps     The desired number of frames per second
     * @throws IllegalStateException if start() has been called.
     * @see DEFAULT_FPS
     */
    public void setFps(double fps) {
        checkStarted(false);
        this.fps = fps;
    }


    /**
     * Sets the tile size to the desired value.
     *
     * @param  tileSize The desired tile size
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
     * Sets a flag to silence warnings that are sent to the
     * terminal, such as when animation falls behind.  This
     * flag defaults to false.  The flag can be changed at
     * any time.
     *
     * @param silent	true if you don't want to get warnings.
     */
    public void setSilent(boolean silent) {
	this.silent = silent;
    }

    boolean getSilent() {
	return silent;
    }

    /**
     * Sets a key typed handler.  If a key is typed, the key typed event
     * will be be sent to the handler during a call to waitForNextFrame()
     *
     * @param  handler  The handler.  
     * @throws IllegalStateException if start() has been called, or if
     *                               a handler was previously set.
     * @see #waitForNextFrame()
     */
    public void setKeyTypedHandler(KeyTypedHandler handler) {
        checkStarted(false);
        if (this.keyHandler != null) {
            throw new IllegalStateException();
        }
        this.keyHandler = handler;
    }

    /**
     * Sets a mouse handler.  If the mouse is clicked, the mouse event will
     * be sent to the handler during a call to waitForNextFrame()
     *
     * @param handler   The handler.
     * @throws IllegalStateException if start() has been called.
     * @see #waitForNextFrame()
     */
    public void setMouseClickedHandler(MouseClickedHandler handler) {
        checkStarted(false);
        if (this.mouseHandler != null) {
            throw new IllegalStateException();
        }
        this.mouseHandler = handler;
    }

    void keyTyped(char ch) {
        if (keyHandler != null) {
            eventQueue.add(() -> keyHandler.keyTyped(ch));
        }
    }

    //
    // Input are x, y pixel positions, scaled by zoom factor
    //
    void mouseClicked(double sx, double sy) {
        if (mouseHandler != null) {
            final int x = (int) (sx / tileSize.width);
            final int y = (int) (sy / tileSize.height);
            eventQueue.add(() -> mouseHandler.mouseClicked(x, y));
        }
    }
    
    void setOpened() {
	synchronized(LOCK) {
	    opened = true;
	    LOCK.notifyAll();
	}
    }


    /**
     * Returns true if this animation window is running .  An animation
     * window can be stopped by calling stop().  Additionally, it
     * is stopped if the current thread is interrupted during a call to
     * waitForNextFrame().
     *
     * @return true iff we're running
     * @see Thread#interrupt()
     * @see #stop()
     */
    public boolean isRunning() {
        if (!started) {
            return false;
        }
        synchronized(LOCK) {
            return running;
        }
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
	currentAnimationFrame = new AnimationFrame(gridSize, tileSize, null);
	return currentAnimationFrame;
    }
     

    /**
     * Start this SpriteWindow.  If our environment is graphics-capable,
     * this will create a window where the graphics are displayed.  This
     * may only be called once per SpriteWindow.
     *
     * @throws IllegalStateException  if we were already started
     */
    public void start() {
        checkStarted(false);
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
        started = true;
        running = true;
	if (currentAnimationFrame != null) {
	    display.setInitialFrame(currentAnimationFrame);
	}
        display.start();
        startTime = System.currentTimeMillis();
        currFrame = -1L;
    }

    /**
     * Stop this SpriteWindow's animation, and close the window, if it's
     * visible.
     *
     * @throws IllegalStateException    if we were never started
     */
    public void stop() {
        checkStarted(true);
        synchronized(LOCK) {
            if (running) {
                running = false;
                display.closeFrame();
            }
            LOCK.notifyAll();
        }
    }

    /**
     * Give the time since the start of the animation of the current animation
     * frame, in milliseconds.  This value can drift off of wall clock time,
     * if the animation is too slow.  This can also happen if the program
     * is suspended for a time, e.g. for debugging.  It is therefore 
     * recommended that all time-based events in an animation be based off 
     * the time value returned by this method, rather than e.g.
     * System.currentTimeMillis().
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
     * @see System#currentTimeMillis()
     */
    public double getTimeSinceStart() {
        return currFrame * (1000 / fps);
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
        synchronized(LOCK) {
            currFrame++;
        }
        boolean excused = false;
        for (;;) {
            Runnable event = null;
            synchronized(LOCK) {
                if (!running) {
                    return null;
                } else if (!eventQueue.isEmpty()) {
                    event = eventQueue.removeFirst();
                } else if (display.pollForInput(mouseHandler != null)) {
                    excused = true;
		} else if (!opened) {
		    assert currFrame == 0;
		    try {
			LOCK.wait();
		    } catch (InterruptedException ex) {
			stop();
			Thread.currentThread().interrupt();
			return null;
		    }
		    startTime = System.currentTimeMillis();
                } else {
                    double frameMS = 1000 / fps;
                    double timeSinceStart = getTimeSinceStart();
                    long nextTime = startTime + (long) timeSinceStart;
                    long now = System.currentTimeMillis();
                    long waitTime = nextTime - now;
                    if (waitTime < -4 * frameMS 
		        || (excused && waitTime < -frameMS))
		    {
			// Don't drop more than 4 frames
                        if (excused) {
                            excused = false;
                        } else if (!silent) {
                            System.out.println(
                                "NOTE (Spritely):  Animation fell behind by " +
				(long) Math.ceil(-frameMS - waitTime) + 
				" ms on frame " + currFrame + ".");
			    System.out.println(
				"                  Animation clock reset.");
                        }
                        startTime = now - (long) timeSinceStart;
                        break;
                    } else if (waitTime < -frameMS) {
			currFrame++;	// Drop a frame
                    } else if (waitTime <= 0) {
                        break;
                    } else {
                        try {
                            LOCK.wait(waitTime);
                        } catch (InterruptedException ex) {
                            stop();
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }
                }
            }
            if (event != null) {
                event.run();
            }
        }
        currentAnimationFrame = 
            new AnimationFrame(gridSize, tileSize, currentAnimationFrame);
        return currentAnimationFrame;
    }

    /**
     * Show the next frame of animation.  The AnimationFrame last returned
     * by waitForNextFrame is displayed to the screen.
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
        display.showFrame(currentAnimationFrame);
    }

    /**
     * Pause the animation for the given time, and reset the animation
     * clock.  Pausing the program might be useful for  debugging.  This
     * is particularly true in text mode, since the screen's cursor is 
     * constantly being sent to the home position, which tends to
     * scramble debut output.
     * <p>
     * Return immediately if the animation stops, e.g. because the
     * window is closed.
     *
     * @param pauseMS   The number of milliseconds to pause
     * 
     * @throws IllegalStateException  if we haven't been started
     */
     public void pauseAnimation(int pauseMS) {
        if (display == null) {
            throw new IllegalStateException();
        }
	if (pauseMS <= 0) {
	    return;
	}
	long timeWanted = System.currentTimeMillis() + pauseMS;
	synchronized(LOCK) {
	    for (;;) {
		if (!running) {
		    return;
		}
		long now = System.currentTimeMillis();
		long toWait = timeWanted - now;
		if (toWait <= 0L) {
		    break;
		}
		try {
		    LOCK.wait(toWait);
		} catch (InterruptedException ex) {
		    stop();
		    Thread.currentThread().interrupt();
		    return;
		}
	    }
	    //
	    // Reset the animation clock:
	    //
	    double frameMS = 1000 / fps;
	    double timeSinceStart = getTimeSinceStart();
	    long now = System.currentTimeMillis();
	    startTime = now - (long) (timeSinceStart + frameMS);
	}
    }
}
