

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

import edu.calpoly.spritely.AnimationFrame;
import edu.calpoly.spritely.GraphicsWindow;
import edu.calpoly.spritely.Size;

public class TestEventDriven implements Runnable {

    private static class Event implements Comparable<Event> {
        public final double due;
        public final Runnable payload;

        public Event(double due, Runnable payload) {
            this.due = due;
            this.payload = payload;
        }

        public int compareTo(Event other) {
            if (due < other.due) {
                return -1;
            } else if (due == other.due) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private class Circle {
        public final int x;
        public final int y;
        public Color color;             // mutable

        public Circle(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private final Size canvasSize= new Size(900, 700);
    private final GraphicsWindow window 
        = new GraphicsWindow("Kimmy Discovers Events!", canvasSize);
    private final PriorityQueue<Event> events = new PriorityQueue<Event>();
    private final List<Circle> circles = new ArrayList<Circle>();


    private void mouseClicked(int x, int y) {
        final Circle circle = new Circle(x, y, Color.red);
        circles.add(circle);
        events.add(new Event(window.getTimeSinceStart() + 1000.0, () -> {
            circle.color = Color.green;
        }));
        window.showNextFrameBy(0f);
    }

    @Override
    public void run() {
        double end = 50_000;    // End after 50 seconds
        window.setFps(0f);
        window.setMouseClickedHandler((x, y) -> mouseClicked(x, y));
	paintCircles(window.getInitialFrame());
        window.start();
        while (true) {
            window.showNextFrameBy(end);
            Graphics2D g = window.waitForNextFrame();
            System.out.println("Showing frame:  " + window.getTimeSinceStart());
            processEvents();
            if (g == null) {
                return;
            }
	    paintCircles(g);
            window.showNextFrame();
            if (window.getTimeSinceStart() >= end) {
                break;
            }
        window.showNextFrameBy(0f);
        }
        System.out.println("Stopping...    ");
        window.stop();
        System.out.println("Goodbye.    ");
    }

    private void processEvents() {
        double now = window.getTimeSinceStart();
        while(true) {
            Event e = events.peek();
            if (e == null) {
                return;
            } else if (e.due > now) {
                window.showNextFrameBy(e.due);
                return;
            }
            e = events.poll();
            e.payload.run();
        }
    }

    void paintCircles(Graphics2D g) {
	g.setColor(Color.black);
        g.fillRect(0, 0, canvasSize.width, canvasSize.height);
        for (Circle c : circles) {
            g.setColor(c.color);
	    g.fillOval(c.x - 30, c.y - 30, 60, 60);
        }
    }
}
