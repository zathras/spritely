
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

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An AnimationController controls the timing aspects on an animation.
 * It is used internally in spritely, but it can also be used by client
 * code to control any kind of frame-based animation.  For example, you
 * can use AnimationController to control animation on a 
 * <code>java.awt.Canvas</code> object that is embedded in a widget
 * hierarchy.  To use it in this way, something like the following
 * would work:
 * <pre>
 *
 *    Canvas canvas = ...;
 *    AnimationController controller = new AnimationController();
 *    controller.setXXX() (frames/second, etc.)
 *    controller.setOpened();  // Assuming canvas has been opened
 *    controller.start();
 *    while (controller.isRunning()) {
 *        update any needed data structures
 *        if (controller.waitForNextFrame()) {
 *            break;
 *        }
 *        Draw the next frame into a buffer;
 *        Copy that buffer to canvas, e.g. using java.awt.image.BufferStrategy
 *        do anything else you want to do
 *        if (you're done with this animation) {
 *            controller.stop();
 *        }
 *    }
 * </pre>
 */
 public final class AnimationController {
    /**
     * The default number of frames per second.
     */
    public final static double DEFAULT_FPS = 30.0;

    /**
     * The maximum number of frames/second.
     */
    public final static double DEFAULT_MAX_FPS = 60;

    private final static long MS_TO_NANOS = 1_000_000L;

    private final ReentrantLock LOCK = new ReentrantLock();
    private final Condition LOCK_CONDITION = LOCK.newCondition();
    private boolean started = false;
    private boolean running = false;
    private boolean opened = false;
    private double fps = DEFAULT_FPS;
    private double maxFps = DEFAULT_MAX_FPS;
    private long startTime = Long.MIN_VALUE;
    private long currFrame = -1L; 
        // int would only buy < 2 years of animation :-)
    private LinkedList<Runnable> eventQueue = new LinkedList<>();
    private boolean silent = false;
    private boolean inWaitForNextFrame = false;
    private double nextFrameTime = Double.POSITIVE_INFINITY;
        // Time for the next frame relative to getTimeSinceStart() when
        // in 0 fps mode
    private double minNextFrameTime = Double.NEGATIVE_INFINITY;
        // Minimum value for nextFrameTime

    /**
     * Throw an IllegalStateException if started isn't in the expected state.
     * This is useful if you have a setter for a parameter that should only
     * be set before an animation starts.
     *
     * @param	expected	The expected value for started
     *
     * @throws	IllegalStateException   if started isn't in the expected state
     */
    public void checkStarted(boolean expected) {
        if (started != expected) {
            throw new IllegalStateException("AnimationController started "
	    		+ started + ", expected " + expected);
        }
    }

    /**
     * Sets the number of frames/second that are displayed.  A value
     * of 0.0 is permitted; in this case, Spritely will only show
     * a new frame when one is requested.
     *
     * @param   fps     The desired number of frames per second.  If over
     *                  the maximum value, the framerate will be set to the
     *                  maximum.
     * @throws IllegalStateException if start() has been called.
     * @see DEFAULT_FPS
     * @see DEFAULT_MAX_FPS
     * @see #setMaxFps(double)
     */
    public void setFps(double fps) {
        if (fps < 0.0) {
            throw new IllegalArgumentException(
                            "Negative fps value not allowed:  " + fps);
        } else if (fps > maxFps) {
            System.out.println("NOTE (Spritely):  " + fps 
                + " frames/second requested.  " + maxFps + " set instead.");
            fps = maxFps;
        }
        checkStarted(false);
        LOCK.lock();    // Probably unnecessary, but harmless.
        try {
            this.fps = fps;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Sets the maximum number of frames/second.  If Spritely is in
     * event-driven mode (0 frames/second), this sets the maximum fps
     * rate spritely will ever attempt to achieve, regardless of 
     * the next frame time.  If Spritely is in constant-rate mode
     * (frames/second above 0), this sets a ceiling on the frames/second
     * value.
     *
     * @param  maxFps   The desired maximum frames/second value
     *
     * @see #setFps(double)
     * @see DEFAULT_MAX_FPS
     * @see #showNextFrameBy(double)
     */
    public void setMaxFps(double maxFps) {
        if (maxFps < 0.0) {
            throw new IllegalArgumentException(
                            "Negative fps value not allowed:  " + maxFps);
        }
        checkStarted(false);
        this.maxFps = maxFps;
        setFps(fps);
    }

    /**
     * Show the next frame by the given time.  Spritely will make a
     * best-faith effort to show the next frame by this time value, which
     * is on the time scale reported by getTimeSinceStart().  If this value
     * is before the current time, Spritely will show the next frame as soon
     * as possible.  When waitForNextFrame() returns, the "next time value" 
     * is cleared; calling showNextFrameBy() will take effect if called at 
     * any time after waitForNextFrame() returns.
     * <p>
     * This method may be called on any thread.  It may be called multiple
     * times per frame; in this case, the minimum value is considered.
     * <p>
     * This method may only be used if the frames/second value is 0.
     *
     * @param nextTime The time the next frame is desired by.
     *
     * @see #getTimeSinceStart()
     * @see #setFps(double)
     * @see #setMaxFps(double)
     *
     * @throws IllegalStateException if the frames/second value is not 0.
     */
    public void showNextFrameBy(double nextTime) {
        LOCK.lock();
        try {
            if (fps != 0.0) {
                throw new IllegalArgumentException("fps value is not 0:  "+fps);
            }
            if (nextTime < minNextFrameTime) {
                nextTime = minNextFrameTime;
            }
            if (nextTime < nextFrameTime) {
                nextFrameTime = nextTime;
                LOCK_CONDITION.signalAll();
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Get the frames/second value.  Result will be >= 0.0.
     */
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
     * Sets a flag to indicate that the place where the animation is being
     * shown has been opened.  This can be called on any thread, like the
     * AWT event thread, possibly within a WindowEvent's windowOpened callback.
     * A call to waitForNextFrame() on the animation
     * thread will wait until setOpened() gets called before letting animation
     * proceed.
     */
    public void setOpened() {
	LOCK.lock();
	try {
	    opened = true;
	    LOCK_CONDITION.signalAll();
	} finally {
	    LOCK.unlock();
	}
    }

    /**
     * Queue an event to be executed during the next call to waitForNextFrame().
     * This method may be called on any thread, like the AWT event thread.
     * If waitForNextFrame() isn't called (e.g. because the animation has
     * stopped), the event will be silently ignored.
     *
     * @param  event An event to queue for running during waitForNextFrame()
     *
     * @see #waitForNextFrame()
     */
    public void queueEvent(Runnable event) {
	LOCK.lock();
	try {
	    if (eventQueue != null) {
		eventQueue.add(event);
	    }
	    LOCK_CONDITION.signalAll();
	} finally {
	    LOCK.unlock();
	}
    }

    /**
     * Returns true if this animation is running .  An animation
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
	LOCK.lock();
	try {
            return running;
	} finally {
	    LOCK.unlock();
        }
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
        checkStarted(true);
        if (fps > 0.0) {
            return currFrame * (1000 / fps);
        } else {
            return ((double) (System.nanoTime() - startTime)) 
                / ((double) MS_TO_NANOS);
        }
    }


    /**
     * Start this animation.  This may only be called once per animation.
     *
     * @throws IllegalStateException  if we were already started
     */
    public void start() {
        checkStarted(false);
        started = true;
        running = true;
        startTime = System.nanoTime();
        currFrame = -1L;
    }

    /**
     * Stop this animation.
     *
     * @throws IllegalStateException    if we were never started
     */
    public void stop() {
        checkStarted(true);
	LOCK.lock();
	try {
	    running = false;
	    eventQueue = null;
	    LOCK_CONDITION.signalAll();
	} finally {
	    LOCK.unlock();
        }
    }

    /**
     * Execute all queued events, and wait until it's time to produce the 
     * next frame of animation.  Return
     * true if we're still running and all is good, and false if we're
     * stopped.
     *
     * @return true if we're still running, false if stopped.
     */
    public boolean waitForNextFrame() {
	return waitForNextFrame((Display) null, false);
    }

    //
    // For internal spritely use, we need to pass the display here, so that
    // in text mode we can simulate mouse clicks.  There's no reason to
    // expose this functionality externally, because the point of breaking
    // AnimationController out is to allow animation within an AWT/Swing widget
    // hierarchy.
    //
    boolean waitForNextFrame(Display display, boolean mouseWanted) {
        checkNotWaiting();
        inWaitForNextFrame = true;
	currFrame++;
        boolean excused = false;
        try {
            for (;;) {
                Runnable event = null;
                LOCK.lock();
                try {
                    if (!running) {
                        return false;
                    } else if (!eventQueue.isEmpty()) {
                        event = eventQueue.removeFirst();
                    } else if (display != null 
                               && display.pollForInput(mouseWanted)) 
                    {
                        excused = true;
                    } else if (!opened) {
                        assert currFrame == 0;
                        try {
                            LOCK_CONDITION.await();
                        } catch (InterruptedException ex) {
                            stop();
                            Thread.currentThread().interrupt();
                            return false;
                        }
                        startTime = System.nanoTime();
                    } else if (fps == 0.0) {
                        try {
                            double tss = getTimeSinceStart();
                            if (nextFrameTime == Double.POSITIVE_INFINITY) {
                                LOCK_CONDITION.await();
                            } else if (nextFrameTime <= tss) {
                                nextFrameTime = Double.POSITIVE_INFINITY;
                                minNextFrameTime = tss + 1000.0 / maxFps;
                                break;
                            } else {
                                long w = (long)
                                    ((nextFrameTime - tss) * MS_TO_NANOS + 0.5);
                                if (w <= 0) {
                                    break;
                                }
                                LOCK_CONDITION.awaitNanos((long) w);
                            }
                        } catch (InterruptedException ex) {
                            stop();
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    } else {    // constant-rate animation.  This gets hairy!
                        double frameNS = MS_TO_NANOS * 1000 / fps;
                        double timeSinceStart = getTimeSinceStart() * MS_TO_NANOS;
                        long nextTime = startTime + (long) timeSinceStart;
                        long now = System.nanoTime();
                        long waitTime = nextTime - now;
                        if (waitTime < -4 * frameNS 
                            || (excused && waitTime < -frameNS))
                        {
                            // Don't drop more than 4 frames
                            if (excused) {
                                excused = false;
                            } else if (!silent 
                                       && timeSinceStart > (2 * 1000L * MS_TO_NANOS))
                            {
                                System.out.println(
                                    "NOTE (Spritely):  Animation fell behind by " +
                                    (long) Math.ceil((-frameNS - waitTime)
                                                     / MS_TO_NANOS)
                                    + " ms on frame " + currFrame + ".");
                                System.out.println(
                                    "                  Animation clock reset.");
                            }
                            startTime = now - (long) timeSinceStart;
                            break;
                        } else if (waitTime < -frameNS) {
                            currFrame++;	// Drop a frame
                        } else if (waitTime <= 0) {
                            break;
                        } else {
                            try {
                                LOCK_CONDITION.awaitNanos(waitTime);
                            } catch (InterruptedException ex) {
                                stop();
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }
                    }
                } finally {
                    LOCK.unlock();
                }
                if (event != null) {
                    event.run();
                }
            }
        } finally {
            inWaitForNextFrame = false;
        }
	return true;
    }

    //
    // Fail if we're in the middle of a call to waitForNextFrame().
    //
    void checkNotWaiting() {
        if (inWaitForNextFrame) {
            if (GradingSupport.ENABLED 
                && GradingSupport.checkNotWaitingFailIsExcused()) 
            {
                System.out.println(
                        "*** Ignoring a checkNotWaiting() failure ***");
            } else {
                throw new IllegalStateException("It is forbidden to call "
                        + "this while waitForNextFrame() is executing");
            }
        }
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
	if (pauseMS < 0) {
	    return;
	}
	long timeWanted = System.nanoTime() + pauseMS * MS_TO_NANOS;
	LOCK.lock();
	try {
	    for (;;) {
		if (!running) {
		    return;
		}
		long now = System.nanoTime();
		long toWait = timeWanted - now;
		if (toWait <= 0L) {
		    break;
		}
		try {
		    LOCK_CONDITION.awaitNanos(toWait);
		} catch (InterruptedException ex) {
		    stop();
		    Thread.currentThread().interrupt();
		    return;
		}
	    }
	    //
	    // Reset the animation clock:
	    //
	    double timeSinceStart = getTimeSinceStart() * MS_TO_NANOS;
	    long newStart = System.nanoTime() - (long) timeSinceStart;
	    if (newStart > startTime) {
		startTime = newStart;
	    }
	} finally {
	    LOCK.unlock();
	}
    }
}
