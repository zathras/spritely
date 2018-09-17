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
import java.awt.image.BufferStrategy;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JComponent;

//
// The main display window for a SpriteWindow in graphical mode.
//
class SpriteCanvas extends AnimatedCanvas implements SpriteDisplay {

    private final SpriteWindow window;
    private AnimationFrame lastAnimationFrame = null;

    SpriteCanvas(SpriteWindow window) {
	super(window.name);
	this.window = window;
    }

    @Override
    AnimationWindow getWindow() {
	return window;
    }

    @Override
    Dimension getCanvasSize() {
	return new Dimension(
                window.gridSize.width * window.tileSize.width,
                window.gridSize.height * window.tileSize.height);
    }

    @Override
    void finishInitialFrame(Dimension canvasSize) {
	if (lastAnimationFrame == null) {
	    Graphics2D g = currentBuffer.createGraphics();
	    g.setColor(Color.black);
	    g.fillRect(0, 0, canvasSize.width, canvasSize.height);
	    g.dispose();
	} else {
	    Rectangle damage = lastAnimationFrame.calculateDamage(null);
	    lastAnimationFrame.paint(currentBuffer, null, damage);
	}
    }

    //
    // Called from SpriteWindow.start()
    //
    public void setInitialFrame(AnimationFrame f) {
	assert lastAnimationFrame == null;
	lastAnimationFrame = f;
    }

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
	showNextBuffer();
    }

}
