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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

//
// The main display window in for a GrahpicsWindow 
//
final class GraphicsCanvas extends AnimatedCanvas {

    private final GraphicsWindow window;

    GraphicsCanvas(GraphicsWindow window) {
	super(window.name);
	this.window = window;
    }

    @Override
    AnimationWindow getWindow() {
	return window;
    }

    @Override
    Dimension getCanvasSize() {
	Size canvasSize = window.canvasSize;
	return new Dimension(canvasSize.width, canvasSize.height);
    }

    Graphics2D createGraphicsForInitialFrame(Size canvasSize) {
	assert currentBuffer == null;
	currentBuffer = createBuffer(canvasSize.width, canvasSize.height);
	return currentBuffer.createGraphics();
    }

    Graphics2D createGraphicsForNextBuffer() {
	return nextBuffer.createGraphics();
    }

    @Override
    void finishInitialFrame(Dimension canvasSize) {
	window.finishInitialFrame();
    }

    void paintLastFrameTo(Graphics2D g) {
	g.drawImage(currentBuffer, 0, 0, null);
    }

    void showFrame(boolean drawingWasDone) {
	if (drawingWasDone) {
	    showNextBuffer();
	}
    }
}
