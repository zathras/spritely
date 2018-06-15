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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

//
// The main display window in graphical mode.
//
class SpriteCanvas extends JComponent implements SpriteDisplay {

    private final SpriteWindow window;
    private final JFrame frame;
    private AnimationFrame animationFrame = null;
    private double scale = 1.0;
    private long keyDownEventWhen = Long.MIN_VALUE;

    SpriteCanvas(SpriteWindow window) {
        setDoubleBuffered(true);                // Because I'm paranoid
        this.window = window;
        frame = new JFrame(window.name);
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
        JScrollPane sp = new JScrollPane(this);
        frame.getContentPane().add(sp); // , java.awt.BorderLayout.CENTER);
        Dimension d = new Dimension(
                window.gridSize.width * window.tileSize.width,
                window.gridSize.height * window.tileSize.height);
        setPreferredSize(d);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                window.stop();
            }
        });
        frame.pack();
        frame.setVisible(true);
        System.out.println();
        System.out.println("Spritely:  Type alt-plus, alt-minus, and " +
                           "alt-zero to zoom");
        System.out.println();

    }

    private void handleKeyPressed(KeyEvent e) {
        if ((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
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

    private synchronized void handleKeyTyped(KeyEvent e) {
        if (e.getWhen() != keyDownEventWhen
            || (e.getModifiers() & ActionEvent.ALT_MASK) == 0)
        {
            // We don't want to send the alt-plus, alt-minus or alt-0
            // to our client.
            window.keyTyped(e.getKeyChar());
            notifyAll();
        }
    }

    private synchronized void handleMouseClicked(MouseEvent e) {
        double sx = Math.round(e.getX() / scale);
        double sy = Math.round(e.getY() / scale);
        window.mouseClicked(sx, sy);
        notifyAll();
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
    }

    @Override
    public synchronized void closeFrame() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(false);
            frame.dispose();
        });
    }

    @Override
    public synchronized void paint(Graphics graphicsArg) {
        if (animationFrame == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphicsArg;
        g.setColor(Color.black);
        Dimension d = getSize();
        g.fillRect(0, 0, d.width, d.height);
        if (scale != 0) {
            g.scale(scale, scale);
        }
        animationFrame.paint(g);
    }

    @Override
    public synchronized void showFrame(AnimationFrame f) {
        animationFrame = f;
        Dimension d = getSize();
        repaint();
    }

    @Override
    public boolean pollForInput(boolean mouseWanted) {
        // AWT does this for us, without polling
        return false;
    }
}
