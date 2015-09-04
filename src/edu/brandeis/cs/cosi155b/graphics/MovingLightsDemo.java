package edu.brandeis.cs.cosi155b.graphics;

import edu.brandeis.cs.cosi155b.scene.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kahliloppenheimer on 9/4/15.
 */
public class MovingLightsDemo {

    private static final Scene3D sampleScene = new Scene3D(
            new Sphere3D(new Point3D(0, -.5, -2), .5, null, new Material(Color.RED, 0)),
            new Sphere3D(new Point3D(0, .5, -2), .5, null, new Material(Color.YELLOW, 0)),
            new Sphere3D(new Point3D(-.5, -1.5, -2), .5, null, new Material(Color.PINK, 0)));

    public static void main(String[] args) throws InterruptedException {
        double radius = Math.sqrt(2);
        double radians = 0;
        SimpleFrame3D frame = new SimpleFrame3D(new Point3D(-1, -1, -1), 2, 2, 800, 800);
        Camera3D camera = new Camera3D(new Point3D(0, 0, 0), new Point3D(0, 0, -1));
        List<Light3D> lights = new ArrayList<>();
        lights.add(new Light3D(new Point3D(-2, -2.5, 1), 1));
        // lights.add(new Light3D(new Point3D(-1, -1, -1), .2));
        RayTracer rt = new RayTracer(frame, camera, sampleScene, lights);
        SimpleFrame3D rendered = rt.render();
        Canvas3D canvas = display(rendered);

        while(true) {
            System.out.println("Radians = " + radians);
            radians += Math.PI / 4;
            lights.remove(0);
            lights.add(new Light3D(new Point3D(2 * Math.cos(radians), 2 * Math.sin(radians), .5), 1));
            System.out.println("We have " + lights.size() + " lights");
            rendered = rt.render();
            display(rendered, canvas);
        }

    }

    private static Canvas3D display(SimpleFrame3D rendered) throws InterruptedException {
        Canvas3D canvas = new MyCanvas3D(rendered.getWidthPx(), rendered.getHeightPx());
        SwingUtilities.invokeLater(() -> createAndShowGUI((MyCanvas3D) canvas));
        Thread.sleep(1000);
        display(rendered, canvas);
        return canvas;
    }

    private static void display(SimpleFrame3D rendered, Canvas3D canvas) throws InterruptedException {
        for(int i = 0; i < rendered.getWidthPx(); ++i) {
            for(int j = 0; j < rendered.getHeightPx(); ++j) {
                canvas.drawPixel(i, j, rendered.getPixel(i, j).getColor());
            }
        }
        SwingUtilities.invokeLater( () -> canvas.refresh() );
        Thread.sleep(10);
    }

    /*
     * here we create a window, add the canvas,
     * set the window size and make it visible!
     */
    private static void createAndShowGUI(MyCanvas3D canvas) {

        JFrame f = new JFrame("PA01 Demo");

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(canvas);
        System.out.println("Width = " + canvas.getWidth() + "\theight = " + canvas.getHeight());
        f.setSize(canvas.getWidth(), canvas.getHeight());
        f.setVisible(true);
    }
}