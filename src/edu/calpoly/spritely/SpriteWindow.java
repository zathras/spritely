//
// TODO:  When window closes, stop the SpriteWindow
//

package edu.calpoly.spritely;

import java.awt.GraphicsEnvironment;
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
 *    SpriteWindow window = new SpriteWindow();
 *    window.setXXX() (initial width, height, callbacks, etc.)
 *    window.start();
 *    while (!window.getStopped()) {
 *        update any needed data structures
 *        AnimationFrame frame = window.waitForNextFrame();        
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
 */
public class SpriteWindow {

    /**
     * The default number of frames per second.
     */
    public final static double DEFAULT_FPS = 60.0;

    /**
     * The default tile size:  32x32
     */
    public final static Size DEFAULT_TILE_SIZE = new Size(32, 32);

    String name;
    Size gridSize;
    Size tileSize = DEFAULT_TILE_SIZE;

    private boolean started = false;
    private boolean running = false;
    private SpriteDisplay display;
    private double fps = DEFAULT_FPS;
    private AnimationFrame currentAnimationFrame = null;
    private long startTime;
    private long currFrame;     // int buys < 2 years of animation :-)
    private LinkedList<Runnable> eventQueue = new LinkedList<>();
    private KeyTypedHandler keyHandler = null;
    private MouseClickedHandler mouseHandler = null;

    /**
     * Initialize a SpriteWindow to represent a grid gridSize.width columns wide
     * and gridSize.height rows high.
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
     * @throws IllegalStateException if start() has been called.
     * @see DEFAULT_TILE_SIZE
     */
    public void setTileSize(Size tileSize) {
        checkStarted(false);
        this.tileSize = tileSize;
    }

    /**
     * Get the size of the tiles this window is showing.
     */
    public Size getTileSize() {
        return tileSize;
    }

    /**
     * Sets a key typed handler.  If a key is typed, the key typed event
     * will be be sent to the handler during a call to waitForNextFrame()
     *
     * @throws IllegalStateException if start() has been called.
     * @see #waitForNextFrame()
     */
    public void setKeyTypedHandler(KeyTypedHandler handler) {
        checkStarted(false);
        this.keyHandler = handler;
    }

    /**
     * Sets a mouse handler.  If the mouse is clicked, the mouse event will
     * be sent to the handler during a call to waitForNextFrame()
     *
     * @throws IllegalStateException if start() has been called.
     * @see #waitForNextFrame()
     */
    public void setMouseClickedHandler(MouseClickedHandler handler) {
        checkStarted(false);
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


    /**
     * Returns true if this animation window is stopped.  An animation
     * window can be stopped by calling stop().  Additionally, it
     * is stopped if the current thread is interrupted during a call to
     * waitForNextFrame().
     *
     * @see Thread#interrupt()
     */
    public boolean getStopped() {
        if (!started) {
            return false;
        }
        synchronized(display) {
            return !running;
        }
    }

    /**
     * Start this SpriteWindow.  If our environment is graphics-capable,
     * this will create a window where the graphics are displayed.  This
     * may only be called once per SpriteWindow.
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
        display.start();
        startTime = System.currentTimeMillis();
        currFrame = -1L;
    }

    /**
     * Stop this SpriteWindow's animation, and close the window, if it's
     * visible.
     */
    public void stop() {
        checkStarted(true);
        synchronized(display) {
            if (running) {
                running = false;
                display.closeFrame();
            }
            display.notifyAll();
        }
    }

    /**
     * The time since the start of the animation of the current animation
     * frame, in milliseconds.  This value can drift off of wall clock time,
     * if the animation is too slow.  This can also happen if the program
     * is suspended for a time.
     */
    public double getTimeSinceStart() {
        return currFrame * (1000 / fps);
    }

    /**
     * Wait for the next frame of animation.  Returns null if this window
     * is stopped.  If the current thread is interrupted, the window will
     * be stopped.
     */
    public AnimationFrame waitForNextFrame() {
        synchronized(display) {
            currFrame++;
        }
        boolean excused = false;
        for (;;) {
            Runnable event = null;
            synchronized(display) {
                if (!running) {
                    return null;
                } else if (!eventQueue.isEmpty()) {
                    event = eventQueue.removeFirst();
                } else if (display.pollForInput(mouseHandler != null)) {
                    excused = true;
                } else {
                    double frameMS = 1000 / fps;
                    double timeSinceStart = getTimeSinceStart();
                    long nextTime = startTime + (long) timeSinceStart;
                    long now = System.currentTimeMillis();
                    long waitTime = nextTime - now;
                    if (waitTime < -frameMS) {
                        if (excused) {
                            excused = false;
                        } else {
                            System.out.println(
                                "NOTE:  Animation fell behind by more than "+
                                "a full frame on frame " + currFrame + ".");
                            System.out.println("       Animation clock reset.");
                        }
                        startTime = now - (long) timeSinceStart;
                        break;
                    } else if (waitTime <= 0) {
                        break;
                    } else {
                        try {
                            display.wait(waitTime);
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
     * @see #waitForNextFrame()
     */
    public void showNextFrame() {
        display.showFrame(currentAnimationFrame);
    }

    /**
     * Pause the animation for the given time, and reset the animation
     * clock.  Pausing the program might be useful for  debugging.  This
     * is particularly ture in text mode, since the screen's cursor is 
     * constantly being sent to the home position, which tends to
     * scramble debut output.
     *
     * @param pauseMS   The number of seconds to pause
     */
     public void pauseAnimation(int pauseMS) {
        try {
            Thread.currentThread().sleep(pauseMS);
        } catch (InterruptedException ex) {
            stop();
            Thread.currentThread().interrupt();
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
