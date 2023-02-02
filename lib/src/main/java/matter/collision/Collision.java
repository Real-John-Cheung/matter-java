package matter.collision;

import java.util.ArrayList;
import java.util.HashMap;

import matter.core.Common;
import matter.body.Body;
import matter.geometry.Vertices.Vertex;
import matter.geometry.Vector;
import matter.geometry.Vertices;

/**
 * @author JohnC
 */
public class Collision {

    private static Vertex[] _supports = new Vertex[2];
    private static HashMap<String, Object> _overlapAB = Common.opts("overlap", 0d, "axis", null);
    private static HashMap<String, Object> _overlapBA = Common.opts("overlap", 0d, "axis", null);

    /**
     * class for collisionFilter object
     * 
     * @see Body
     */
    static public class CollisionFilter {
        /**
         * an int that specifies the collision group this body belongs to.
         */
        public int group;
        /**
         * a bit field that specifies the collision category this body belongs to
         */
        public byte category;
        /**
         * a bit mask that specifies the collision categories this body may collide with
         */
        public int mask;

        public CollisionFilter() {
            this(null);
        }

        public CollisionFilter(HashMap<String, Object> options) {
            this.category = (Byte) Common.parseOption(options, "category", (byte) 1);
            this.mask = (Integer) Common.parseOption(options, "mask", 0xFFFFFFFF);
            this.group = (Integer) Common.parseOption(options, "group", 0);
        }
    }

    /**
     * the pair using this collision record, if there is one
     */
    public Pair pair;

    /**
     * indicate if the bodies were colliding when the collision was last updated
     */
    public boolean collided;

    /**
     * first body part represented by the collision
     */
    public Body bodyA;

    /**
     * second body part represented by the collision
     */
    public Body bodyB;

    /**
     * first body represented by the collision
     */
    public Body parentA;

    /**
     * second body represented by the collision
     */
    public Body parentB;

    /**
     * represents the minimum separating distance between the bodies
     */
    public double depth;

    /**
     * represents the direction between the bodies that provides the minimum
     * separating distance
     */
    public Vector normal;

    /**
     * the tangent direction to the collision normal
     */
    public Vector tangent;

    /**
     * the direction and depth of the collision
     */
    public Vector penetration;

    /**
     * support points in the collision, the deepest vertices (along the collision
     * normal) of each body that are contained by the other body's vertices
     */
    public ArrayList<Vertex> supports;

    /**
     * create a new collision
     * 
     * @param bodyA bodyA the first body part
     * @param bodyB bodyB the first body part
     */
    public Collision(Body bodyA, Body bodyB) {
        this.pair = null;
        this.collided = false;
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.parentA = bodyA.parent;
        this.parentB = bodyB.parent;
        this.depth = 0;
        this.normal = new Vector();
        this.tangent = new Vector();
        this.penetration = new Vector();
        this.supports = new ArrayList<>();
    }

    /**
     * create a new collision
     * 
     * @param bodyA bodyA the first body part
     * @param bodyB bodyB the first body part
     * @return the collision object
     */
    static public Collision create(Body bodyA, Body bodyB) {
        return new Collision(bodyA, bodyB);
    }

    /**
     * detect collision between two bodies
     * 
     * @param bodyA bodyA
     * @param bodyB bodyB
     * @return a collision record if detected, otherwise null
     */
    static public Collision collides(Body bodyA, Body bodyB) {
        return Collision.collides(bodyA, bodyB, null);
    }

