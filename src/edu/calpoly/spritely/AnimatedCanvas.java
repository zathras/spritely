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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JComponent;

//
// The main display window in graphical mode.
//
abstract class AnimatedCanvas extends JComponent implements Display {

    private final JFrame frame;
    private final ReentrantLock LOCK = new ReentrantLock();
    private double scale = 1.0;
    private long keyDownEventWhen = Long.MIN_VALUE;
    protected BufferedImage currentBuffer = null;
    protected BufferedImage nextBuffer;

    AnimatedCanvas(String frameName) {
	setDoubleBuffered(false);
        frame = new JFrame(frameName);
	frame.setLayout(new BorderLayout());
    }

    abstract AnimationWindow getWindow();
    
    abstract Dimension getCanvasSize();

    abstract void finishInitialFrame(Dimension canvasSize);

    double getScale() {
	return scale;
    }

    protected BufferedImage createBuffer(int width, int height) {
	return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

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
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });
        Dimension d = getCanvasSize();
        assert d.height > 0 && d.width > 0 
              && d.height <= 4096 && d.width <= 4096
                : ("Illegal window size:  " + d);
        setPreferredSize(d);
	if (currentBuffer == null) {
	    // Won't be null for a GraphicsWindow where the initial frame
	    // has been drawn to.
	    currentBuffer = createBuffer(d.width, d.height);
	}
	nextBuffer = createBuffer(d.width, d.height);
	finishInitialFrame(d);
        JScrollPane sp = new JScrollPane(this);
        frame.add(sp);
	final AnimationWindow window = getWindow();
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
	if (e.getWhen() != keyDownEventWhen
	    || (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0)
	{
	    // We don't want to send the alt-plus, alt-minus or alt-0
	    // to our client.
	    getWindow().keyTyped(e.getKeyChar());
	}
    }

    private void handleMouseReleased(MouseEvent e) {
	double sx = Math.round(e.getX() / scale);
	double sy = Math.round(e.getY() / scale);
	// Since this is the only mouse gesture we use,
	// mouse released is better than an actual AWT mouse clicked
	// event, since it covers press-drag-release, whereas AWT's
	// mousePressed doesn't.
	getWindow().mouseClicked(sx, sy);
    }

    private void setScale(double scale) {
        if (this.scale == scale) {
            return;
        }
        this.scale = scale;
	Dimension d = getCanvasSize();
	if (scale != 1.0) {
	    int w = (int) Math.ceil(scale * d.width);
	    int h = (int) Math.ceil(scale * d.height);
	    d = new Dimension(w, h);
	}
        setPreferredSize(d);
        revalidate();
	repaint();
    }

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
	LOCK.lock();
	try {
	    if (scale == 1.0) {
		graphicsArg.drawImage(currentBuffer, 0, 0, null);
	    } else {
		Graphics2D g = (Graphics2D) graphicsArg.create();
		g.scale(scale, scale);
		g.drawImage(currentBuffer, 0, 0, null);
		g.dispose();
	    }
	} finally {
	    LOCK.unlock();
	}
    }

    protected void showNextBuffer() {
	LOCK.lock();
	try {
	    BufferedImage tmp = nextBuffer;
	    nextBuffer = currentBuffer;
	    currentBuffer = tmp;
	} finally {
	    LOCK.unlock();
	}
        repaint();
    }

    @Override
    public boolean pollForInput(boolean mouseWanted) {
        // AWT does this for us, without polling
        return false;
    }
}
