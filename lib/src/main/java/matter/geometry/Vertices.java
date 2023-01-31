package matter.geometry;

import matter.body.Body;
import matter.core.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vertices class contains methods for creating and manipulating sets of
 * vertices.
 * 
 * @author JohnC
 */
public class Vertices {

    static public final Pattern pathPattern = Pattern.compile("L?\\s*([-\\d.e]+)[\\s,]*([-\\d.e]+)*",
            Pattern.CASE_INSENSITIVE);

    /**
     * class for chamfer object
     * 
     */
    static public class Chamfer {
        public double[] radius;
        public double quality;
        public double qualityMin;
        public double qualityMax;

        /**
         * create a chamfer object to store the chamfer setting
         * 
         * @param radius radius
         * @param quality quality
         * @param qualityMin qualityMin
         * @param qualityMax qualityMax
         */
        public Chamfer( double[] radius, double quality, double qualityMin, double qualityMax) {
            this.radius = radius.clone();
            this.quality = quality;
            this.qualityMin = qualityMin;
            this.qualityMax = qualityMax;
        }
    }

    /**
     * class for vertex object
     * 
     */
    static public class Vertex extends Vector {
        public double x;
        public double y;
        public int index;
        public Body body;
        public boolean isInternal;

        /**
         * init a vertex
         * 
         * @param x    x
         * @param y    y
         * @param index   index in the vertices group
         * @param body reference to the body
         */
        public Vertex(double x, double y, int index, Body body) {
            this.x = x;
            this.y = y;
            this.index = index;
            this.body = body;
            this.isInternal = false;
        }

        /**
         * return a copy of this Vertex
         * 
         * @return a copy of this Vertex
         */
        public Vertex copy() {
            Vertex n = new Vertex(this.x, this.y, this.index, this.body);
            if (this.isInternal)
                n.isInternal = true;
            return n;
        }

        /**
         * return a copy of a Vertex
         * 
         * @return a copy of a Vertex
         */
        static public Vertex copy(Vertex vertex) {
            return vertex.copy();
        }
    }

    /**
     * create a new set of Body compatible vertices
     * points must be specified in clockwise order
     * 
     * @param points points in clockwise order
     * @param body   body
     * @return the vertices array
     */
    static public Vertices.Vertex[] create(Vector[] points, Body body) {
        Vertex[] vertices = new Vertex[points.length];
        for (int i = 0; i < points.length; i++) {
            Vector point = points[i];
            Vertex vertex = new Vertices.Vertex(point.x, point.y, i, body);
            vertices[i] = vertex;
        }
        return vertices;
    }

    /**
     * parse a string containing ordered x y pairs separated by spaces (and
     * optionally commas)
     * 
     * @param path path
     * @param body body
     * @return the vertices array
     */
    static public Vertices.Vertex[] fromPath(String path, Body body) {
        ArrayList<Vector> points = new ArrayList<Vector>();
        Matcher m = pathPattern.matcher(path);
        while (m.find()) {
            double x = Double.parseDouble(m.group(1));
            double y = Double.parseDouble(m.group(2));
            points.add(new Vector(x, y));
        }
        Vector[] pointsArr = points.toArray(Vector[]::new);
        return Vertices.create(pointsArr, body);
    }

    /**
     * return the centre of the set of vertices
     * 
     * @param vertices vertices
     * @return the centre point
     */
    static public Vector centre(Vertices.Vertex[] vertices) {
        double area = Vertices.area(vertices, true);
        Vector centre = new Vector();
        double cross;
        Vector temp;
        int j;

        for (int i = 0; i < vertices.length; i++) {
            j = (i + 1) % vertices.length;
            cross = Vector.cross(vertices[i], vertices[j]);
            temp = Vector.mult(Vector.add(vertices[i], vertices[j]), cross);
            centre = Vector.add(centre, temp);
        }

        return Vector.div(centre, 6 * area);
    }

    /**
     * return the average of the set of vertices
     * 
     * @param vertices vertices
     * @return the average point
     */
    static public Vector mean(Vertices.Vertex[] vertices) {
        Vector average = new Vector();
        for (int i = 0; i < vertices.length; i++) {
            average.x += vertices[i].x;
            average.y += vertices[i].y;
        }
        return Vector.div(average, vertices.length);
    }

    static public double area(Vertices.Vertex[] vertices) {
        return Vertices.area(vertices, false);
    }

    /**
     * return the area of the set of vertices
     * 
     * @param vertices vertices
     * @param signed   signed
     * @return the area
     */
    static public double area(Vertices.Vertex[] vertices, boolean signed) {
        double area = 0;
        int j = vertices.length - 1;
        for (int i = 0; i < vertices.length; i++) {
            area += (vertices[j].x - vertices[i].x) * (vertices[j].y + vertices[i].y);
            j = i;
        }
        if (signed)
            return area / 2;
        return Math.abs(area) / 2;
    }