    /**
     * detect collision between two bodies
     * 
     * @param bodyA bodyA
     * @param bodyB bodyB
     * @param pairs  reuse collision records from existing pairs
     * @return a collision record if detected, otherwise null
     */
    static public Collision collides(Body bodyA, Body bodyB, Pairs pairs) {
        Collision._overlapAxes(_overlapAB, bodyA.vertices, bodyB.vertices, bodyA.axes);

        if ((Double) _overlapAB.get("overlap") <= 0) {
            return null;
        }

        Collision._overlapAxes(_overlapBA, bodyB.vertices, bodyA.vertices, bodyB.axes);

        if ((Double) _overlapBA.get("overlap") <= 0) {
            return null;
        }

        Collision collision;
        Pair pair = null;
        if (pairs != null && pairs.table.get(Pair.id(bodyA, bodyB)) != null) {
            pair = pairs.table.get(Pair.id(bodyA, bodyB));
        }

        if (pair == null) {
            collision = Collision.create(bodyA, bodyB);
            collision.collided = true;
            collision.bodyA = bodyA.id < bodyB.id ? bodyA : bodyB;
            collision.bodyB = bodyA.id < bodyB.id ? bodyB : bodyA;
            collision.parentA = collision.bodyA.parent;
            collision.parentB = collision.bodyB.parent;
        } else {
            collision = pair.collision;
        }

        bodyA = collision.bodyA;
        bodyB = collision.bodyB;

        HashMap<String, Object> minOverlap;
        double temAB = ((Double) _overlapAB.get("overlap"));
        double temBA = ((Double) _overlapBA.get("overlap"));
        if (temAB < temBA) {
            minOverlap = _overlapAB;
        } else {
            minOverlap = _overlapBA;
        }

        Vector normal = collision.normal;
        ArrayList<Vertex> supports = collision.supports;
        Vector minAxis = (Vector) minOverlap.get("axis");
        double minAxisX = minAxis.x;
        double minAxisY = minAxis.y;

        if (minAxisX * (bodyB.position.x - bodyA.position.x) + minAxisY * (bodyB.position.y - bodyA.position.y) < 0) {
            normal.x = minAxisX;
            normal.y = minAxisY;
        } else {
            normal.x = -minAxisX;
            normal.y = -minAxisY;
        }

        collision.tangent.x = -normal.y;
        collision.tangent.y = normal.x;

        collision.depth = (Double) minOverlap.get("overlap");

        collision.penetration.x = normal.x * collision.depth;
        collision.penetration.y = normal.y * collision.depth;

        Vertex[] supportsB = Collision._findSupports(bodyA, bodyB, normal, 1);
        int supportCount = 0;
        if (Vertices.contains(bodyA.vertices, supportsB[1])) {
            while (supports.size() < supportCount + 1) {
                supports.add(null);
            }
            supports.set(supportCount++, supportsB[0]);
        }

        if (Vertices.contains(bodyA.vertices, supportsB[1])) {
            while (supports.size() < supportCount + 1) {
                supports.add(null);
            }
            supports.set(supportCount++, supportsB[1]);
        }

        if (supportCount < 2) {
            var supportsA = Collision._findSupports(bodyB, bodyA, normal, -1);

            if (Vertices.contains(bodyB.vertices, supportsA[0])) {
                while (supports.size() < supportCount + 1) {
                    supports.add(null);
                }
                supports.set(supportCount++, supportsA[0]);
            }

            if (supportCount < 2 && Vertices.contains(bodyB.vertices, supportsA[1])) {
                while (supports.size() < supportCount + 1) {
                    supports.add(null);
                }
                supports.set(supportCount++, supportsA[1]);
            }
        }

        if (supportCount == 0) {
            while (supports.size() < supportCount + 1) {
                supports.add(null);
            }
            supports.set(supportCount++, supportsB[0]);
        }

        while (supports.size() > supportCount) {
            supports.remove(supports.size() - 1);
        }

        while (supports.size() < supportCount + 1) {
            supports.add(null);
        }

        return collision;
    }

