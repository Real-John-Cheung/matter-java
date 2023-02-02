package matter.body;

import matter.geometry.Vector;
import matter.geometry.Vertices;
import matter.geometry.Vertices.Chamfer;
import matter.geometry.Bounds;
import matter.geometry.Axes;
import matter.core.Common;
import matter.collision.Collision;
import matter.collision.Collision.CollisionFilter;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This matter.body module contains methods for creating amd manipulating body
 * models
 * 
 * @class Body
 */
public class Body {
    static public double _inertiaScale = 4;
    static public int _nextCollidingGroupId = 1;
    static public int _nextNonCollidingGroupId = -1;
    static public byte _nextCategory = 0x0001;

    /**
     * an int uniquely identifying number
     */
    public int id;
    /**
     * a string denoting the type
     */
    public String type;
    /**
     * label
     */
    public String label;
    /**
     * an array of bodies that make up this body, the first body in the array must
     * always be a self reference to the current body instance, parts are allowed to
     * overlap, have gaps or holes or even form concave bodies
     */
    public ArrayList<Body> parts;
    /**
     * an hashmap reserved for storing plugin-specific properties
     */
    public HashMap<String, Object> plugin;
    /**
     * a reference to the body that this is a part of, if it is not a part of any
     * body, referencing to itself
     */
    public Body parent;
    /**
     * specify the angle of the body, in radians
     */
    public double angle;
    /**
     * specify the convex hull of the rigid body
     */
    public Vertices.Vertex[] vertices;
    /**
     * specify the current world-space position of the body
     */
    public Vector position;
    /**
     * specify the force to apply in the current step
     */
    public Vector force;
    /**
     * specify the torque (turning force) to apply in the current step
     */
    public double torque;
    public Vector positionImpulse;
    public double[] constraintImpulse; //x, y, angle
    public int totalContacts;
    /**
     * measure the current speed of the body after the last update
     */
    public double speed;
    /**
     * measure the current angular speed of the body after the last update
     */
    public double angularSpeed;
    /**
     * measure the current velocity of the body after the last update
     */
    public Vector velocity;
    /**
     * measure the current angular velocity of the body after the last update
     */
    public double angularVelocity;
    /**
     * indicate if a body is static, which means it will not move
     */
    public boolean isStatic;
    /**
     * indicate if a body is a sensor
     */
    public boolean isSensor;
    /**
     * indicate if a body is sleeping, a body is static when it's sleeping
     */
    public boolean isSleeping;
    /**
     * measure the amount of movement a body currently has
     */
    public double motion;
    /**
     * define the number of updates in which this body must have near-zero velocity
     * before it is set as sleeping
     */
    public int sleepThreshold;
    /**
     * mass per unit area
     */
    public double density;
    /**
     * the mass of the body
     */
    public double mass;
    /**
     * 1 / mass
     */
    public double inverseMass;
    /**
     * define the moment of inertia of a body
     */
    public double inertia;
    /**
     * 1 / inertia
     */
    public double inverseInertia;
    /**
     * define the restitution (elasticity) of the body, range (0, 1)
     */
    public double restitution;
    /**
     * define the friction of the body, range (0, 1)
     */
    public double friction;
    /**
     * define the static friction of the body
     */
    public double frictionStatic;
    /**
     * define the air friction (air resistance) of the body
     */
    public double frictionAir;
    /**
     * specify the collision filtering properties of this body
     * 
     * @see Collision.CollisionFilter
     */
    public Collision.CollisionFilter collisionFilter;
    /**
     * specify a tolerance on how far a body is allowed to 'sink' or rotate into
     * other bodies
     */
    public double slop;
    /**
     * per-body time scaling
     */
    public double timeScale;
    // public Render render
    /**
     * an array of unique axis vectors (edge normals) used for collision detection
     */
    public Vector[] axes;
    /**
     * the area of the body's convex hull
     */
    public double area;
    /**
     * a Bounds object that defines the AABB region for the body.
     */
    public Bounds bounds;
    public HashMap<String, ArrayList<Consumer<HashMap<String, Object>>>> events;
    public int sleepCounter;
    public Vertices.Chamfer chamfer;
    public double circleRadius;
    public Vector positionPrev;
    public double anglePrev;
    public HashMap<String, Double> _original;

