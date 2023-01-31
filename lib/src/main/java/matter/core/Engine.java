package matter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import matter.body.Composite;
import matter.body.Body;
import matter.collision.Collision;
import matter.collision.Detector;
import matter.collision.Pairs;
import matter.collision.Pair;
import matter.constraint.Constraint;
import matter.geometry.Bounds;
import matter.geometry.Vector;
import matter.collision.Resolver;

/**
 * methods for creating and manipulating engines
 * 
 * @author JohnC
 */
public class Engine {

    /**
     * an intege that specifies the number of position iterations to perform each
     * update
     */
    public int positionIterations;

    /**
     * an integer that specifies the number of velocity iterations to perform each
     * update
     */
    public int velocityIterations;

    /**
     * an integer that specifies the number of constraint iterations to perform each
     * update
     */
    public int constraintIterations;

    /**
     * specifies whether the engine should allow sleeping
     */
    public boolean enableSleeping;

    /**
     * containing properties regarding the timing systems of the engine
     * timing.timeScale : specifies the global scaling factor of time for all bodies
     * timing.timestamp : specifies the current simulation-time in milliseconds
     * timing.lastElapsed : represents the total execution time elapsed during the
     * last Engine.update in milliseconds
     * timing.lastDelta : represents the delta value used in the last engine update
     */
    public HashMap<String, Double> timing;

    /**
     * a detector
     */
    public Detector detector;

    /**
     * a grid
     */
    // public Grid grid;

    /**
     * root Matter.Composite instance that will contain all bodies, constraints and
     * other composites to be simulated by this engine
     */
    public Composite world;

    /**
     * reserved for storing plugin-specific properties
     */
    public HashMap<String, Object> plugin;

    /**
     * gravity to apply on all bodies
     * gravity.x: gravity x component
     * gravity.y: gravity y component
     * gravity.scale: gravity scale factor
     */
    public HashMap<String, Double> gravity;

    public HashMap<String, ArrayList<Consumer<HashMap<String, Object>>>> events;
    public Pairs pairs;

    /**
     * create a new engine
     */
    public Engine() {
        this(null);
    }

    /**
     * create a new engine, the options parameter is an object that specifies any
     * properties you wish to override the defaults
     * 
     * @param options options
     */
    @SuppressWarnings("unchecked")
    public Engine(HashMap<String, Object> options) {
        this.positionIterations = (Integer) options.getOrDefault("positionIterations", 6);
        this.velocityIterations = (Integer) options.getOrDefault("velocityIterations", 4);
        this.constraintIterations = (Integer) options.getOrDefault("constraintIterations", 2);
        this.enableSleeping = (Boolean) options.getOrDefault("enableSleeping", false);
        this.events = new HashMap<>();
        this.plugin = (HashMap<String, Object>) options.getOrDefault("plugin", null);
        HashMap<String, Double> defaultGravity = new HashMap<String, Double>();
        defaultGravity.put("x", 0d);
        defaultGravity.put("y", 1d);
        defaultGravity.put("scale", 0.001);
        this.gravity = (HashMap<String, Double>) options.getOrDefault("gravity", defaultGravity);
        HashMap<String, Double> defaultTiming = new HashMap<String, Double>();
        defaultTiming.put("timestamp", 0d);
        defaultTiming.put("timeScale", 1d);
        defaultTiming.put("lastDelta", 0d);
        defaultTiming.put("lastElapsed", 0d);
        this.timing = (HashMap<String, Double>) options.getOrDefault("timing", defaultTiming);

        this.world = (Composite) options.getOrDefault("world", Composite.create(Common.opts("label", "World")));
        this.pairs = (Pairs) options.getOrDefault("pairs", Pairs.create(null));
        this.detector = (Detector) options.getOrDefault("detector", Detector.create(null));
    }

    /**
     * create a new engine
     * 
     * @return engine
     */
    static public Engine create() {
        return new Engine();
    }

