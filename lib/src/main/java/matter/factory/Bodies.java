package matter.factory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import matter.core.Common;
import matter.body.Body;
import matter.geometry.Bounds;
import matter.geometry.Vector;
import matter.geometry.Vertices;
import matter.geometry.Vertices.Chamfer;
import matter.geometry.Vertices.Vertex;

/**
 * factory methods for creating rigid body models
 * 
 * @author JohnC
 */
public class Bodies {

    /**
     * create a new rigid body model with a rectangle hull
     * 
     * @param x      x
     * @param y      y
     * @param width  w
     * @param height h
     * @return a new rectangle body
     */
    static public Body rectangle(double x, double y, double width, double height) {
        return Bodies.rectangle(x, y, width, height, null);
    }

    /**
     * create a new rigid body model with a rectangle hull
     * 
     * @param x       x
     * @param y       y
     * @param width   w
     * @param height  h
     * @param options options
     * @return a new rectangle body
     */
    static public Body rectangle(double x, double y, double width, double height, HashMap<String, Object> options) {

        if (options.get("label") == null)
            options.put("label", "Rectangle Body");
        options.put("position", new Vector(x, y));
        // options.put("vertices",
        // Vertices.fromPath("L 0 0 L " + width + " 0 L " + width + " " + height + " L 0
        // " + height, null));
        Chamfer chamfer = (Chamfer) Common.parseOption(options, "chamfer", null);
        if (chamfer != null)
            options.remove("chamfer");
        Body body = new Body(options);
        Vertices.Vertex[] vertices = Vertices.fromPath(
                "L 0 0 L " + width + " 0 L " + width + " " + height + " L 0 " + height,
                body);
        if (chamfer != null) {
            vertices = Vertices.chamfer(vertices, chamfer.radius, chamfer.quality, chamfer.qualityMin,
                    chamfer.qualityMax);
        }
        Body.setVertices(body, vertices);
        return body;
    }

    /**
     * create a new rigid body model with a trapezoid hull
     * 
     * @param x      x
     * @param y      y
     * @param width  w
     * @param height h
     * @param slope  slope
     * @return a new traoezoid body
     */
    static public Body trapezoid(double x, double y, double width, double height, double slope) {
        return Bodies.trapezoid(x, y, width, height, slope, null);
    }

    /**
     * create a new rigid body model with a trapezoid hull
     * 
     * @param x       x
     * @param y       y
     * @param width   w
     * @param height  h
     * @param slope   slop
     * @param options options
     * @return a new traoezoid body
     */
    static public Body trapezoid(double x, double y, double width, double height, double slope,
            HashMap<String, Object> options) {
        slope *= 0.5;
        double roof = (1 - (slope * 2)) * width,
                x1 = width * slope,
                x2 = x1 + roof,
                x3 = x2 + x1;
        String verticesPath;

        if (slope < 0.5) {
            verticesPath = "L 0 0 L " + x1 + " " + (-height) + " L " + x2 + " " + (-height) + " L " + x3 + " 0";
        } else {
            verticesPath = "L 0 0 L " + x2 + " " + (-height) + " L " + x3 + " 0";
        }

        if (options.get("label") == null)
            options.put("label", "Trapezoid Body");
        options.put("position", new Vector(x, y));
        Chamfer chamfer = (Chamfer) Common.parseOption(options, "chamfer", null);
        if (chamfer != null)
            options.remove("chamfer");
        Body body = new Body(options);
        Vertices.Vertex[] vertices = Vertices.fromPath(verticesPath, body);
        if (chamfer != null) {
            vertices = Vertices.chamfer(vertices, chamfer.radius, chamfer.quality, chamfer.qualityMin,
                    chamfer.qualityMax);
        }
        Body.setVertices(body, vertices);
        return body;
    }

    /**
     * create a new rigid body model with a circle hull
     * 
     * @param x      x
     * @param y      y
     * @param radius radius
     * @return a new body
     */
    static public Body circle(double x, double y, double radius) {
        return Bodies.circle(x, y, radius, null, 25);
    }

