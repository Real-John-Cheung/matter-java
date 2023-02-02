package matter.collision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import matter.body.Body;
import matter.geometry.Bounds;
import matter.core.Common;

/**
 * methods for efficiently detecting collisions between a list of bodies
 * @author JohnC
 */
public class Detector {

    static public Detector create(HashMap<String, Object> options) {
        return new Detector(options);
    }

    /**
     * array of `Matter.Body` between which the detector finds collisions
     */
    public ArrayList<Body> bodies;

    /**
     * a pairs object from which previous collision objects may be reused
     */
    public Pairs pairs;

    @SuppressWarnings("unchecked")
    public Detector(HashMap<String, Object> options) {
        this.bodies = (ArrayList<Body>) Common.parseOption(options, "bodies", new ArrayList<Body>());
        this.pairs = (Pairs) Common.parseOption(options, "pairs", null);
    }

    /**
     * set the list of bodies in the detector
     * 
     * @param detector detector
     * @param bodies bodies
     */
    static public void setBodies(Detector detector, Body[] bodies) {
        detector.bodies.clear();
        detector.bodies.addAll(Arrays.asList(bodies));
    }

    /**
     * clear the detector
     * 
     * @param detector detector
     */
    static public void clear(Detector detector) {
        detector.bodies.clear();
    }

    /**
     * finds all collisions among all the bodies
     * 
     * @param detector detector
     * @return collisions
     */
    static public Collision[] collisions(Detector detector) {
        ArrayList<Collision> collisions = new ArrayList<Collision>();
        Pairs pairs = detector.pairs;
        ArrayList<Body> bodies = detector.bodies;
        int bodiesLength = bodies.size();
        //cancollide;
        // collides;

        bodies.sort(Detector._compareBoundsX);

        for (int i = 0; i < bodiesLength; i++) {
            Body bodyA = bodies.get(i);
            //Bounds boundsA = bodyA.bounds;
            double boundXMax = bodyA.bounds.max.x,
                    boundYMax = bodyA.bounds.max.y,
                    boundYMin = bodyA.bounds.min.y;
            boolean bodyAStatic = bodyA.isStatic || bodyA.isSleeping;
            int partsALength = bodyA.parts.size();
            boolean partsASingle = partsALength == 1;

            for (int j = i + 1; j < bodiesLength; j++) {
                Body bodyB = bodies.get(j);
                Bounds boundsB = bodyB.bounds;
                if (boundsB.min.x > boundXMax) {
                    break;
                }
                if (boundYMax < boundsB.min.y || boundYMin > boundsB.max.y) {
                    continue;
                }

                if (bodyAStatic && (bodyB.isStatic || bodyB.isSleeping)) {
                    continue;
                }

                if (!Detector.canCollide(bodyA.collisionFilter, bodyB.collisionFilter)) {
                    continue;
                }
                int partsBLength = bodyB.parts.size();
                if (partsASingle && partsBLength == 1) {
                    Collision collision = Collision.collides(bodyA, bodyB, pairs);

                    if (collision != null) {
                        collisions.add(collision);
                    }
                } else {
                    int partsAStart = partsALength > 1 ? 1 : 0,
                            partsBStart = partsBLength > 1 ? 1 : 0;
                    for (int k = partsAStart; k < partsALength; k++) {
                        Body partA = bodyA.parts.get(k);
                        Bounds boundsAA = partA.bounds;
                        for (int z = partsBStart; z < partsBLength; z++) {
                            Body partB = bodyB.parts.get(z);
                            Bounds boundsBB = partB.bounds;

                            if (boundsAA.min.x > boundsBB.max.x || boundsAA.max.x < boundsBB.min.x
                                    || boundsAA.max.y < boundsBB.min.y || boundsAA.min.y > boundsBB.max.y) {
                                continue;
                            }
                            Collision collision = Collision.collides(partA, partB, pairs);
                            if (collision != null) {
                                collisions.add(collision);
                            }
                        }
                    }
                }
            }
        }

        return collisions.toArray(Collision[]::new);
    }

    /**
     * return true if both supplied collision filters will allow a collision to occur
     * 
     * @param filterA filterA
     * @param filterB filterB
     * @return true if collision can occur
     */
    static public boolean canCollide(Collision.CollisionFilter filterA, Collision.CollisionFilter filterB) {
        if (filterA.group == filterB.group && filterA.group != 0) {
            return filterA.group > 0;
        }
        return (filterA.mask & filterB.category) != 0 && (filterB.mask & filterA.category) != 0;
    }

    /**
     * return the signed delta of the bodies bounds on the x-axis
     * 
     * @param bodyA bodyA
     * @param bodyB bodyB
     * @return the signed delta in int
     */
    static private int _compareBoundsX(Body bodyA, Body bodyB) {
        return bodyA.bounds.min.x - bodyB.bounds.min.x > 0 ? 1 : (bodyA.bounds.min.x - bodyB.bounds.min.x < 0 ? -1 : 0);
    }

    static final private Comparator<Body> _compareBoundsX = (bodyA, bodyB) -> {
        return Detector._compareBoundsX(bodyA, bodyB);
    };
}