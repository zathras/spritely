
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

import edu.calpoly.spritely.AnimationFrame;
import edu.calpoly.spritely.GraphicsWindow;
import edu.calpoly.spritely.Size;

public class TestGraphics implements Runnable {

    private final Size canvasSize= new Size(800, 600);
    private final GraphicsWindow window 
        = new GraphicsWindow("Kimmy Discovers Graphics!", canvasSize);
    private BufferedImage toaster;

    private void mouseClicked(int x, int y) {
        System.out.println("Mouse clicked:  x=" + x + ", y=" + y + "    ");
    }

    private int lastX = Integer.MIN_VALUE;
    private int lastY = Integer.MIN_VALUE;

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

    @Override
    public void run() {
        window.setFps(30);
        window.setKeyTypedHandler(ch -> keyTyped(ch));
        window.setMouseClickedHandler((x, y) -> mouseClicked(x, y));
        File imageFile = new File(
                "../docs/pink_flying_toaster_morgaine1976-300px.png");
	try {
	    toaster = ImageIO.read(imageFile);
	} catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	paintOrbit(window.getInitialFrame(), 0.0);
        window.start();
        for (int i = 0; i < 1500; i++) {
            Graphics2D g = window.waitForNextFrame();
            if (g == null) {
                return;
            }
	    double theta = 2.0 * Math.PI * i / 60.0;
	    paintOrbit(g, theta);
            window.showNextFrame();
        }
        System.out.println("Stopping...    ");
        window.stop();
        System.out.println("Goodbye    ");
    }

    void paintOrbit(Graphics2D g, double theta) {
	g.setColor(Color.black);
	if (Math.IEEEremainder(theta, 2 * Math.PI) > 0) {
	    g.fillRect(0, 0, canvasSize.width, canvasSize.height);
	    g.setColor(Color.yellow);
	    g.fillOval(canvasSize.width/2-20, canvasSize.height/2-20, 40, 40);
	} else {
	    // Test the other mode where we start with the contents of the
	    // last frame
	    window.paintLastFrameTo(g);
	    // Erase the last toaster.  This only works because the image
	    // never overlaps with the sun.
	    g.fillRect(lastX, lastY, 
	    	       toaster.getWidth(null), toaster.getHeight(null));
	}
	double centerX = (canvasSize.width - toaster.getWidth(null)) / 2.0;
	double centerY = (canvasSize.height- toaster.getHeight(null)) / 2.0;
	int x = (int) Math.round(centerX +  250.0 * Math.cos(theta));
	int y = (int) Math.round(centerY + 250.0 * Math.sin(theta));
	g.drawImage(toaster, x, y, null);
	lastX = x;
	lastY = y;
    }
}