    public Body() {
        this(null);
    }

    /**
     * create a new rigid body model, the options parameter is a hash map that
     * specifies any properties you wish to override the defaults
     * see the fields of Body class for more infomation about what can be pass
     * through options
     * 
     * @param options
     */
    @SuppressWarnings("unchecked")
    public Body(HashMap<String, Object> options) {
        this.id = Common.nextId();
        this.type = (String) Common.parseOption(options, "type", "body");
        this.label = (String) Common.parseOption(options, "label", "Body");
        this.parts = (ArrayList<Body>) Common.parseOption(options, "parts", new ArrayList<Body>());
        this.plugin = (HashMap<String, Object>) Common.parseOption(options, "plugin", null);
        this.angle = (Double) Common.parseOption(options, "angle", 0d);
        this.vertices = (Vertices.Vertex[]) Common.parseOption(options, "vertices",
                Vertices.fromPath("L 0 0 L 40 0 L 40 40 L 0 40", this));
        this.position = (Vector) Common.parseOption(options, "position", new Vector());
        this.force = (Vector) Common.parseOption(options, "position", new Vector());
        this.torque = (Double) Common.parseOption(options, "torque", 0d);
        this.positionImpulse = new Vector();
        this.constraintImpulse = new double[] {0, 0, 0};
        this.totalContacts = 0;
        this.speed = (Double) Common.parseOption(options, "speed", 0d);
        this.angularSpeed = (Double) Common.parseOption(options, "angularSpeed", 0d);
        this.velocity = (Vector) Common.parseOption(options, "velocity", new Vector());
        this.angularVelocity = (Double) Common.parseOption(options, "angularVelocity", 0d);
        this.isSensor = (Boolean) Common.parseOption(options, "isSensor", false);
        this.isStatic = (Boolean) Common.parseOption(options, "isStatic", false);
        this.isSleeping = (Boolean) Common.parseOption(options, "isSleeping", false);
        this.motion = 0;
        this.sleepThreshold = (Integer) Common.parseOption(options, "sleepThreshold", 60);
        this.density = (Double) Common.parseOption(options, "density", 0.001d);
        this.restitution = (Double) Common.parseOption(options, "restitution", 0d);
        this.friction = (Double) Common.parseOption(options, "friction", 0.1);
        this.frictionStatic = (Double) Common.parseOption(options, "frictionStatic", 0.5d);
        this.frictionAir = (Double) Common.parseOption(options, "frictionAir", 0.01d);
        this.collisionFilter = (Collision.CollisionFilter) Common.parseOption(options, "collisionFilter",
                new Collision.CollisionFilter(null));
        this.slop = (Double) Common.parseOption(options, "slop", 0.05d);
        this.timeScale = (Double) Common.parseOption(options, "timeScale", 1d);
        // this.render
        this.events = null;
        this.bounds = (Bounds) Common.parseOption(options, "bounds", null);
        this.chamfer = (Chamfer) Common.parseOption(options, "chamfer", null);
        this.circleRadius = 0;
        this.positionPrev = null;
        this.anglePrev = 0;
        this.parent = (Body) Common.parseOption(options, "parent", null);
        this.axes = null;
        this.area = 0;
        this.mass = 0;
        this.inertia = 0;
        this._original = null;
        this.sleepCounter = this.isSleeping ? this.sleepThreshold : 0;
        Body._initProperties(this, options);
    }

    /**
     * create a new rigid body model
     * 
     * @param options options
     * @return body
     */
    static public Body create(HashMap<String, Object> options) {
        return new Body(options);
    }

    /**
     * return the next unique group index for which bodies will collide
     * 
     * @return unique group index
     */
    static public int nextGroup() {
        return Body.nextGroup(false);
    }

    /**
     * return the next unique group index for which bodies will collide
     * 
     * @param isNonColliding
     * @return unique group index
     */
    static public int nextGroup(boolean isNonColliding) {
        if (isNonColliding)
            return Body._nextNonCollidingGroupId--;
        return Body._nextCollidingGroupId++;
    }

