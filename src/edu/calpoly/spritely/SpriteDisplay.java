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

/**
 * Something that can display an animation frame.  This can either be
 * a text display, or a graphical display.
 *
 *      @author         Bill Foote, http://jovial.com
 */
interface SpriteDisplay {

    /**
     * Start the display, e.g. by opening a graphical window.
     */
    public void start();

    /**
     * Close the frame, if one exists, and free any needed resources.
     */
    public void closeFrame();

    /**
     * Show the given frame of animation
     *
     * @param f the frame to show
     */
    public void showFrame(AnimationFrame f);

    /**
     * Do any polling for input that might be needed.  Return
     * true if input was received.
     *
     * @param mouseEvents       true iff mouse events are wanted, too
     * @return true if input was processed
     */
    public boolean pollForInput(boolean mouseEvents);
}
