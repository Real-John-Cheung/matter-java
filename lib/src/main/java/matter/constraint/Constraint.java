package matter.constraint;

import java.util.HashMap;

import matter.core.Common;
import matter.body.Body;
import matter.geometry.Vector;
import matter.geometry.Vertices;
import matter.geometry.Axes;
import matter.geometry.Bounds;
import matter.core.Sleeping;

/**
 * contains methods for creating and manipulating constraints
 * @author JohnC
 */

public class Constraint {
    static double _warming = 0.4;
    static double _torqueDampen = 1;
    static double _minLength = 0.000001;

    /**
     * a integer uniquely identifying number generated in by Common.nextId
     */
    public int id;

    /**
     * the type of object
     */
    public String type;

    /**
     * name to help the user identify and manage constraint
     */
    public String label;

    //public Object render

    /**
     * first possible Body that this constraint is attached to
     */
    public Body bodyA;

    /**
     * second possible Body that this constraint is attached to
     */
    public Body bodyB;

    /**
     * specify the offset of the constraint from center of the bodyA if defined, otherwise a world-space position
     */
    public Vector pointA;

    /**
     * specify the offset of the constraint from center of the bodyB if defined, otherwise a world-space position
     */
    public Vector pointB;

    /**
     * specify the stiffness of the constraint, i.e. the rate at which it returns to its resting length
     */
    public double stiffness;

    /**
     * specify the damping of the constraint
     */
    public double damping;

    /**
     * the target resting length of the constraint
     */
    public double length;

    /**
     * object reserved for storing plugin-specific properties
     */
    public HashMap<String, Object> plugin;

    /**
     * angularStiffness
     */
    public double angularStiffness;

    /**
     * angle A
     */
    public double angleA;

    /**
     * angle B
     */
    public double angleB;

    /**
     * create a constraint
     */
    public Constraint() {
        this(null);
    }

    /**
     * create a new constraint
     * see the fields of Constraint class to see what can be pass in options
     * 
     * @param options options
     */
    @SuppressWarnings("unchecked")
    public Constraint(HashMap<String, Object> options) {
        this.bodyA = (Body) options.getOrDefault("bodyA", null);
        this.bodyB = (Body) options.getOrDefault("bodyA", null);
        this.id = (Integer) options.getOrDefault("id", Common.nextId());
        this.type = "constraint";
        this.label = (String) options.getOrDefault("label", "Constraint");
        this.pointA = (Vector) options.getOrDefault("pointA", null);
        this.pointB = (Vector) options.getOrDefault("pointB", null);
        this.damping = (Double) options.getOrDefault("damping", 0);
        this.plugin = (HashMap<String, Object>) options.getOrDefault("plugin", null);

        if (this.bodyA != null && this.pointA == null) {
            this.pointA = new Vector();
        }

        if (this.bodyB != null && this.pointB == null) {
            this.pointB = new Vector();
        }

        Vector initialPointA = this.bodyA != null ? Vector.add(this.bodyA.position, this.pointA) : this.pointA;
        Vector initialPointB = this.bodyB != null ? Vector.add(this.bodyB.position, this.pointB) : this.pointB;
        double length = Vector.magnitude(Vector.sub(initialPointA, initialPointB));
        this.length = (Double) options.getOrDefault("length", length);
        this.stiffness = (Double) options.getOrDefault("stiffness", this.length > 0 ? 1 : 0.7);
        this.angularStiffness = (Double) options.getOrDefault("angularStiffness", 0);
        this.angleA = this.bodyA != null ? this.bodyA.angle : (Double) options.getOrDefault("angleA", 0);
        this.angleB = this.bodyB != null ? this.bodyB.angle : (Double) options.getOrDefault("angleB", 0);
    }
    
    /**
     * create a new constraint
     * 
     * @return a constraint
     */
    static public Constraint create() {
        return Constraint.create(null);
    }

    /**
     * create a new constraint
     * 
     * @param options options
     * @return a constraint
     */
    static public Constraint create(HashMap<String, Object> options) {
        return new Constraint(options);
    }