    /**
     * return the next unique category bitfield
     * 
     * @see Collision.CollisionFilter
     * 
     * @return unique category bitfield
     */
    static public byte nextCategory() {
        Body._nextCategory = (byte) ((Body._nextCategory & 0xFF) << 1);
        return Body._nextCategory;
    }

    /**
     * init
     * 
     * @param body    body
     * @param options options
     */
    static private void _initProperties(Body body, HashMap<String, Object> options) {
        if (body.bounds == null) {
            body.bounds = Bounds.create(body.vertices);
        }
        if (body.positionPrev == null) {
            body.positionPrev = Vector.clone(body.position);
        }
        body.anglePrev = body.angle;
        if (body.parts == null) {
            body.parts = new ArrayList<Body>();
        }
        if (body.parts.size() == 0) {
            body.parts.add(body);
        }
        if (body.parent == null)
            body.parent = body;
        //
        if (body.axes == null)
            body.axes = Axes.fromVertices(body.vertices);
        Vertices.rotate(body.vertices, body.angle, body.position);
        Axes.rotate(body.axes, body.angle);
        Bounds.update(body.bounds, body.vertices, body.velocity);
        //
        if (options.get("axes") != null) {
            body.axes = (Vector[]) options.get("axes");
        }
        if (options.get("area") != null) {
            body.area = (Double) options.get("area");
        }
        if (options.get("mass") != null) {
            body.mass = (Double) options.get("mass");
        }
        if (options.get("inertia") != null) {
            body.inertia = (Double) options.get("inertia");
        }
        //
        body.inverseInertia = 1 / body.inertia;
        body.inverseMass = 1 / body.mass;
    }

