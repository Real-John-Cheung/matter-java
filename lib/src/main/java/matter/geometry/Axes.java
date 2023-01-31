package matter.geometry;

import java.util.ArrayList;

/**
 * methods for axes
 * 
 * @author JohnC
 */
public class Axes {

    /**
     * create a new set of axes from the given vertices
     * 
     * @param vertices vertices
     * @return a new axes (array of vector)
     */
    static public Vector[] fromVertices(Vertices.Vertex[] vertices) {
        ArrayList<Vector> axes = new ArrayList<Vector>();

        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            Vector normal = Vector.normalise(new Vector(vertices[j].y - vertices[i].y, vertices[i].x - vertices[j].x));
            //double gradient = (normal.y == 0) ? Double.POSITIVE_INFINITY : (normal.x / normal.y);
            axes.add(normal);
        }
        return axes.toArray(Vector[]::new);
    }

    /**
     * rotata a set of axes by the given angle
     * 
     * @param axes axes
     * @param angle angle
     */
    static public void rotate(Vector[] axes, double angle) {
        if (angle == 0)
            return;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0; i < axes.length; i++) {
            Vector axis = axes[i];
            double xx;
            xx = axis.x * cos - axis.y * sin;
            axis.y = axis.x * sin + axis.y * cos;
            axis.x = xx;
        }
    }
}