    /**
     * create a new engine, the options parameter is an object that specifies any
     * properties you wish to override the defaults
     * 
     * @param options options
     * @return engine
     */
    static public Engine create(HashMap<String, Object> options) {
        return new Engine(options);
    }

    /**
     * move the simulation forward in time
     * 
     * @param engine engine
     */
    static public Engine update(Engine engine) {
        return Engine.update(engine, 16.666, 1);
    }

    /**
     * move the simulation forward in time by delta ms
     * 
     * @param engine     engine
     * @param delta      delta
     * @param correction delta / lastDelta, help improve the accuracy of the
     *                   simulation in cases where delta is changing between updates
     */
    static public Engine update(Engine engine, double delta, double correction) {
        double startTime = Common.now();
        Composite world = engine.world;
        Detector detector = engine.detector;
        Pairs pairs = engine.pairs;
        HashMap<String, Double> timing = engine.timing;
        double timestamp = timing.get("timestamp");
        timing.put("timestamp", timestamp + delta * timing.get("timeScale"));
        timing.put("lastDelta", delta * timing.get("timeScale"));
        HashMap<String, Object> event = Common.opts("timestamp", timing.get("timestamp"));
        Events.trigger(engine, "beforeUpdate", event);

        Body[] allBodies = Composite.allBodies(world);
        Constraint[] allConstraints = Composite.allConstraints(world);

        if (world.isModified) {
            Detector.setBodies(detector, allBodies);
        }

        if (world.isModified) {
            Composite.setModified(world, false, false, true);
        }

        if (engine.enableSleeping)
            Sleeping.update(allBodies, timing.get("timeScale"));

        Engine._bodiesApplyGravity(allBodies, engine.gravity);

        // update all body position and rotation by integration
        Engine._bodiesUpdate(allBodies, delta, timing.get("timeScale"), correction);

        // update all constraints (first pass)
        Constraint.preSolveAll(allBodies);
        for (int i = 0; i < engine.constraintIterations; i++) {
            Constraint.solveAll(allConstraints, timing.get("timeScale"));
        }
        Constraint.postSolveAll(allBodies);

        detector.pairs = engine.pairs;
        Collision[] collisions = Detector.collisions(detector);

        Pairs.update(pairs, collisions, timestamp);

        if (engine.enableSleeping)
            Sleeping.afterCollisions(pairs.list.toArray(Pair[]::new), timing.get("timeScale"));
        if (pairs.collisionStart.size() > 0)
            Events.trigger(engine, "collisionStart", Common.opts("pairs", pairs.collisionStart));

        Resolver.preSolvePosition(pairs.list.toArray(Pair[]::new));
        for (int i = 0; i < engine.positionIterations; i++) {
            Resolver.solvePosition(pairs.list.toArray(Pair[]::new), timing.get("timeScale"));
        }
        Resolver.postSolvePosition(allBodies);

        Constraint.preSolveAll(allBodies);
        for (int i = 0; i < engine.constraintIterations; i++) {
            Constraint.solveAll(allConstraints, timing.get("timeScale"));
        }
        Constraint.postSolveAll(allBodies);

        Resolver.preSolveVelocity(pairs.list.toArray(Pair[]::new));
        for (int i = 0; i < engine.velocityIterations; i++) {
            Resolver.solveVelocity(pairs.list.toArray(Pair[]::new), timing.get("timeScale"));
        }

        if (pairs.collisionActive.size() > 0)
            Events.trigger(engine, "collisionActive", Common.opts("pairs", pairs.collisionActive));

        if (pairs.collisionEnd.size() > 0)
            Events.trigger(engine, "collisionEnd", Common.opts("pairs", pairs.collisionEnd));

        Engine._bodiesClearForces(allBodies);

        Events.trigger(engine, "afterUpdate", event);
        engine.timing.put("lastElapsed", Common.now() - startTime);
        return engine;
    }