    /**
     * set the body as static
     * 
     * @param body
     * @param isStatic
     */
    static public void setStatic(Body body, boolean isStatic) {
        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            part.isStatic = isStatic;
            if (isStatic) {
                if (part._original == null)
                    part._original = new HashMap<String, Double>();
                part._original.put("restitution", part.restitution);
                part._original.put("friction", part.friction);
                part._original.put("mass", part.mass);
                part._original.put("inertia", part.inertia);
                part._original.put("inverseInertia", part.inverseInertia);
                part._original.put("inverseMass", part.inverseMass);
                part._original.put("density", part.density);

                part.restitution = 0;
                part.friction = 1;
                part.mass = Double.POSITIVE_INFINITY;
                part.inertia = Double.POSITIVE_INFINITY;
                part.density = Double.POSITIVE_INFINITY;
                part.inverseInertia = 0;
                part.inverseMass = 0;

                part.positionPrev.x = part.position.x;
                part.positionPrev.y = part.position.y;
                part.anglePrev = part.angle;
                part.angularVelocity = 0;
                part.speed = 0;
                part.angularSpeed = 0;
                part.motion = 0;
            } else if (part._original != null) {
                part.restitution = part._original.get("restitution");
                part.friction = part._original.get("friction");
                part.mass = part._original.get("mass");
                part.inertia = part._original.get("inertia");
                part.density = part._original.get("density");
                part.inverseMass = part._original.get("inverseMass");
                part.inverseInertia = part._original.get("inverseInertia");

                part._original = null;
            }
        }
    }

    /**
     * set the mass of the body
     * 
     * @param body body
     * @param mass mass
     */
    static public void setMass(Body body, double mass) {
        double moment = body.inertia / (body.mass / 6);
        body.inertia = moment * (mass / 6);
        body.inverseInertia = 1 / body.inertia;

        body.mass = mass;
        body.inverseMass = 1 / body.mass;
        body.density = body.mass / body.area;
    }

    /**
     * set the density of the body
     * 
     * @param body    body
     * @param density density
     */
    static public void setDensity(Body body, double density) {
        Body.setMass(body, density * body.area);
        body.density = density;
    }

    /**
     * set the moment of inertia
     * 
     * @param body    body
     * @param inertia inertia
     */
    static public void setInertia(Body body, double inertia) {
        body.inertia = inertia;
        body.inverseInertia = 1 / body.inertia;
    }

    /**
     * set the body's vertices and updates body properties accordingly
     * 
     * @param body     body
     * @param vertices vertices
     */
    static public void setVertices(Body body, Vector[] vertices) {
        Vertices.Vertex[] vs = Vertices.create(vertices, body);
        Body.setVertices(body, vs);
    }

    /**
     * set the body's vertices and updates body properties accordingly
     * 
     * @param body     body
     * @param vertices vertices
     */
    static public void setVertices(Body body, Vertices.Vertex[] vertices) {
        if (Objects.equals(vertices[0].body, body)) {
            body.vertices = vertices;
        } else {
            body.vertices = Vertices.create(vertices, body);
        }

        body.axes = Axes.fromVertices(body.vertices);
        body.area = Vertices.area(body.vertices);
        Body.setMass(body, body.density * body.area);

        Vector centre = Vertices.centre(body.vertices);
        Vertices.translate(body.vertices, centre, -1);

        Body.setInertia(body, Body._inertiaScale * Vertices.inertia(body.vertices, body.mass));

        Vertices.translate(body.vertices, body.position);
        Bounds.update(body.bounds, body.vertices, body.velocity);
    }

    /**
     * set the parts of the body and updates mass, inertia and centroid
     * 
     * @param body  body
     * @param parts parts
     */
    static public void setParts(Body body, Body[] parts) {
        Body.setParts(body, parts, true);
    }

    /**
     * set the parts of the body and updates mass, inertia and centroid
     * 
     * @param body     body
     * @param parts    parts
     * @param autoHull autoHull
     */
    static public void setParts(Body body, Body[] parts, boolean autoHull) {
        parts = Arrays.copyOf(parts, parts.length);
        body.parts.clear();
        body.parts.add(body);
        body.parent = body;
        for (int i = 0; i < parts.length; i++) {
            Body part = parts[i];
            if (!Objects.equals(part, body)) {
                part.parent = body;
                body.parts.add(part);
            }
        }
        if (body.parts.size() == 1)
            return;

        if (autoHull) {
            Vertices.Vertex[] vertices = new Vertices.Vertex[0];

            for (int i = 0; i < parts.length; i++) {
                Vertices.Vertex[] res = Arrays.copyOf(vertices, vertices.length + parts[i].vertices.length);
                System.arraycopy(parts[i].vertices, 0, res, vertices.length, parts[i].vertices.length);
                vertices = res;
            }

            Vertices.clockwiseSort(vertices);

            Vertices.Vertex[] hull = Vertices.hull(vertices);
            Vector hullCentre = Vertices.centre(vertices);
            Body.setVertices(body, hull);
            Vertices.translate(body.vertices, hullCentre);
        }

        HashMap<String, Double> total = Body._totalProperties(body);
        body.area = total.get("area");
        body.parent = body;
        body.position.x = total.get("centreX");
        body.position.y = total.get("centreY");
        body.positionPrev.x = total.get("centreX");
        body.positionPrev.y = total.get("centreY");

        Body.setMass(body, total.get("mass"));
        Body.setInertia(body, total.get("inertia"));
        Body.setPosition(body, new Vector(total.get("centreX"), total.get("centreY")));
    }

    /**
     * set the centre of mass of the body
     * 
     * @param body     body
     * @param centre   centre
     * @param relative relative
     */
    static public void setCentre(Body body, Vector centre, boolean relative) {
        if (!relative) {
            body.positionPrev.x = centre.x - (body.position.x - body.positionPrev.x);
            body.positionPrev.y = centre.y - (body.position.y - body.positionPrev.y);
            body.position.x = centre.x;
            body.position.y = centre.y;
        } else {
            body.positionPrev.x += centre.x;
            body.positionPrev.y += centre.y;
            body.position.x += centre.x;
            body.position.y += centre.y;
        }
    }

    /**
     * set the positon of the body instantly
     * 
     * @param body    body
     * @param postion postion
     */
    static public void setPosition(Body body, Vector position) {
        Vector delta = Vector.sub(position, body.position);
        body.positionPrev.x += delta.x;
        body.positionPrev.y += delta.y;

        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            part.position.x += delta.x;
            part.position.y += delta.y;
            Vertices.translate(part.vertices, delta);
            Bounds.update(part.bounds, part.vertices, body.velocity);
        }
    }

    /**
     * set the angle of the body instantly
     * 
     * @param body  body
     * @param angle angle
     */
    static public void setAngle(Body body, double angle) {
        double delta = angle - body.angle;
        body.anglePrev += delta;

        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            part.angle += delta;
            Vertices.rotate(part.vertices, delta, body.position);
            Axes.rotate(part.axes, delta);
            Bounds.update(part.bounds, part.vertices, body.velocity);
            if (i > 0) {
                Vector.rotateAbout(part.position, delta, body.position, part.position);
            }
        }
    }

    /**
     * set the velocity of the body instantly
     * 
     * @param body     body
     * @param velocity velocity
     */
    static public void setVelocity(Body body, Vector velocity) {
        body.positionPrev.x = body.position.x - velocity.x;
        body.positionPrev.y = body.position.y - velocity.y;
        body.velocity.x = velocity.x;
        body.velocity.y = velocity.y;
        body.speed = Vector.magnitude(body.velocity);
    }

    /**
     * set the angular velocity of the body instantly
     * 
     * @param body     body
     * @param velocity velocity
     */
    static public void setAngularVelocity(Body body, double velocity) {
        body.anglePrev = body.angle - velocity;
        body.angularVelocity = velocity;
        body.angularSpeed = Math.abs(body.angularVelocity);
    }

    /**
     * move a body by a given vector relative to its current position, without
     * imparting any velocity
     * 
     * @param body        body
     * @param translation
     */
    static public void translate(Body body, Vector translation) {
        Body.setPosition(body, Vector.add(body.position, translation));
    }

    /**
     * rotate a body by an angle
     * 
     * @param body     body
     * @param rotation rotation
     */
    static public void rotate(Body body, double rotation) {
        Body.rotate(body, rotation, null);
    }

    /**
     * rotate a body by an angle around a point
     * 
     * @param body     body
     * @param rotation angle
     * @param point    point
     */
    static public void rotate(Body body, double rotation, Vector point) {
        if (point == null) {
            Body.setAngle(body, body.angle + rotation);
        } else {
            double cos = Math.cos(rotation);
            double sin = Math.sin(rotation);
            double dx = body.position.x - point.x;
            double dy = body.position.y - point.y;

            Body.setPosition(body, new Vector(point.x + (dx * cos - dy * sin), point.y + (dx * sin + dy * cos)));
            Body.setAngle(body, body.angle + rotation);
        }
    }

    /**
     * scale the body
     * 
     * @param body   body
     * @param scaleX scaleX
     * @param scaleY scaleY
     */
    static public void scale(Body body, double scaleX, double scaleY) {
        Body.scale(body, scaleX, scaleY, null);
    }

    /**
     * scale the body from a world-space point
     * 
     * @param body   body
     * @param scaleX scaleX
     * @param scaleY scaleY
     * @param point  point
     */
    static public void scale(Body body, double scaleX, double scaleY, Vector point) {
        double totalArea = 0;
        double totalInertia = 0;
        if (point == null) {
            point = body.position;
        }

        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            Vertices.scale(part.vertices, scaleX, scaleY, point);
            part.axes = Axes.fromVertices(part.vertices);
            part.area = Vertices.area(part.vertices);
            Body.setMass(part, body.density * part.area);
            Vertices.translate(part.vertices, new Vector(-part.position.x, -part.position.y));
            Body.setInertia(part, Body._inertiaScale * Vertices.inertia(part.vertices, part.mass));
            Vertices.translate(part.vertices, new Vector(part.position.x, part.position.y));
            if (i > 0) {
                totalArea += part.area;
                totalInertia += part.inertia;
            }
            part.position.x = point.x + (part.position.x - point.x) * scaleX;
            part.position.y = point.y + (part.position.y - point.y) * scaleY;

            Bounds.update(part.bounds, part.vertices, body.velocity);
        }

        if (body.parts.size() > 1) {
            body.area = totalArea;
            if (!body.isStatic) {
                Body.setMass(body, body.density * totalArea);
                Body.setInertia(body, totalInertia);
            }
        }

        if (body.circleRadius > 0) {
            if (scaleX == scaleY) {
                body.circleRadius *= scaleX;
            } else {
                body.circleRadius = -1; // no longer circle
            }
        }
    }

    /**
     * perform a simulation step for the given body
     * 
     * @param body body
     * @param deltaTime deltaTime
     * @param timeScale timeScale
     * @param correction correction
     */
    static public void update(Body body, double deltaTime, double timeScale, double correction) {
        double deltaTimeSquared = deltaTime * deltaTime;
        double frictionAir = 1 - body.frictionAir * timeScale * body.timeScale,
                velocityPrevX = body.position.x - body.positionPrev.x,
                velocityPrevY = body.position.y - body.positionPrev.y;
        body.velocity.x = (velocityPrevX * frictionAir * correction) + (body.force.x / body.mass) * deltaTimeSquared;
        body.velocity.y = (velocityPrevY * frictionAir * correction) + (body.force.y / body.mass) * deltaTimeSquared;

        body.positionPrev.x = body.position.x;
        body.positionPrev.y = body.position.y;
        body.position.x += body.velocity.x;
        body.position.y += body.velocity.y;

        body.angularVelocity = ((body.angle - body.anglePrev) * frictionAir * correction) + (body.torque / body.inertia) * deltaTimeSquared;
        body.anglePrev = body.angle;
        body.angle += body.angularVelocity;

        body.speed = Vector.magnitude(body.velocity);
        body.angularSpeed = Math.abs(body.angularVelocity);

        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            Vertices.translate(part.vertices, body.velocity);
            
            if (i > 0) {
                part.position.x += body.velocity.x;
                part.position.y += body.velocity.y;
            }

            if (body.angularVelocity != 0) {
                Vertices.rotate(part.vertices, body.angularVelocity, body.position);
                Axes.rotate(part.axes, body.angularVelocity);
                if (i > 0) {
                    Vector.rotateAbout(part.position, body.angularVelocity, body.position, part.position);
                }
            }

            Bounds.update(part.bounds, part.vertices, body.velocity);
        }
        
    }

    /**
     * apply force to a body from a given point
     * 
     * @param body body
     * @param position position
     * @param force force
     */
    static public void applyForce(Body body, Vector position, Vector force) {
        body.force.x += force.x;
        body.force.y += force.y;
        Vector offset = new Vector(position.x - body.position.x, position.y - body.position.y);
        body.torque += offset.x * force.y - offset.y * force.x;
    }

    /**
     * return the sums of properties of all comps
     * 
     * @param body body
     * @return properties
     */
    static private HashMap<String, Double> _totalProperties(Body body) {
        HashMap<String, Double> properties = new HashMap<String, Double>();
        double mass = 0;
        double area = 0;
        double inertia = 0;
        Vector centre = new Vector();

        for (int i = 0; i < body.parts.size(); i++) {
            Body part = body.parts.get(i);
            double pm = part.mass != Double.POSITIVE_INFINITY ? part.mass : 1;
            mass += pm;
            area += part.area;
            inertia += part.inertia;
            centre = Vector.add(centre, Vector.mult(part.position, mass));
        }

        centre = Vector.div(centre, mass);
        properties.put("mass", mass);
        properties.put("area", area);
        properties.put("inertia", inertia);
        properties.put("centreX", centre.x);
        properties.put("centreY", centre.y);
        return properties;
    }
}

 /*
    *
    *  Events Documentation
    *
    */

    /**
    * Fired when a body starts sleeping (where `this` is the body).
    *
    * @event sleepStart
    * @this {body} The body that has started sleeping
    * @param {} event An event object
    * @param {} event.source The source object of the event
    * @param {} event.name The name of the event
    */

    /**
    * Fired when a body ends sleeping (where `this` is the body).
    *
    * @event sleepEnd
    * @this {body} The body that has ended sleeping
    * @param {} event An event object
    * @param {} event.source The source object of the event
    * @param {} event.name The name of the event
    */
