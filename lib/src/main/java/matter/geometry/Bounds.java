package matter.geometry;

/**
 * methods for creating and manipulating axis-aligned bounding boxes
 */
public class Bounds {
    public Vector min;
    public Vector max;

    /**
     * init a Bounds object {min:Vector, max:Vector};
     */
    public Bounds() {
        this.min = new Vector();
        this.max = new Vector();
    }

    /**
     * create a new axis-aligned bounding box
     * 
     * @return a new bounds object
     */
    static public Bounds create() {
        return Bounds.create(null);
    }

    /**
     * create a new axis-aligned bounding box
     * 
     * @param vertices vertices
     * @return a new bounds object
     */
    static public Bounds create(Vertices.Vertex[] vertices) {
        Bounds bounds = new Bounds();
        if (vertices != null)
            Bounds.update(bounds, vertices);
        return bounds;
    }

    /**
     * create a new axis-aligned bounding box
     * 
     * @param vertices vertices
     * @return a new bounds object
     */
    static public Bounds create(Vector[] vertices) {
        Bounds bounds = new Bounds();
        if (vertices != null)
            Bounds.update(bounds, vertices);
        return bounds;
    }

    /**
     * update bounds using the given vertices
     * 
     * @param bounds   bounds
     * @param vertices vertices
     */
    static public void update(Bounds bounds, Vertices.Vertex[] vertices) {
        Bounds.update(bounds, vertices, null);
    }

    /**
     * update bounds using the given vertices
     * 
     * @param bounds   bounds
     * @param vertices vertices
     */
    static public void update(Bounds bounds, Vector[] vertices) {
        Bounds.update(bounds, vertices, null);
    }

    /**
     * update bounds using the given vertices and extends the bounds given a
     * velocity
     * 
     * @param bounds   bounds
     * @param vertices vertices
     * @param velocity velocity
     */
    static public void update(Bounds bounds, Vertices.Vertex[] vertices, Vector velocity) {
        bounds.min.x = Double.MAX_VALUE;
        bounds.max.x = Double.MIN_VALUE;
        bounds.min.y = Double.MAX_VALUE;
        bounds.max.y = Double.MIN_VALUE;
        for (int i = 0; i < vertices.length; i++) {
            Vertices.Vertex vertex = vertices[i];
            if (vertex.x > bounds.max.x)
                bounds.max.x = vertex.x;
            if (vertex.x < bounds.min.x)
                bounds.min.x = vertex.x;
            if (vertex.y > bounds.max.y)
                bounds.max.y = vertex.y;
            if (vertex.y < bounds.min.y)
                bounds.min.y = vertex.y;
        }

        if (velocity != null) {
            if (velocity.x > 0) {
                bounds.max.x += velocity.x;
            } else {
                bounds.min.x += velocity.x;
            }

            if (velocity.y > 0) {
                bounds.max.y += velocity.y;
            } else {
                bounds.min.y += velocity.y;
            }
        }
    }

    /**
     * update bounds using the given vertices and extends the bounds given a
     * velocity
     * 
     * @param bounds   bounds
     * @param vertices vertices
     * @param velocity velocity
     */
    static public void update(Bounds bounds, Vector[] vertices, Vector velocity) {
        bounds.min.x = Double.MAX_VALUE;
        bounds.max.x = Double.MIN_VALUE;
        bounds.min.y = Double.MAX_VALUE;
        bounds.max.y = Double.MIN_VALUE;
        for (int i = 0; i < vertices.length; i++) {
            Vector vertex = vertices[i];
            if (vertex.x > bounds.max.x)
                bounds.max.x = vertex.x;
            if (vertex.x < bounds.min.x)
                bounds.min.x = vertex.x;
            if (vertex.y > bounds.max.y)
                bounds.max.y = vertex.y;
            if (vertex.y < bounds.min.y)
                bounds.min.y = vertex.y;
        }

        if (velocity != null) {
            if (velocity.x > 0) {
                bounds.max.x += velocity.x;
            } else {
                bounds.min.x += velocity.x;
            }

            if (velocity.y > 0) {
                bounds.max.y += velocity.y;
            } else {
                bounds.min.y += velocity.y;
            }
        }
    }

    /**
     * return true if the bounds contains the given point
     * 
     * @param bounds bounds
     * @param point  point
     * @return true if the bounds contains the given point
     */
    static public boolean contains(Bounds bounds, Vector point) {
        return point.x >= bounds.min.x && point.x <= bounds.max.x && point.y >= bounds.min.y && point.y <= bounds.max.y;
    }

    /**
     * return true if the two bounds intersect
     * 
     * @param boundsA boundsA
     * @param boundsB boundsB
     * @return
     */
    static public boolean overlaps(Bounds boundsA, Bounds boundsB) {
        return (boundsA.min.x <= boundsB.max.x && boundsA.max.x >= boundsB.min.x && boundsA.max.y >= boundsB.min.y
                && boundsA.min.y <= boundsB.max.y);
    }

    /**
     * translate the bounds
     * 
     * @param bounds bounds
     * @param vector vector
     */
    static public void translate(Bounds bounds, Vector vector) {
        bounds.min.x += vector.x;
        bounds.max.x += vector.x;
        bounds.min.y += vector.y;
        bounds.max.y += vector.y;
    }

    /**
     * shift the bounds
     * 
     * @param bounds bounds
     * @param position position
     */
    static public void shift(Bounds bounds, Vector position) {
        double deltaX = bounds.max.x - bounds.min.x;
        double deltaY = bounds.max.y - bounds.min.y;
        bounds.min.x = position.x;
        bounds.max.x = position.x + deltaX;
        bounds.min.y = position.y;
        bounds.max.y = position.y + deltaY;
    }
}