    /**
     * constraint warming
     * 
     * @param bodies bodies
     */
    static public void preSolveAll(Body[] bodies) {
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            double[] impulse = body.constraintImpulse; // x, y, angle

            if (body.isStatic || (impulse[0] == 0 && impulse[1] == 0 && impulse[2] == 0)) {
                continue;
            }

            body.position.x += impulse[0];
            body.position.y += impulse[1];
            body.angle += impulse[2];
        }
    }

    /**
     * solve all constraints in a list
     * 
     * @param constraints constraints
     * @param timeScale timeScale
     */
    static public void solveAll(Constraint[] constraints, double timeScale) {
        for (int i = 0; i < constraints.length; i++) {
            Constraint constraint = constraints[i];
            boolean fixedA = constraint.bodyA == null || (constraint.bodyA != null && constraint.bodyA.isStatic);
            boolean fixedB = constraint.bodyB == null || (constraint.bodyB != null && constraint.bodyB.isStatic);

            if (fixedA || fixedB) {
                Constraint.solve(constraints[i], timeScale);
            }
        }

        for (int i = 0; i < constraints.length; i++) {
            Constraint constraint = constraints[i];
            boolean fixedA = constraint.bodyA == null || (constraint.bodyA != null && constraint.bodyA.isStatic);
            boolean fixedB = constraint.bodyB == null || (constraint.bodyB != null && constraint.bodyB.isStatic);
            if (!fixedA && !fixedB) {
                Constraint.solve(constraints[i], timeScale);
            }
        }
    }

    /**
     * solve a distance constraint 
     * 
     * @param constraint constraint
     * @param timeScale timeScale
     */
    static public void solve(Constraint constraint, double timeScale) {
        Body bodyA = constraint.bodyA;
        Body bodyB = constraint.bodyB;
        Vector pointA = constraint.pointA;
        Vector pointB = constraint.pointB;

        if (bodyA == null && bodyB == null)
            return;

        if (bodyA != null && !bodyA.isStatic) {
            Vector.rotate(pointA, bodyA.angle - constraint.angleA, pointA);
            constraint.angleA = bodyA.angle;
        }

        if (bodyB != null && !bodyB.isStatic) {
            Vector.rotate(pointB, bodyB.angle - constraint.angleB, pointB);
            constraint.angleB = bodyB.angle;
        }

        Vector pointAWorld = pointA, pointBWorld = pointB;

        if (bodyA != null)
            pointAWorld = Vector.add(bodyA.position, pointA);
        if (bodyB != null)
            pointBWorld = Vector.add(bodyB.position, pointB);

        if (pointAWorld == null || pointBWorld == null)
            return;

        Vector delta = Vector.sub(pointAWorld, pointBWorld);
        double currentLength = Vector.magnitude(delta);

        if (currentLength < Constraint._minLength) {
            currentLength = Constraint._minLength;
        }

        double difference = (currentLength - constraint.length) / currentLength,
                stiffness = constraint.stiffness < 1 ? constraint.stiffness * timeScale : constraint.stiffness;
        Vector force = Vector.mult(delta, difference * stiffness);
        double massTotal = (bodyA != null ? bodyA.inverseMass : 0) + (bodyB != null ? bodyB.inverseMass : 0);
        double inertiaTotal = (bodyA != null ? bodyA.inverseInertia : 0) + (bodyB != null ? bodyB.inverseInertia : 0);
        double resistanceTotal = massTotal + inertiaTotal;

        double torque;
        double share;
        Vector normal = new Vector();
        double normalVelocity = 0;
        Vector relativeVelocity;

        if (constraint.damping != 0) {
            Vector zero = new Vector();
            normal = Vector.div(delta, currentLength);
            Vector s1 = bodyB != null ? Vector.sub(bodyB.position, bodyB.positionPrev) : zero;
            Vector s2 = bodyA != null ? Vector.sub(bodyA.position, bodyA.positionPrev) : zero;
            relativeVelocity = Vector.sub(s1, s2);

            normalVelocity = Vector.dot(normal, relativeVelocity);
        }

        if (bodyA != null && !bodyA.isStatic) {
            share = bodyA.inverseMass / massTotal;

            bodyA.constraintImpulse[0] -= force.x * share;
            bodyA.constraintImpulse[1] -= force.y * share;

            bodyA.position.x -= force.x * share;
            bodyA.position.y -= force.y * share;

            if (constraint.damping != 0) {
                bodyA.positionPrev.x -= constraint.damping * normal.x * normalVelocity * share;
                bodyA.positionPrev.y -= constraint.damping * normal.y * normalVelocity * share;
            }

            torque = (Vector.cross(pointA, force) / resistanceTotal) * Constraint._torqueDampen * bodyA.inverseInertia
                    * (1 - constraint.angularStiffness);
            bodyA.constraintImpulse[2] -= torque;
            bodyA.angle -= torque;
        }

        if (bodyB != null && !bodyB.isStatic) {
            share = bodyB.inverseMass / massTotal;
            bodyB.constraintImpulse[0] += force.x * share;
            bodyB.constraintImpulse[1] += force.y * share;
            bodyB.position.x += force.x * share;
            bodyB.position.y += force.y * share;
            if (constraint.damping != 0) {
                bodyB.positionPrev.x += constraint.damping * normal.x * normalVelocity * share;
                bodyB.positionPrev.y += constraint.damping * normal.y * normalVelocity * share;
            }
            torque = (Vector.cross(pointB, force) / resistanceTotal) * Constraint._torqueDampen * bodyB.inverseInertia
                    * (1 - constraint.angularStiffness);
            bodyB.constraintImpulse[2] += torque;
            bodyB.angle += torque;
        }
    }
    
    /**
     * perform body updates required after solving constraints
     * 
     * @param bodies bodies
     */
    static public void postSolveAll(Body[] bodies) {
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            double[] impulse = body.constraintImpulse;
            if (body.isStatic || (impulse[0] == 0 && impulse[1] == 0 && impulse[2] == 0)) {
                continue;
            }

            Sleeping.set(body, false);

            for (int j = 0; j < body.parts.size(); j++) {
                Body part = body.parts.get(j);

                Vertices.translate(part.vertices, new Vector(impulse[0], impulse[1]));

                if (j > 0) {
                    part.position.x += impulse[0];
                    part.position.y += impulse[1];
                }

                if (impulse[2] != 0) {
                    Vertices.rotate(part.vertices, impulse[2], body.position);
                    Axes.rotate(part.axes, impulse[2]);
                    if (j > 0) {
                        Vector.rotateAbout(part.position, impulse[2], body.position, part.position);
                    }
                }

                Bounds.update(part.bounds, part.vertices, body.velocity);
            }

            impulse[2] *= Constraint._warming;
            impulse[0] *= Constraint._warming;
            impulse[1] *= Constraint._warming;
        }
    }

    /**
     * return the world-space position of pointA
     * 
     * @param constraint constraint
     * @return the world-space position
     */
    static public Vector pointAWorld(Constraint constraint) {
        double x = (constraint.bodyA != null ? constraint.bodyA.position.x : 0)
                + (constraint.pointA != null ? constraint.pointA.x : 0);
        double y = (constraint.bodyA != null ? constraint.bodyA.position.y : 0)
                + (constraint.pointA != null ? constraint.pointA.y : 0);
        return new Vector(x, y);
    }

    /**
     * return the world-space position of pointB
     * 
     * @param constraint constraint
     * @return the world-space position
     */
    static public Vector pointBWorld(Constraint constraint) {
        double x = (constraint.bodyB != null ? constraint.bodyB.position.x : 0)
                + (constraint.pointB != null ? constraint.pointB.x : 0);
        double y = (constraint.bodyB != null ? constraint.bodyB.position.y : 0)
                + (constraint.pointB != null ? constraint.pointB.y : 0);
        return new Vector(x, y);
    }
}