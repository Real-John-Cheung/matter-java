package matter.collision;

import java.util.ArrayList;

import matter.core.Common;
import matter.body.Body;
import matter.geometry.Bounds;
import matter.geometry.Vector;
import matter.geometry.Vertices;
import matter.factory.Bodies;

/**
 * 
 * methods for performing collision queries
 * 
 * @author JohnC
 */
public class Query {

    /**
     * return a list of collisions between body and bodies
     * 
     * @param body   body
     * @param bodies bodies
     * @return collisions
     */
    static public Collision[] collides(Body body, Body[] bodies) {
        ArrayList<Collision> collisions = new ArrayList<Collision>();
        int bodiesLength = bodies.length;
        Bounds bounds = body.bounds;
        // collides = Collision.collides
        // overlaps = Bounds.overlaps

        for (int i = 0; i < bodiesLength; i++) {
            Body bodyA = bodies[i];
            int partsALength = bodyA.parts.size();
            int partsAStart = partsALength == 1 ? 0 : 1;
            if (Bounds.overlaps(bodyA.bounds, bounds)) {
                for (int j = partsAStart; j < partsALength; j++) {
                    Body part = bodyA.parts.get(j);
                    if (Bounds.overlaps(part.bounds, bounds)) {
                        Collision collision = Collision.collides(part, body);
                        if (collision != null) {
                            collisions.add(collision);
                            break;
                        }
                    }
                }
            }
        }

        return collisions.toArray(Collision[]::new);
    }

    /**
     * cast a ray segment against a set of bodies and returns all collisions
     * 
     * @param bodies     bodies
     * @param startPoint start point to define the ray
     * @param endPoint   end point to define the ray
     * @return collisions
     */
    static public Collision[] ray(Body[] bodies, Vector startPoint, Vector endPoint) {
        return Query.ray(bodies, startPoint, endPoint, 1e-100);
    }

    /**
     * cast a ray segment against a set of bodies and returns all collisions
     * 
     * @param bodies bodies
     * @param startPoint start point to define the ray
     * @param endPoint end point to define the ray
     * @param rayWidth ray width
     * @return collisions
     */
    static public Collision[] ray(Body[] bodies, Vector startPoint, Vector endPoint, double rayWidth) {
        double rayAngle = Vector.angle(startPoint, endPoint),
                rayLength = Vector.magnitude(Vector.sub(startPoint, endPoint)),
                rayX = (endPoint.x + startPoint.x) * 0.5,
                rayY = (endPoint.y + startPoint.y) * 0.5;
        Body ray = Bodies.rectangle(rayX, rayY, rayLength, rayWidth, Common.opts("angle", rayAngle));
        Collision[] collisions = Query.collides(ray, bodies);

        for (int i = 0; i < collisions.length; i++) {
            Collision collision = collisions[i];
            collision.bodyB = collision.bodyA;
        }

        return collisions;
    }

    /**
     * return all bodies whose bounds are inside the given set of bounds
     * 
     * @param bodies bodies
     * @param bounds bounds
     * @return the bodies matching the query
     */
    static public Body[] region(Body[] bodies, Bounds bounds) {
        return Query.region(bodies, bounds, false);
    }

    /**
     * return all bodies whose bounds are inside (or outside if set) the given set of bounds
     * 
     * @param bodies bodies
     * @param bounds bounds
     * @param outside if true, return the bodies that's outside the bounds
     * @return the bodies matching the query
     */
    static public Body[] region(Body[] bodies, Bounds bounds, boolean outside) {
        ArrayList<Body> result = new ArrayList<Body>();
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            boolean overlaps = Bounds.overlaps(body.bounds, bounds);
            if ((overlaps && !outside) || (!overlaps && outside))
                result.add(body);
        }
        return result.toArray(Body[]::new);
    }

    /**
     * return all bodies whose vertices contain the given point
     * 
     * @param bodies bodies
     * @param point point 
     * @return the bodies matching the query
     */
    static public Body[] point(Body[] bodies, Vector point) {
        ArrayList<Body> result = new ArrayList<Body>();

        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];

            if (Bounds.contains(body.bounds, point)) {
                for (int j = body.parts.size() == 1 ? 0 : 1; j < body.parts.size(); j++) {
                    Body part = body.parts.get(j);
                    if (Bounds.contains(part.bounds, point) && Vertices.contains(part.vertices, point)) {
                        result.add(body);
                        break;
                    }
                }
            }
        }
        return result.toArray(Body[]::new);
    }
}