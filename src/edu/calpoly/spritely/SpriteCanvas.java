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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JComponent;

//
// The main display window in graphical mode.
//
class SpriteCanvas extends JComponent implements SpriteDisplay {

    private final SpriteWindow window;
    private final JFrame frame;
    private AnimationFrame lastAnimationFrame = null;
    private BufferStrategy bufferStrategy;
    private double scale = 1.0;
    private long keyDownEventWhen = Long.MIN_VALUE;
    private BufferedImage currentBuffer;
    private BufferedImage nextBuffer;

    SpriteCanvas(SpriteWindow window) {
        this.window = window;
	setDoubleBuffered(false);
        frame = new JFrame(window.name);
	frame.setLayout(new BorderLayout());
    }

    double getScale() {
	return scale;
    }


    @Override
    public void start() {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
            @Override
            public void keyTyped(KeyEvent e) {
                handleKeyTyped(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        Dimension d = new Dimension(
                window.gridSize.width * window.tileSize.width,
                window.gridSize.height * window.tileSize.height);
        setPreferredSize(d);
	currentBuffer = new BufferedImage(d.width, d.height, 
					  BufferedImage.TYPE_INT_RGB);
	nextBuffer = new BufferedImage(d.width, d.height, 
				       BufferedImage.TYPE_INT_RGB);
	if (lastAnimationFrame == null) {
	    Graphics2D g = currentBuffer.createGraphics();
	    g.setColor(Color.black);
	    g.fillRect(0, 0, d.width, d.height);
	    g.dispose();
	} else {
	    Rectangle damage = lastAnimationFrame.calculateDamage(null);
	    lastAnimationFrame.paint(currentBuffer, null, damage);
	}
        JScrollPane sp = new JScrollPane(this);
        frame.add(sp);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                window.stop();
            }
	    @Override
	    public void windowOpened(WindowEvent e) {
		window.setOpened();
	    }
        });
	frame.pack();
	frame.createBufferStrategy(2);
	bufferStrategy = frame.getBufferStrategy();
	frame.setVisible(true);

	if (!window.getSilent()) {
	    System.out.println();
	    System.out.println("Spritely:  Type alt-plus, alt-minus, and " +
			       "alt-zero to zoom");
	    System.out.println();
	}
    }

    private void handleKeyPressed(KeyEvent e) {
        if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) {
            int c = e.getKeyCode();

            if (c == KeyEvent.VK_EQUALS) {
                setScale(scale * 1.25);
            } else if (c == KeyEvent.VK_MINUS) {
                setScale(scale * 0.8);
            } else if (c == KeyEvent.VK_0) {
                setScale(1.0);
            } else {
                return;
            }
            keyDownEventWhen = e.getWhen();
        }
    }

    private void handleKeyTyped(KeyEvent e) {
	synchronized(window.LOCK) {
	    if (e.getWhen() != keyDownEventWhen
		|| (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0)
	    {
		// We don't want to send the alt-plus, alt-minus or alt-0
		// to our client.
		window.keyTyped(e.getKeyChar());
		window.LOCK.notifyAll();
	    }
	}
    }

    private void handleMouseClicked(MouseEvent e) {
	synchronized(window.LOCK) {
	    double sx = Math.round(e.getX() / scale);
	    double sy = Math.round(e.getY() / scale);
	    window.mouseClicked(sx, sy);
	    window.LOCK.notifyAll();
	}
    }

    private void setScale(double scale) {
        if (this.scale == scale) {
            return;
        }
        this.scale = scale;
        int w = (int) Math.ceil(scale * window.gridSize.width 
                                * window.tileSize.width);
        int h = (int) Math.ceil(scale * window.gridSize.height 
                                * window.tileSize.height);
        setPreferredSize(new Dimension(w, h));
        revalidate();
	repaint();
    }

    @Override
    public void closeFrame() {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		frame.setVisible(false);
		frame.dispose();
	    }
	});
    }

    @Override
    public void paint(Graphics graphicsArg) {
	synchronized(window.LOCK) {
	    if (scale == 1.0) {
		graphicsArg.drawImage(currentBuffer, 0, 0, null);
	    } else {
		Graphics2D g = (Graphics2D) graphicsArg.create();
		g.scale(scale, scale);
		g.drawImage(currentBuffer, 0, 0, null);
		g.dispose();
	    }
	}
    }

    @Override
    public void showFrame(AnimationFrame f) {
	if (!window.isRunning()) {
	    return;
	}
	// Bounding rectangle for changed tiles
	Rectangle damage = f.calculateDamage(lastAnimationFrame);
	lastAnimationFrame = f;
	if (damage.isEmpty()) {
	    return;
	}
	f.paint(nextBuffer, currentBuffer, damage);
	synchronized(window.LOCK) {
	    BufferedImage tmp = nextBuffer;
	    nextBuffer = currentBuffer;
	    currentBuffer = tmp;
	}
        try {
            do {
                do {
		    synchronized(window.LOCK) {
			if (!window.isRunning()) {
			    return;
			}
		    }
		    Graphics bsg = bufferStrategy.getDrawGraphics();
		    frame.paint(bsg);
		    bsg.dispose();
                } while (bufferStrategy.contentsRestored());
                bufferStrategy.show();
            } while (bufferStrategy.contentsLost());
        } catch (IllegalStateException ex) {
            // If the window is closed while this is in process, we 
            // might get an IllegalStateException here.  With a different
	    // code structure one was seen that looked like a manifestation
            // of JDK bug 6933331:
            //    https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6933331
            // It's relatively harmless, and has been around since 2010.

            System.err.println("Spritely ignoring exception - window closing?");
            System.err.println("  Probably https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6933331");
            ex.printStackTrace();
        }
	Toolkit.getDefaultToolkit().sync();
	// sync() probably does nothing these days, and shouldn't be
	// necessary when using bufferStrategy, but it can't hurt.
    }

    //
    // Called from SpriteWindow.start()
    //
    public void setInitialFrame(AnimationFrame f) {
	synchronized(window.LOCK) {
	    assert lastAnimationFrame == null;
	    lastAnimationFrame = f;
	}
    }

    @Override
    public boolean pollForInput(boolean mouseWanted) {
        // AWT does this for us, without polling
        return false;
    }
}