    /**
     * create a new rigid body model with a circle hull
     * 
     * @param x       x
     * @param y       y
     * @param radius  radius
     * @param options options
     * @return a new body
     */
    static public Body circle(double x, double y, double radius, HashMap<String, Object> options) {
        return Bodies.circle(x, y, radius, options, 25);
    }

    /**
     * create a new rigid body model with a circle hull
     * 
     * @param x       x
     * @param y       y
     * @param radius  radius
     * @param options options
     * @param maxSize maxSides
     * @return a new body
     */
    static public Body circle(double x, double y, double radius, HashMap<String, Object> options, double maxSides) {
        if (options.get("label") == null)
            options.put("label", "Circle Body");
        options.put("circleRadius", radius);

        int sides = (int) Math.ceil(Math.max(10, Math.min(maxSides, radius)));
        if (sides % 2 == 1)
            sides += 1;

        return Bodies.polygon(x, y, sides, radius, options);
    }

    /**
     * create a new rigid body model with a regular polygon hull with the given
     * number of sides
     * 
     * @param x      x
     * @param y      y
     * @param sides  sides
     * @param radius radius
     * @return a new body
     */
    static public Body polygon(double x, double y, int sides, double radius) {
        return Bodies.polygon(x, y, sides, radius, null);
    }

