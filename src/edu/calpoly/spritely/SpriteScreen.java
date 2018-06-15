
package edu.calpoly.spritely;

import java.io.IOException;

/**
 * An object to display sprite animation on the terminal screen.
 */
final class SpriteScreen implements SpriteDisplay {

    private boolean mouseNoticeGiven = false;
    private boolean frameShown = false;
    private SpriteWindow window;

    SpriteScreen(SpriteWindow window) {
        this.window = window;
    }

    public void start() {
        clearScreen();
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
            System.out.println();
            System.out.println("Spritely:  Note that you can simulate a " +
                               "mouse click ty typing \"m\".");
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