    /**
     * find the overlap between two sets of vertices
     * 
     * @param result    result to be put in
     * @param verticesA verticesA
     * @param verticesB verticesB
     * @param axes      axes
     */
    static private void _overlapAxes(HashMap<String, Object> result, Vertex[] verticesA, Vertex[] verticesB,
            Vector[] axes) {
        int verticesALength = verticesA.length, verticesBLength = verticesB.length, axesLength = axes.length;
        double verticesAX = verticesA[0].x,
                verticesAY = verticesA[0].y,
                verticesBX = verticesB[0].x,
                verticesBY = verticesB[0].y,
                overlapMin = Double.MAX_VALUE,
                dot, overlapAB, overlapBA, overlap;
        int overlapAxisNumber = 0;

        for (int i = 0; i < axesLength; i++) {
            Vector axis = axes[i];
            double axisX = axis.x,
                    axisY = axis.y,
                    minA = verticesAX * axisX + verticesAY * axisY,
                    minB = verticesBX * axisX + verticesBY * axisY,
                    maxA = minA,
                    maxB = minB;
            for (int j = 0; j < verticesALength; j++) {
                dot = verticesA[j].x * axisX + verticesA[j].y * axisY;

                if (dot > maxA) {
                    maxA = dot;
                } else if (dot < minA) {
                    minA = dot;
                }
            }

            for (int j = 0; j < verticesBLength; j++) {
                dot = verticesB[j].x * axisX + verticesB[j].y * axisY;

                if (dot > maxB) {
                    maxB = dot;
                } else if (dot < minB) {
                    minB = dot;
                }
            }

            overlapAB = maxA - minB;
            overlapBA = maxB - minA;
            overlap = overlapAB < overlapBA ? overlapAB : overlapBA;

            if (overlap < overlapMin) {
                overlapMin = overlap;
                overlapAxisNumber = i;

                if (overlap <= 0) {
                    break;
                }
            }
        }

        result.put("axis", axes[overlapAxisNumber]);
        result.put("overlap", overlapMin);
    }

    /**
     * project vertices on an axis and returns an interval
     * 
     * @param projection projection [0]: min [1]: max
     * @param vertices   vertices
     * @param axis       axis
     */
    static private void _projectToAxis(double[] projection, Vertex[] vertices, Vector axis) {
        double min = vertices[0].x * axis.x + vertices[0].y * axis.y,
                max = min;
        for (int i = 0; i < vertices.length; i++) {
            double dot = vertices[i].x * axis.x + vertices[i].y * axis.y;
            if (dot > max) {
                max = dot;
            } else if (dot < min) {
                min = dot;
            }
        }
        projection[0] = min;
        projection[1] = max;
    }

    static private Vertex[] _findSupports(Body bodyA, Body bodyB, Vector normal, double direction) {
        Vertex[] vertices = bodyB.vertices;
        int verticesLength = vertices.length;
        double bodyAPositionX = bodyA.position.x,
                bodyAPositionY = bodyA.position.y,
                normalX = normal.x * direction,
                normalY = normal.y * direction,
                nearestDistance = Double.MAX_VALUE,
                distance;
        Vertex vertexA = new Vertex(0, 0, 0, null);
        Vertex vertexB;
        Vertex vertexC;
        for (int j = 0; j < verticesLength; j++) {
            vertexB = vertices[j];
            distance = normalX * (bodyAPositionX - vertexB.x) + normalY * (bodyAPositionY - vertexB.y);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                vertexA = vertexB;
            }
        }

        vertexC = vertices[(verticesLength + vertexA.index - 1) % verticesLength];
        nearestDistance = normalX * (bodyAPositionX - vertexC.x) + normalY * (bodyAPositionY - vertexC.y);
        vertexB = vertices[(vertexA.index + 1) % verticesLength];
        if (normalX * (bodyAPositionX - vertexB.x) + normalY * (bodyAPositionY - vertexB.y) < nearestDistance) {
            _supports[0] = vertexA;
            _supports[1] = vertexB;
            return _supports.clone();
        }
        _supports[0] = vertexA;
        _supports[1] = vertexC;
        return _supports.clone();
    }
}