    /**
     * return the moment of inertia of the set of vertices given the total mass
     * 
     * @param vertices vertices
     * @param mass     mass
     * @return the polygon's moment of inertia
     */
    static public double inertia(Vertices.Vertex[] vertices, double mass) {
        double numerator = 0;
        double denominator = 0;
        Vertices.Vertex[] v = vertices;
        double cross;
        int j;

        for (int n = 0; n < v.length; n++) {
            j = (n + 1) % v.length;
            cross = Math.abs(Vector.cross(v[j], v[n]));
            numerator += cross * (Vector.dot(v[j], v[j]) + Vector.dot(v[j], v[n]) + Vector.dot(v[n], v[n]));
            denominator += cross;
        }

        return (mass / 6) * (numerator / denominator);
    }

    /**
     * translate the set of vertices
     * 
     * @param vertices vertices
     * @param vector   vector
     * @return the set of vertices after translate
     */
    static public Vertices.Vertex[] translate(Vertices.Vertex[] vertices, Vector vector) {
        return Vertices.translate(vertices, vector, 1);
    }

    /**
     * translate the set of vertices, this method will change the vertives
     * 
     * @param vertices vertices
     * @param vector   vector
     * @param scalar   scalar
     * @return the set of vertices after translate
     */
    static public Vertices.Vertex[] translate(Vertices.Vertex[] vertices, Vector vector, double scalar) {
        double translateX = vector.x * scalar;
        double translateY = vector.y * scalar;
        int i;

        for (i = 0; i < vertices.length; i++) {
            vertices[i].x += translateX;
            vertices[i].y += translateY;
        }

        return vertices;
    }

    /**
     * rotate the set of vertices
     * 
     * @param vertices vertices
     * @param angle    angle
     * @param point    point
     * @return the vertices after rotation
     */
    static public Vector[] rotate(Vertices.Vertex[] vertices, double angle, Vector point) {
        if (angle == 0)
            return vertices;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double pointX = point.x;
        double pointY = point.y;
        Vertices.Vertex vertex;
        double dx;
        double dy;

        for (int i = 0; i < vertices.length; i++) {
            vertex = vertices[i];
            dx = vertex.x - pointX;
            dy = vertex.y - pointY;
            vertex.x = pointX + (dx * cos - dy * sin);
            vertex.y = pointY + (dx * sin + dy * cos);
        }

        return vertices;
    }

    /**
     * return true if the point is inside the set
     * 
     * @param vertices vertices
     * @param point    point
     * @return true if the vertices contain the point
     */
    static public boolean contains(Vertices.Vertex[] vertices, Vector point) {
        double pointX = point.x;
        double pointY = point.y;
        Vertices.Vertex vertex = vertices[vertices.length - 1];
        Vertices.Vertex nextVertex;
        for (int i = 0; i < vertices.length; i++) {
            nextVertex = vertices[i];
            if ((pointX - vertex.x) * (nextVertex.y - vertex.y)
                    + (pointY - vertex.y) * (vertex.x - nextVertex.x) > 0) {
                return false;
            }

            vertex = nextVertex;
        }

        return true;
    }

    /**
     * scale the vertices from its centre
     * 
     * @param vertices vertices
     * @param scaleX   scaleX
     * @param scaleY   scaleY
     * @return the vertices after scale
     */
    static public Vertices.Vertex[] scale(Vertices.Vertex[] vertices, double scaleX, double scaleY) {
        return Vertices.scale(vertices, scaleX, scaleY, null);
    }

    /**
     * scale the vertices from a point
     * 
     * @param vertices vertices
     * @param scaleX   scaleX
     * @param scaleY   scaleY
     * @param point    point
     * @return the vertices after scale
     */
    static public Vertices.Vertex[] scale(Vertices.Vertex[] vertices, double scaleX, double scaleY, Vector point) {
        if (scaleX == 1 && scaleY == 1)
            return vertices;
        if (point == null)
            point = Vertices.centre(vertices);

        Vertices.Vertex vertex;
        Vector delta;

        for (int i = 0; i < vertices.length; i++) {
            vertex = vertices[i];
            delta = Vector.sub(vertex, point);
            vertices[i].x = point.x + delta.x * scaleX;
            vertices[i].y = point.y + delta.y * scaleY;
        }

        return vertices;
    }

    /**
     * chamfer a set of vetices by giving them rounded corners, returns a new set of
     * vertices
     * 
     * @param vertices
     * @return
     */
    static Vertices.Vertex[] chamfer(Vertices.Vertex[] vertices) {
        return Vertices.chamfer(vertices, new double[] { 8 }, -1, 2, 14);
    }

