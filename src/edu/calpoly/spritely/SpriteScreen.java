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

import java.io.IOException;

/**
 * An object to display sprite animation on the terminal screen.
 *
 *      @author         Bill Foote, http://jovial.com
 */
final class SpriteScreen implements SpriteDisplay {

    private boolean mouseNoticeGiven = false;
    private boolean frameShown = false;
    private SpriteWindow window;

    SpriteScreen(SpriteWindow window) {
        this.window = window;
    }

    public void start() {
        System.out.println();
        // Set the title:  http://tldp.org/HOWTO/Xterm-Title-3.html
        System.out.println("" + ((char) 27) + "]0;" + window.name + ((char) 7));
        clearScreen();
	window.setOpened();
    }

    public void closeFrame() {
    }

    private void clearScreen() {
        System.out.print("" + ((char) 27) + "[2J");
        // clear screen escape sequence
        // see https://en.wikipedia.org/wiki/ANSI_escape_code
        sendCursorHome();
    }

    /**
     * Clear from the current cursor position to the end of the screen.
     */
    private void clearToEndOfScreen() {
        System.out.print("" + ((char) 27) + "[0J");
        // see https://en.wikipedia.org/wiki/ANSI_escape_code
        System.out.flush();
    }

    //
    // Send cursor to an x,y coordinate (counting from 0
    //

    private void sendCursorTo(int x, int y) {
        System.out.print("" + ((char) 27) + "[" + (y+1) + ";" + (x+1) + "H");
        // CUP escape sequence
        // see https://en.wikipedia.org/wiki/ANSI_escape_code
        System.out.flush();
    }
    //
    // Send the cursor to the upper-left hand corner
    //
    private void sendCursorHome() {
        sendCursorTo(0, 0);
    }


    public void showFrame(AnimationFrame f) {
        sendCursorHome();
        f.print();
        frameShown = true;
    }

    @Override
    public synchronized boolean pollForInput(boolean mouseWanted) {
        if (!mouseNoticeGiven && frameShown && mouseWanted) {
	    // We don't respect window.getSilent() here, since text mode
	    // really is just intended for debugging.
            System.out.println();
            System.out.println("Spritely:  Note that you can simulate a " +
                               "mouse click by typing \"m\".");
            System.out.println();
            mouseNoticeGiven = true;
        }
        boolean inputProcessed = false;
        try {
            while (System.in.available() > 0) {
                char ch = (char) System.in.read();
                if (mouseWanted && ch == 'm') {
                    simulateMouseInput();
                } else {
                    window.keyTyped(ch);
                }
                inputProcessed = true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Exception ignored.");
        }
        return inputProcessed;
    }

    private void simulateMouseInput() throws IOException {
        clearToEndOfScreen();
        System.out.println();
        System.out.println("Spritely:  Simulated mouse input " +
                           "(\"m\" entered)    ");
        System.out.println(
            "    Move the cursor with h,j,k,l (right/down/up/left)    ");
        System.out.println("    Press enter when done, q to quit.    ");
        System.out.println("    Press m to send an m to the program.    ");
        int x = 0;
        int y = 0;
        for (;;) {
            sendCursorTo(1 + x * 2, y);
            char ch = (char) System.in.read();
            if (ch == 'm') {
                window.keyTyped(ch);
                break;
            } else if (ch == '\n' || ch == '\r') {
                window.mouseClicked(x * window.tileSize.width, 
                                    y * window.tileSize.height);
                break;
            } else if (ch == 'h' && x > 0) {
                x--;
            } else  if (ch == 'j' && y < window.gridSize.height - 1) {
                y++;
            } else if (ch == 'k' && y > 0) {
                y--;
            } else if (ch == 'l' && x < window.gridSize.width - 1) {
                x++;
            } else {
                System.out.print((char) 7);     // Bell
            }
        }
        sendCursorTo(0, window.gridSize.height);
        clearToEndOfScreen();
    }
}
