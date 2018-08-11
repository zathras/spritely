
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import java.util.Random;

import edu.calpoly.spritely.AnimationFrame;
import edu.calpoly.spritely.SpriteWindow;
import edu.calpoly.spritely.Size;
import edu.calpoly.spritely.Tile;
import edu.calpoly.spritely.SolidColorTile;
import edu.calpoly.spritely.ImageTile;


public class Main {

    private final Size gridSize = new Size(30, 20);
    private final Tile[][] tiles = new Tile[gridSize.width][gridSize.height];
    private final Random rand = new Random();
    private final SpriteWindow window 
        = new SpriteWindow("Kimmy Discovers Sprites!", gridSize);

    private Tile backgroundTile;

    private void mouseClicked(int x, int y) {
        System.out.println("Mouse clicked:  x=" + x + ", y=" + y + "    ");
    }

    private void keyTyped(char ch) {
        System.out.println("Key typed:  " + ch + "    ");
        if (ch == (char) 4 || ch == 'q') {
            System.out.println();
            System.out.println("Quitting program.    ");
            System.out.println();
            System.exit(0);
        } else if (ch == 'p') {
            System.out.println("Pausing animation for 5 seconds...    ");
            window.pauseAnimation(5000);
            System.out.println("Pause done.    ");
        } else if (ch == 'r') {
	    System.out.println("Testing reset of animation clock...");
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException ex) {
		Thread.currentThread().interrupt();
	    }
	    window.pauseAnimation(0);
	    System.out.println("Resuming.");
	}
    }

    public void run() throws IOException {
        // SpriteWindow.setTextMode();
        window.setFps(10);
        window.setKeyTypedHandler(ch -> keyTyped(ch));
        window.setMouseClickedHandler((x, y) -> mouseClicked(x, y));

        window.start();
        backgroundTile = new SolidColorTile(Color.blue.darker(), '.');
        File imageFile = new File(
                "../docs/pink_flying_toaster_morgaine1976-300px.png");
        Tile toaster = new ImageTile(imageFile, window.getTileSize(), '*');
        for (int i = 0; i < 500; i++) {
            AnimationFrame frame = window.waitForNextFrame();
            if (frame == null) {
                return;
            }
            for (int j = 0; j < 4; j++) {
                int x = rand.nextInt(gridSize.width);
                int y = rand.nextInt(gridSize.height);
                Color color = new Color(rand.nextInt(256), rand.nextInt(256),
                                        rand.nextInt(256));
                char c = (char) ('a' + rand.nextInt(26));
                if (rand.nextInt(2) == 0) {
                    c = Character.toUpperCase(c);
                }
                if (rand.nextInt(20) == 7) {
                    tiles[x][y] = toaster;
                } else {
                    tiles[x][y] = new SolidColorTile(color, c);
                }
            }
            for (int x = 0; x < gridSize.width; x++) {
                for (int y = 0; y < gridSize.height; y++) {
                    frame.addTile(x, y, backgroundTile);
                    Tile t = tiles[x][y];
                    if (t != null) {
                        frame.addTile(x, y, t);
                    }
                }
            }
            window.showNextFrame();
        }
        System.out.println("Stopping...    ");
        window.stop();
        System.out.println("Goodbye    ");
    }

    public static void main(String[] args) throws IOException {
        (new Main()).run();
        System.exit(0);
    }
}
