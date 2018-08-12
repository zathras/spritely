
package edu.calpoly.spritely;
import javax.swing.JFrame;
import java.util.LinkedList;

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
    final Object LOCK = new Object();

    private boolean started = false;
    private boolean running = false;
    private boolean opened = false;
    private double fps = DEFAULT_FPS;
    private long startTime;
    private long currFrame;     // int would only buy < 2 years of animation :-)
    private LinkedList<Runnable> eventQueue = new LinkedList<>();
    private KeyTypedHandler keyHandler = null;
    private MouseClickedHandler mouseHandler = null;
    private boolean silent = false;

    AnimationWindow(String name) {
	this.name = name;
    }

    abstract Display getDisplay();

    //
    // Throw IllegalStateExcption if started isn't in the right state
    //
    void checkStarted(boolean expected) {
        if (started != expected) {
            throw new IllegalStateException("AnimationWindow.start()");
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

    double getFps() {
	return fps;
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
     * @see SpriteWindow#waitForNextFrame()
     * @see GraphicsWindow#waitForNextFrame()
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
     * be sent to the handler during a call to waitForNextFrame().  Note
     * that SpriteWindow scales the x an y coordinate so that they refer
     * to tiles, not pixels.
     *
     * @param handler   The handler.
     * @throws IllegalStateException if start() has been called.
     * @see SpriteWindow#waitForNextFrame()
     * @see GraphicsWindow#waitForNextFrame()
     */
    public void setMouseClickedHandler(MouseClickedHandler handler) {
        checkStarted(false);
        if (this.mouseHandler != null) {
            throw new IllegalStateException();
        }
        this.mouseHandler = handler;
    }

    void keyTyped(char ch) {
        if (running && keyHandler != null) {
            eventQueue.add(() -> keyHandler.keyTyped(ch));
        }
    }

    //
    // Input are x, y pixel positions, scaled by zoom factor.  SpriteWindow
    // further scales by tile size.
    //
    void mouseClicked(double sx, double sy) {
        if (running && mouseHandler != null) {
            final int x = (int) sx;
            final int y = (int) sy;
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
     * is stopped if the current thread is interrupted, e.g. while waiting 
     * for when the next animation frame is ready.
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
     * Start this window.  This * may only be called once per window.
     *
     * @throws IllegalStateException  if we were already started
     */
    public void start() {
	Display display = getDisplay();
	assert display  != null;	// our subclass must create a display
        checkStarted(false);
        started = true;
        running = true;
        display.start();
        startTime = System.currentTimeMillis();
        currFrame = -1L;
    }

    /**
     * Stop this AnimationWindow's animation, and close the window, if it's
     * visible.
     *
     * @throws IllegalStateException    if we were never started
     */
    public void stop() {
        checkStarted(true);
        synchronized(LOCK) {
	    running = false;
	    eventQueue = null;
            LOCK.notifyAll();
        }
	getDisplay().closeFrame();
    }

    //
    // Wait until it's time to produce the next frame of animation.  Return
    // true if we're still running and all is good, and false if we're
    // stopped.  Subclasses implement a public waitForNextFrame() that
    //
    boolean waitForNextFrameImpl() {
        synchronized(LOCK) {
            currFrame++;
        }
        boolean excused = false;
        for (;;) {
            Runnable event = null;
            synchronized(LOCK) {
                if (!running) {
                    return false;
                } else if (!eventQueue.isEmpty()) {
                    event = eventQueue.removeFirst();
                } else if (getDisplay().pollForInput(mouseHandler != null)) {
                    excused = true;
		} else if (!opened) {
		    assert currFrame == 0;
		    try {
			LOCK.wait();
		    } catch (InterruptedException ex) {
			stop();
			Thread.currentThread().interrupt();
			return false;
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
                            return false;
                        }
                    }
                }
            }
            if (event != null) {
                event.run();
            }
        }
	return true;
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
	if (pauseMS < 0) {
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
	    double timeSinceStart = getTimeSinceStart();
	    long newStart = System.currentTimeMillis() - (long) timeSinceStart;
	    if (newStart > startTime) {
		startTime = newStart;
	    }
	}
    }
}