    /**
     * create a new rigid body model with a regular polygon hull with the given
     * number of sides
     * 
     * @param x       x
     * @param y       y
     * @param sides   sides
     * @param radius  radius
     * @param options options
     * @return a new body
     */
    static public Body polygon(double x, double y, int sides, double radius, HashMap<String, Object> options) {
        if (sides < 3)
            return Bodies.circle(x, y, radius, options);
        double theta = 2 * Math.PI / sides, offset = theta * 0.5;
        String path = "";

        DecimalFormat df = new DecimalFormat("#.000");
        for (int i = 0; i < sides; i++) {
            double angle = offset + (i * theta), xx = Math.cos(angle) * radius, yy = Math.sin(angle) * radius;
            path += "L " + df.format(xx) + " " + df.format(yy) + " ";
        }

        if (options.get("label") == null)
            options.put("label", "Polygon Body");
        options.put("position", new Vector(x, y));

        Chamfer chamfer = (Chamfer) Common.parseOption(options, "chamfer", null);

        if (chamfer != null) {
            options.remove("chamfer");
        }

        Body body = new Body(options);

        Vertices.Vertex[] vertices = Vertices.fromPath(path, body);

        if (chamfer != null) {
            vertices = Vertices.chamfer(vertices, chamfer.radius, chamfer.quality, chamfer.qualityMin,
                    chamfer.qualityMax);
        }

        Body.setVertices(body, vertices);
        return body;
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x     x
     * @param y     y
     * @param array vertexSet
     * @return a new body
     */
    static public Body fromVertices(double x, double y, Vector[] array) {
        return Bodies.fromVertices(x, y, array, null);
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x       x
     * @param y       y
     * @param array   vertexSet
     * @param options options
     * @return a new body
     */
    static public Body fromVertices(double x, double y, Vector[] array, HashMap<String, Object> options) {
        return Bodies.fromVertices(x, y, array, options, false, 0.01, 10, 0.01);
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x                     x
     * @param y                     y
     * @param array                 vertexSet
     * @param options               options
     * @param flagInternal          if true marks internal edges with isInternal
     * @param removeCollinear       threshold when simplifying vertices along the
     *                              same edge
     * @param minimumArea           threshold when removing small parts
     * @param removeDuplicatePoints threshold when simplifying nearby vertices
     * @return a new body
     */
    static public Body fromVertices(double x, double y, Vector[] array, HashMap<String, Object> options,
            boolean flagInternal, double removeCollinear, double minimumArea, double removeDuplicatePoints) {
        return fromVertices(x, y, new Vector[][] { array }, options, flagInternal, removeCollinear, minimumArea,
                removeDuplicatePoints);
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x     x
     * @param y     y
     * @param array vertexSets
     * @return a new body
     */
    static public Body fromVertices(double x, double y, Vector[][] array) {
        return Bodies.fromVertices(x, y, array, null);
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x       x
     * @param y       y
     * @param array   vertexSets
     * @param options options
     * @return a new body
     */
    static public Body fromVertices(double x, double y, Vector[][] array, HashMap<String, Object> options) {
        return Bodies.fromVertices(x, y, array, options, false, 0.01, 10, 0.01);
    }

    /**
     * create a compound body based on set(s) of vertices
     * 
     * @param x                     x
     * @param y                     y
     * @param vertexSets            vertexSets
     * @param options               options
     * @param flagInternal          if true marks internal edges with isInternal
     * @param removeCollinear       threshold when simplifying vertices along the
     *                              same edge
     * @param minimumArea           threshold when removing small parts
     * @param removeDuplicatePoints threshold when simplifying nearby vertices
     * @return a new body
     */
    @SuppressWarnings("unchecked")
    static public Body fromVertices(double x, double y, Vector[][] vertexSets, HashMap<String, Object> options,
            boolean flagInternal, double removeCollinear, double minimumArea, double removeDuplicatePoints) {
        boolean canDecomp = false; // decomp module load not implemented yet
        Body body; // init body
        ArrayList<Vector> parts_position = new ArrayList<Vector>();
        ArrayList<Vertex[]> parts_vertices = new ArrayList<Vertex[]>();
        ArrayList<Body> parts = new ArrayList<Body>();
        boolean isConvex, isConcave;
        Vertex[] vertices;

        for (int v = 0; v < vertexSets.length; v++) {
            vertices = new Vertex[vertexSets[v].length];
            int idx = 0;
            for (Vector vertex : vertexSets[v]) {
                vertices[idx] = new Vertex(vertex.x, vertex.y, idx, null);
                idx++;
            }
            isConvex = Vertices.isConvex(vertices);
            isConcave = !isConvex;
            if (isConcave && !canDecomp) {
                System.out.println("Bodies.fromVertices: vertices is concave & decomp not found");
            }

            if (isConvex || !canDecomp) {
                if (isConvex) {
                    vertices = Vertices.clockwiseSort(vertices);
                } else {
                    vertices = Vertices.hull(vertices);
                }

                parts_position.add(new Vector(x, y));
                parts_vertices.add(vertices);
            } else {
                // not implemented yet
            }
        }

        for (int i = 0; i < parts_position.size(); i++) {
            HashMap<String, Object> temopts = (HashMap<String, Object>) options.clone();
            temopts.put("position", parts_position.get(i));
            temopts.put("vertices", parts_vertices.get(i));
            Body tem = Body.create(temopts);
            for (Vertex vertex : tem.vertices) {
                vertex.body = tem;
            }
            parts.add(tem);
        }

        if (flagInternal) {
            double coincident_max_dist = 5;
            for (int i = 0; i < parts.size(); i++) {
                Body partA = parts.get(i);
                for (int j = i + 1; j < parts.size(); j++) {
                    Body partB = parts.get(j);
                    if (Bounds.overlaps(partA.bounds, partB.bounds)) {
                        Vertex[] pav = partA.vertices,
                                pbv = partB.vertices;
                        for (int k = 0; k < pav.length; k++) {
                            for (int z = 0; z < pbv.length; z++) {
                                double da = Vector.magnitudeSquared(Vector.sub(pav[(k + 1) % pav.length], pbv[z])),
                                        db = Vector.magnitudeSquared(Vector.sub(pav[k], pbv[(z + 1) % pbv.length]));
                                if (da < coincident_max_dist && db < coincident_max_dist) {
                                    pav[k].isInternal = true;
                                    pbv[z].isInternal = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (parts.size() > 1) {
            ArrayList<Body> parts_copy = (ArrayList<Body>) parts.clone();
            options.put("parts", parts_copy);
            body = Body.create(options);
            Body.setPosition(body, new Vector(x, y));
            return body;
        } else {
            return parts.get(0);
        }
    }
}