    /**
     * chamfer a set of vetices by giving them rounded corners, returns a new set of
     * vertices
     * 
     * @param vertices
     * @param radius
     * @param quality
     * @param qualityMin
     * @param qualityMax
     * @return
     */
    static public Vertices.Vertex[] chamfer(Vertices.Vertex[] vertices, double[] radius, double quality,
            double qualityMin, double qualityMax) {

        ArrayList<Vertices.Vertex> newVertices = new ArrayList<Vertices.Vertex>();
        Body body = vertices[0].body;
        int idx = 0;
        for (int i = 0; i < vertices.length; i++) {
            Vertices.Vertex prevVertex = vertices[i - 1 >= 0 ? i - 1 : vertices.length - 1];
            Vertices.Vertex vertex = vertices[i];
            Vertices.Vertex nextVertex = vertices[(i + 1) % vertices.length];
            double currentRadius = radius[i < radius.length ? i : radius.length - 1];

            if (currentRadius == 0) {
                newVertices.add(new Vertices.Vertex(vertex.x, vertex.y, idx, body));
                idx++;
                continue;
            }

            Vector prevNormal = Vector.normalise(new Vector(vertex.y - prevVertex.y, prevVertex.x - vertex.x));
            Vector nextNormal = Vector.normalise(new Vector(nextVertex.y - vertex.y, vertex.x - nextVertex.x));

            double diagonalRadius = Math.sqrt(2 * Math.pow(currentRadius, 2));
            Vector radiusVector = Vector.mult(Vector.copy(prevNormal), currentRadius);
            Vector midNormal = Vector.normalise(Vector.mult(Vector.add(prevNormal, nextNormal), 0.5));
            Vector scaledVertex = Vector.sub(vertex, Vector.mult(midNormal, diagonalRadius));

            double precision = quality;

            if (quality == -1) {
                precision = Math.pow(currentRadius, 0.32) * 1.75;
            }

            precision = Common.clamp(precision, qualityMin, qualityMax);
            precision = Math.round(precision);

            if (precision % 2 == 1) {
                precision += 1;
            }

            double alpha = Math.acos(Vector.dot(prevNormal, nextNormal));
            double theta = alpha / precision;

            for (int j = 0; j < precision; j++) {
                Vector temv = Vector.add(Vector.rotate(radiusVector, theta * j), scaledVertex);
                Vertices.Vertex tem = new Vertex(temv.x, temv.y, idx, body);
                newVertices.add(tem);
                idx++;
            }
        }

        return newVertices.toArray(Vertices.Vertex[]::new);
    }

    /**
     * sort the input vertices into clockwise order in place
     * 
     * @param vertices vertices
     * @return vertices
     */
    static public Vertices.Vertex[] clockwiseSort(Vertices.Vertex[] vertices) {
        Vector centre = Vertices.mean(vertices);

        Arrays.sort(vertices, (vertexA, vertexB) -> {
            return Vector.angle(centre, vertexA) - Vector.angle(centre, vertexB) > 0 ? 1 : -1;
        });

        return vertices;
    }

    /**
     * return true if the vertices form a convex shape
     * 
     * @param vertices
     * @return
     */
    static public boolean isConvex(Vertices.Vertex[] vertices) {
        int flag = 0;
        int n = vertices.length;
        int i;
        int j;
        int k;
        double z;

        if (n < 3)
            return false;

        for (i = 0; i < n; i++) {
            j = (i + 1) % n;
            k = (i + 2) % n;
            z = (vertices[j].x - vertices[i].x) * (vertices[k].y - vertices[j].y);
            z -= (vertices[j].y - vertices[i].y) * (vertices[k].x - vertices[j].x);

            if (z < 0) {
                flag |= 1;
            } else if (z > 0) {
                flag |= 2;
            }

            if (flag == 3) {
                return false;
            }
        }

        if (flag != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * return the conver hull of the vertices
     * 
     * @param vertices
     * @return
     */
    static public Vertices.Vertex[] hull(Vertices.Vertex[] vertices) {
        ArrayList<Vertices.Vertex> upper = new ArrayList<Vertices.Vertex>();
        ArrayList<Vertices.Vertex> lower = new ArrayList<Vertices.Vertex>();
        Vertices.Vertex vertex;

        vertices = Arrays.copyOf(vertices, vertices.length);
        Arrays.sort(vertices, (vertexA, vertexB) -> {
            double dx = vertexA.x - vertexB.x;
            return dx != 0 ? (dx > 0 ? 1 : -1)
                    : (vertexA.y - vertexB.y == 0 ? 0 : (vertexA.y - vertexB.y > 0 ? 1 : -1));
        });

        for (int i = 0; i < vertices.length; i++) {
            vertex = vertices[i];

            while (lower.size() >= 2
                    && Vector.cross3(lower.get(lower.size() - 2), lower.get(lower.size() - 1), vertex) <= 0) {
                lower.remove(lower.size() - 1);
            }

            lower.add(vertex);
        }

        for (int i = vertices.length - 1; i >= 0; i--) {
            vertex = vertices[i];
            while (upper.size() >= 2
                    && Vector.cross3(upper.get(upper.size() - 2), upper.get(upper.size() - 1), vertex) <= 0) {
                upper.remove(upper.size() - 1);
            }

            upper.add(vertex);
        }

        upper.remove(upper.size() - 1);
        lower.remove(lower.size() - 1);
        upper.addAll(lower);
        return upper.toArray(Vertices.Vertex[]::new);
    }
}
