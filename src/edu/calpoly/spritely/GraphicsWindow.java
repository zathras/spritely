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
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JFrame;
import java.util.LinkedList;

/**
 * A GraphicsWindow is a frame for displaying graphical animation.  This
 * is an alternate entry point for the Spritely framework.  It removes
 * the restriction of animating a grid, but it does not give the option
 * of a text mode.  It is also more complicated in some ways.
 * <p>
 * The basic lifecycle of a GraphicsWindow application is as follows:
 * <pre>
 *    GraphicsWindow window = new GraphicsWindow(...);
 *    window.setXXX() (frames/second, callbacks, etc.)
 *    window.start();
 *    while (window.isRunning()) {
 *        update any needed data structures
 *        Grahpics2D g = window.waitForNextFrame();        
 *        if (g == null) {   // Stopped
 *            break;
 *        }
 *        call Graphics2D methods on g to draw frame
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
 * usually changes every frame anyway.  If you really want to optimize 
 * painting, check out <code>paintLastFrameTo(java.awt.Graphics2D)</code>.
 * <p>
 * It's expected that you won't have multiple windows, but you can.
 * You can run each window from its own thread, or you
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
public final class GraphicsWindow extends AnimationWindow {

    Size canvasSize;
    private Graphics2D currGraphics = null;

    private GraphicsCanvas display;

    /**
     * Initialize a GraphicsWindow to represent a canvas
     * canvasSize.width pixels wide and canvasSize.height pixels high.
     *
     * @param   name    The name of this window.  This might be shown
     *                  in the title area of the screen.
     * @param   canvasSize  The pixel size of the grid that is to be animated.
     *
     * @see #setFps(double)
     * @see #setKeyTypedHandler(KeyTypedHandler)
     * @see #setMouseClickedHandler(MouseClickedHandler)
     */
    public GraphicsWindow(String name, Size canvasSize) {
	super(name);
        this.canvasSize = canvasSize;
    }

    @Override
    Display getDisplay() {
	return display;
    }

    /**
     * Get the size of the canvas we're animating.
     *
     * @return the size
     */
    public Size getCanvasSize() {
	return canvasSize;
    }

    /**
     * Get a Graphics to set up the initial screen, before
     * start() is called.  This is entirely optional, but it might 
     * help to avoid a flashing effect when a window is created.
     *
     * @return  A graphics context where client code can draw the
     *		initial screen.
     *		The framework will call dispose() on this graphics when
     *		the frame is shown.
     *
     * @throws IllegalStateException  if we've started, or this has been
     *				      called before.
     */
    public Graphics2D getInitialFrame() {
        checkStarted(false);
	if (display != null) {
	    throw new IllegalStateException();
	}
	display = new GraphicsCanvas(this);
	currGraphics = display.createGraphicsForInitialFrame(canvasSize);
	return currGraphics;
    }

    void finishInitialFrame() {
	if (currGraphics != null) {
	    currGraphics.dispose();
	    currGraphics = null;
	}
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
        checkStarted(false);
        if (display != null) {
	    // getInitialFrame() wasn't called
	    display = new GraphicsCanvas(this);
        }
	super.start();
    }

    /**
     * Wait for the next frame of animation.  Returns null if this window
     * is stopped.  If the current thread is interrupted, the window will
     * be stopped.
     *
     * @return  A graphics where client code can draw the next frame of
     *		animation.
     */
    public Graphics2D waitForNextFrame() {
	if (waitForNextFrameImpl()) {
	    currGraphics = display.createGraphicsForNextBuffer();
	    return currGraphics;
	} else {
	    return null;
	}
    }

    /**
     * Paint the last frame of animation to the given graphics.  This can be
     * a useful optimization if only a small part of the frame has changed.
     *
     * @param 	g	A graphics obtained from waitForNextFrame()
     *
     * @see #waitForNextFrame()
     */
    public void paintLastFrameTo(Graphics2D g) {
	display.paintLastFrameTo(g);
    }

    /**
     * Show the next frame of animation.  The drawing done to the graphics
     * returned by last returned AnimationFrame last returned
     * by waitForNextFrame is displayed to the screen.
     *
     * @throws IllegalStateException if waitForNextFrame() has not been called,
     *				    or if start has not been called, or if the
     *				    animation has been stopped.
     * @see #waitForNextFrame()
     * @see #showNextFrame(boolean)
     */
    public void showNextFrame() {
	showNextFrame(true);
    }

    /**
     * Show the next frame of animation.  The drawing done to the graphics
     * last returned by waitForNextFrame is displayed to the screen.  
     * If this frame is identical to the previous frame, the caller should 
     * pass false for drawingWasDone.  In this case, no drawing should have 
     * been done to the graphics; any such drawing will be ignored.
     *
     * @param	drawingWasDone	Flag do indicate if this frame changes anything
     *				relative to the last frame.
     *
     * @throws IllegalStateException if waitForNextFrame() has not been called,
     *				    or if start has not been called, or if the
     *				    animation has been stopped.
     * @see #waitForNextFrame()
     */
    public void showNextFrame(boolean drawingWasDone) {
        checkStarted(true);
	if (currGraphics == null) {
	    throw new IllegalStateException();
	}
	currGraphics.dispose();
	currGraphics = null;
	display.showFrame(drawingWasDone);
    }
}