    /**
     * merge two engines by keeping the configuration of engineA but replacing the
     * world with the one from engineB
     * 
     * @param engineA
     * @param engineB
     */
    static public void merge(Engine engineA, Engine engineB) {
        if (engineB.world != null) {
            engineA.world = engineB.world;
            Engine.clear(engineA);
            Body[] bodies = Composite.allBodies(engineA.world);
            for (int i = 0; i < bodies.length; i++) {
                Body body = bodies[i];
                Sleeping.set(body, false);
                body.id = Common.nextId();
            }
        }
    }

    /**
     * clear the engine pairs and detector
     * 
     * @param engine engine
     */
    static public void clear(Engine engine) {
        Pairs.clear(engine.pairs);
        Detector.clear(engine.detector);
    }

    /**
     * zero the force and torque buffers
     * 
     * @param bodies bodies
     */
    static private void _bodiesClearForces(Body[] bodies) {
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            body.force.x = 0;
            body.force.y = 0;
            body.torque = 0;
        }
    }

    /**
     * apply a mass dependant force to all given bodies
     * 
     * @param bodies  bodies
     * @param gravity gravity
     */
    static private void _bodiesApplyGravity(Body[] bodies, Vector gravity) {
        HashMap<String, Double> temG = new HashMap<>();
        temG.put("x", gravity.x);
        temG.put("y", gravity.y);
        temG.put("scale", 0.001);
        Engine._bodiesApplyGravity(bodies, temG);
    }

    /**
     * apply a mass dependant force to all given bodies
     * 
     * @param bodies       bodies
     * @param gravity      gravity
     * @param gravityScale grabityScale
     */
    static private void _bodiesApplyGravity(Body[] bodies, HashMap<String, Double> gravity) {
        double gravityX = gravity.getOrDefault("x", 0d);
        double gravityY = gravity.getOrDefault("y", 0d);
        double gravityScale = gravity.getOrDefault("scale", 0d);
        if ((gravityX == 0 && gravityY == 0) || gravityScale == 0) {
            return;
        }

        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            if (body.isStatic || body.isSleeping)
                continue;
            body.force.y += body.mass * gravityY * gravityScale;
            body.force.x += body.mass * gravityX * gravityScale;
        }
    }

    /**
     * update all boides
     * 
     * @param bodies     bodies
     * @param deltaTime  deltaTime
     * @param timeScale  timeScale
     * @param correction correction
     */
    static private void _bodiesUpdate(Body[] bodies, double deltaTime, double timeScale, double correction) {
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            if (body.isStatic || body.isSleeping)
                continue;

            Body.update(body, deltaTime, timeScale, correction);
        }
    }

    /*
     * 
     * Events documentation
     * 
     */

    /**
     * Fired just before an update
     *
     * @event beforeUpdate
     * @param event           An event map
     * @param event.timestamp The engine.timing.timestamp of the event
     * @param event.source    The source object of the event
     * @param event.name      The name of the event
     */

    /**
     * Fired after engine update and all collision events
     *
     * @event afterUpdate
     * @param event           An event object
     * @param event.timestamp The engine.timing.timestamp of the event
     * @param event.source    The source object of the event
     * @param event.name      The name of the event
     */

    /**
     * Fired after engine update, provides a list of all pairs that have started to
     * collide in the current tick (if any)
     *
     * @event collisionStart
     * @param event           An event object
     * @param event.pairs     List of affected pairs
     * @param event.timestamp The engine.timing.timestamp of the event
     * @param event.source    The source object of the event
     * @param event.name      The name of the event
     */

    /**
     * Fired after engine update, provides a list of all pairs that are colliding in
     * the current tick (if any)
     *
     * @event collisionActive
     * @param event           An event object
     * @param event.pairs     List of affected pairs
     * @param event.timestamp The engine.timing.timestamp of the event
     * @param event.source    The source object of the event
     * @param event.name      The name of the event
     */

    /**
     * Fired after engine update, provides a list of all pairs that have ended
     * collision in the current tick (if any)
     *
     * @event collisionEnd
     * @param event           An event object
     * @param event.pairs     List of affected pairs
     * @param event.timestamp The engine.timing.timestamp of the event
     * @param event.source    The source object of the event
     * @param event.name      The name of the event
     */

}