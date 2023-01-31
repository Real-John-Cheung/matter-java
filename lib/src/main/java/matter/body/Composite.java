package matter.body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

import matter.Common;
import matter.constraint.Constraint;
import matter.constraint.MouseConstraint;
import matter.geometry.Bounds;
import matter.geometry.Vector;
import matter.geometry.Vertices.Vertex;
import matter.Events;

/**
 * 
 * @author JohnC
 */
public class Composite {

    /**
     * an integer uniquely identifying number generated by Common.nextId
     */
    public int id;

    /**
     * the type of object
     */
    public String type;

    /**
     * name to help the user identify and manage composites
     */
    public String label;

    /**
     * specify whether the composite has been modified during the current step
     */
    public boolean isModified;

    /**
     * the parent of this composite
     */
    public Composite parent;

    /**
     * direct body children of this composite
     */
    public ArrayList<Body> bodies;

    /**
     * direct constraint children of this composite
     */
    public ArrayList<Constraint> constraints;

    /**
     * direct composite children of this composite
     */
    public ArrayList<Composite> composites;

    /**
     * object reserved for plugin
     */
    public HashMap<String, Object> plugin;

    /**
     * object used for storing cached results for performance reasons
     */
    public HashMap<String, Object> chache;

    public HashMap<String, ArrayList<Consumer<HashMap<String, Object>>>> events;
    /**
     * create a new composite object
     */
    public Composite() {
        this(null);
    }

    /**
     * create a new composite object with options to overwrite defaults, for more
     * infomation see the fields of the Composite class
     * 
     * @param options
     */
    @SuppressWarnings("unchecked")
    public Composite(HashMap<String, Object> options) {
        this.id = Common.nextId();
        this.type = "composite";
        this.parent = (Composite) options.getOrDefault("parent", null);
        this.isModified = false;
        this.bodies = new ArrayList<Body>();
        Object opBods = options.get("bodies");
        if (opBods != null) {
            if (opBods instanceof ArrayList) {
                this.bodies.addAll((ArrayList<Body>) opBods);
            } else if (opBods.getClass().isArray()) {
                this.bodies.addAll(Arrays.asList((Body[]) opBods));
            }
        }
        this.constraints = new ArrayList<Constraint>();
        Object opCons = options.get("constraints");
        if (opCons != null) {
            if (opCons instanceof ArrayList) {
                this.constraints.addAll((ArrayList<Constraint>) opCons);
            } else if (opCons.getClass().isArray()) {
                this.constraints.addAll(Arrays.asList((Constraint[]) opCons));
            }
        }
        this.composites = new ArrayList<Composite>();
        Object opComs = options.get("composites");
        if (opComs != null) {
            if (opComs instanceof ArrayList) {
                this.composites.addAll((ArrayList<Composite>) opComs);
            } else if (opComs.getClass().isArray()) {
                this.composites.addAll(Arrays.asList((Composite[]) opComs));
            }
        }
        this.label = (String) options.getOrDefault("label", "Composite");
        this.plugin = (HashMap<String, Object>) options.getOrDefault("plugin", null);
        this.chache = new HashMap<String, Object>();
        this.chache.put("allBodies", null);
        this.chache.put("allConstraints", null);
        this.chache.put("allComposites", null);
        this.events = null;
        //this.bounds = Composite.bounds(this);
    }

    /**
     * create a new composite
     * 
     * @param options options
     * @return a new composite
     */
    static public Composite create(HashMap<String, Object> options) {
        return new Composite(options);
    }

    static public void setModified(Composite composite, boolean isModified) {
        Composite.setModified(composite, isModified, false, false);
    }

    static public void setModified(Composite composite, boolean isModified, boolean updateParents,
            boolean updateChildren) {
        composite.isModified = isModified;
        if (isModified && composite.chache != null) {
            composite.chache.put("allBodies", null);
            composite.chache.put("allConstraints", null);
            composite.chache.put("allComposites", null);
        }
        if (updateParents && composite.parent != null) {
            Composite.setModified(composite.parent, isModified, updateParents, updateChildren);
        }
        if (updateChildren) {
            for (int i = 0; i < composite.composites.size(); i++) {
                Composite childComposite = composite.composites.get(i);
                Composite.setModified(childComposite, isModified, updateParents, updateChildren);
            }
        }
    }

    /**
     * add a body to the composite
     * 
     * @param composite composite
     * @param body      body
     * @return the composite with body added
     */
    static public Composite addBody(Composite composite, Body body) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", body));
        if (Objects.equals(body.parent, body)) {
            composite.bodies.add(body);
        } else {
            Common.warn("Composite.add: skipped adding a compound body part (you must add its parent instead)");
        }
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", body));
        return composite;
    }

    /**
     * add bodies to the composite
     * 
     * @param composite composite
     * @param bodies    bodies
     * @return the composite with body added
     */
    static public Composite addBody(Composite composite, Body[] bodies) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", bodies));
        for (Body body : bodies) {
            if (Objects.equals(body.parent, body)) {
                composite.bodies.add(body);
            } else {
                Common.warn("Composite.add: skipped adding a compound body part (you must add its parent instead)");
            }
        }
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", bodies));
        return composite;
    }

    /**
     * add a constraint to the given composite
     * 
     * @param composite  composite
     * @param constraint constraint
     * @return the composite with the constraint added
     */
    static public Composite addConstraint(Composite composite, Constraint constraint) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", constraint));
        composite.constraints.add(constraint);
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", constraint));
        return composite;
    }

    /**
     * add constraints to the given composite
     * 
     * @param composite   composite
     * @param constraints constraints
     * @return composite with the constraints added
     */
    static public Composite addConstraint(Composite composite, Constraint[] constraints) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", constraints));
        for (Constraint constraint : constraints) {
            composite.constraints.add(constraint);
        }
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", constraints));
        return composite;
    }

    /**
     * add a composite to the given composite
     * 
     * @param composite compositeA
     * @param toadd     composite to add to A
     * @return compositeA with objects added
     */
    static public Composite addComposite(Composite composite, Composite toadd) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", toadd));
        composite.composites.add(toadd);
        toadd.parent = composite;
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", toadd));
        return composite;
    }

    /**
     * add an array of composite to the given composite
     * 
     * @param composite compositeA
     * @param toadd     composites to add to A
     * @return compositeA with objects added
     */
    static public Composite addComposite(Composite composite, Composite[] toadd) {
        Events.trigger(composite, "beforeAdd", Common.opts("object", toadd));
        for (Composite c : toadd) {
            composite.composites.add(c);
            c.parent = composite;
        }
        Composite.setModified(composite, true, true, false);
        Events.trigger(composite, "afterAdd", Common.opts("object", toadd));
        return composite;
    }

    /**
     * add a mouseConstraint to the composite
     * 
     * @param composite       composite
     * @param mouseConstraint mouseConstraint
     * @return the composite with the mouseConstraint added
     */
    static public Composite addMouseConstraint(Composite composite, MouseConstraint mouseConstraint) {
        return Composite.addConstraint(composite, mouseConstraint.constraint);
    }

    /**
     * add mouseCOnstraints to the composite
     * 
     * @param composite        composite
     * @param mouseConstraints mouseConstraints
     * @return the composite with the mouseConstraints added
     */
    static public Composite addMouseConstraint(Composite composite, MouseConstraint[] mouseConstraints) {
        Constraint[] constraints = new Constraint[mouseConstraints.length];
        for (int i = 0; i < constraints.length; i++) {
            constraints[i] = mouseConstraints[i].constraint;
        }
        return Composite.addConstraint(composite, constraints);
    }

    /**
     * remove a body from the composite
     * 
     * @param composite composite
     * @param body      body
     * @return the composite with the body removed
     */
    static public Composite removeBody(Composite composite, Body body) {
        return Composite.removeBody(composite, body, false);
    }

    /**
     * remove a body from the composite
     * 
     * @param composite composite
     * @param body      body
     * @param deep      if true, searching children recursively
     * @return the composite with the body removed
     */
    static public Composite removeBody(Composite composite, Body body, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", body));
        int position = Common.indexOf(composite.bodies, body);
        if (position != -1) {
            Composite.removeBodyAt(composite, position);
        }

        if (deep) {
            for (int i = 0; i < composite.composites.size(); i++) {
                Composite.removeBody(composite.composites.get(i), body, true);
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", body));
        return composite;
    }

    /**
     * remove bodies from the composite
     * 
     * @param composite composite
     * @param bodies    bodies
     * @return the composite with the bodies removed
     */
    static public Composite removeBody(Composite composite, Body[] bodies) {
        return Composite.removeBody(composite, bodies, false);
    }

    /**
     * remove bodies from the composite
     * 
     * @param composite composite
     * @param bodies    bodies
     * @param deep      if true, searching children recursively
     * @return the composite with the bodies removed
     */
    static public Composite removeBody(Composite composite, Body[] bodies, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", bodies));
        for (Body body : bodies) {
            int position = Common.indexOf(composite.bodies, body);
            if (position != -1) {
                Composite.removeBodyAt(composite, position);
            }

            if (deep) {
                for (int i = 0; i < composite.composites.size(); i++) {
                    Composite.removeBody(composite.composites.get(i), body, true);
                }
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", bodies));
        return composite;
    }

    /**
     * remove a body from the given composite
     * 
     * @param composite composite
     * @param position  position
     * @return the original composite with the body removed
     */
    static private Composite removeBodyAt(Composite composite, int position) {
        composite.bodies.remove(position);
        Composite.setModified(composite, true, true, false);
        return composite;
    }

    /**
     * remove a constraint from the given composite
     * 
     * @param composite  composite
     * @param constraint constraint
     * @return the composite with the constraint removed
     */
    static public Composite removeConstraint(Composite composite, Constraint constraint) {
        return Composite.removeConstraint(composite, constraint, false);
    }

    /**
     * remove a constraint from the given composite
     * 
     * @param composite  composite
     * @param constraint constraint
     * @param deep       if true, searching children recursively
     * @return the composite with the constraint removed
     */
    static public Composite removeConstraint(Composite composite, Constraint constraint, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", constraint));
        int position = Common.indexOf(composite.constraints, constraint);
        if (position != -1) {
            Composite.removeConstraintAt(composite, position);
        }
        if (deep) {
            for (int i = 0; i < composite.constraints.size(); i++) {
                Composite.removeConstraint(composite.composites.get(i), constraint, true);
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", constraint));
        return composite;
    }

    /**
     * remove constraints from the given composite
     * 
     * @param composite   composite
     * @param constraints constraints
     * @return the composite with the constraint removed
     */
    static public Composite removeConstraint(Composite composite, Constraint[] constraints) {
        return Composite.removeConstraint(composite, constraints, false);
    }

    /**
     * remove constraints from the given composite
     * 
     * @param composite   composite
     * @param constraints constraints
     * @param deep        if true, searching children recursively
     * @return the composite with the constraint removed
     */
    static public Composite removeConstraint(Composite composite, Constraint[] constraints, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", constraints));
        for (Constraint constraint : constraints) {
            int position = Common.indexOf(composite.constraints, constraint);
            if (position != -1) {
                Composite.removeConstraintAt(composite, position);
            }
            if (deep) {
                for (int i = 0; i < composite.constraints.size(); i++) {
                    Composite.removeConstraint(composite.composites.get(i), constraint, true);
                }
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", constraints));
        return composite;
    }

    /**
     * remove a constraint from the given position
     * 
     * @param composite composite
     * @param position  position
     * @return the composite with the constraint removed
     */
    static private Composite removeConstraintAt(Composite composite, int position) {
        composite.constraints.remove(position);
        Composite.setModified(composite, true, true, false);
        return composite;
    }

    /**
     * remove a composite from the given composite
     * 
     * @param composite compositeA
     * @param toRemove  composite to remove
     * @return compositeA with the composite removed
     */
    static public Composite removeComposite(Composite composite, Composite toRemove) {
        return Composite.removeComposite(composite, toRemove, false);
    }

    /**
     * remove a composite from the given composite
     * 
     * @param composite compositeA
     * @param toRemove  composite to remove
     * @param deep      if true, searching children recursively
     * @return compositeA with the composite removed
     */
    static public Composite removeComposite(Composite composite, Composite toRemove, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", toRemove));
        int position = Common.indexOf(composite.composites, toRemove);
        if (position != -1) {
            Composite.removeCompositeAt(composite, position);
        }
        if (deep) {
            for (int i = 0; i < composite.composites.size(); i++) {
                Composite.removeComposite(composite.composites.get(i), toRemove, true);
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", toRemove));
        return composite;
    }

    /**
     * remove composites from the given composite
     * 
     * @param composite compositeA
     * @param toRemoves composites to remove
     * @return compositeA with the composites removed
     */
    static public Composite removeComposite(Composite composite, Composite[] toRemoves) {
        return Composite.removeComposite(composite, toRemoves, false);
    }

    /**
     * remove composites from the given composite
     * 
     * @param composite compositeA
     * @param toRemoves composites to remove
     * @param deep      if true, searching children recursively
     * @return compositeA with the composites removed
     */
    static public Composite removeComposite(Composite composite, Composite[] toRemoves, boolean deep) {
        Events.trigger(composite, "beforeRemove", Common.opts("object", toRemoves));
        for (Composite toRemove : toRemoves) {
            int position = Common.indexOf(composite.composites, toRemove);
            if (position != -1) {
                Composite.removeCompositeAt(composite, position);
            }
            if (deep) {
                for (int i = 0; i < composite.composites.size(); i++) {
                    Composite.removeComposite(composite.composites.get(i), toRemove, true);
                }
            }
        }
        Events.trigger(composite, "afterRemove", Common.opts("object", toRemoves));
        return composite;
    }

    /**
     * remove a composite from the given composite
     * 
     * @param composite compositeA
     * @param position  position
     * @return compositeA with the composite removed
     */
    static private Composite removeCompositeAt(Composite composite, int position) {
        composite.composites.remove(position);
        Composite.setModified(composite, true, true, false);
        return composite;
    }

    /**
     * remove a mouse constraint from the composite
     * 
     * @param composite       composite
     * @param mouseConstraint mouse constraint
     * @return the composite with the mouse constraint removed
     */
    static public Composite removeMouseConstraint(Composite composite, MouseConstraint mouseConstraint) {
        return Composite.removeMouseConstraint(composite, mouseConstraint, false);
    }

    /**
     * remove a mouse constraint from the composite
     * 
     * @param composite       composite
     * @param mouseConstraint mouse constraint
     * @param deep            if true, searching children recursively
     * @return the composite with the mouse constraint removed
     */
    static public Composite removeMouseConstraint(Composite composite, MouseConstraint mouseConstraint, boolean deep) {
        return Composite.removeConstraint(composite, mouseConstraint.constraint, deep);
    }

    /**
     * remove mouse constraints from the composite
     * 
     * @param composite        composite
     * @param mouseConstraints mouse constraints
     * @return the composite with the mouse constraints removed
     */
    static public Composite removeMouseConstraint(Composite composite, MouseConstraint[] mouseConstraints) {
        return Composite.removeMouseConstraint(composite, mouseConstraints);
    }

    /**
     * remove mouse constraints from the composite
     * 
     * @param composite        composite
     * @param mouseConstraints mouse constraints
     * @param deep             if true, searching children recursively
     * @return the composite with the mouse constraints removed
     */
    static public Composite removeMouseConstraint(Composite composite, MouseConstraint[] mouseConstraints,
            boolean deep) {
        Constraint[] cs = new Constraint[mouseConstraints.length];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = mouseConstraints[i].constraint;
        }
        return Composite.removeConstraint(composite, cs, deep);
    }

    /**
     * remove all bodies, constraints and composites from the given composite
     * 
     * @param composite  composite
     * @param keepStatic keepStatic
     */
    static public void clear(Composite composite, boolean keepStatic) {
        Composite.clear(composite, keepStatic, false);
    }

    /**
     * remove all bodies, constraints and composites from the given composite
     * 
     * @param composite  composite
     * @param keepStatic keepStatic
     * @param deep       if true, searching children recursively
     * @return the composite
     */
    static public Composite clear(Composite composite, boolean keepStatic, boolean deep) {
        if (deep) {
            for (int i = 0; i < composite.composites.size(); i++) {
                Composite.clear(composite.composites.get(i), keepStatic, true);
            }
        }

        if (keepStatic) {
            composite.bodies.removeIf(body -> !body.isStatic);
        } else {
            composite.bodies.clear();
        }

        composite.constraints.clear();
        composite.composites.clear();
        Composite.setModified(composite, true, true, false);
        return composite;
    }

    /**
     * return all bodies in the composite
     * 
     * @param composite
     * @return all the bodies
     */
    static public Body[] allBodies(Composite composite) {
        if (composite.chache != null && composite.chache.get("allBodies") != null) {
            return (Body[]) composite.chache.get("allBodies");
        }

        ArrayList<Body> bodies = new ArrayList<Body>();
        bodies.addAll(composite.bodies);
        for (int i = 0; i < composite.composites.size(); i++) {
            bodies.addAll(Arrays.asList(Composite.allBodies(composite.composites.get(i))));
        }

        if (composite.chache != null) {
            composite.chache.put("allBodies", bodies.toArray(Body[]::new));
        }

        return bodies.toArray(Body[]::new);
    }

    /**
     * return all constraints in the composite
     * 
     * @param composite composite
     * @return all the constraints
     */
    static public Constraint[] allConstraints(Composite composite) {
        if (composite.chache != null && composite.chache.get("allConstraints") != null) {
            return (Constraint[]) composite.chache.get("allConstraints");
        }

        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        constraints.addAll(composite.constraints);
        for (int i = 0; i < composite.composites.size(); i++) {
            constraints.addAll(Arrays.asList(Composite.allConstraints(composite.composites.get(i))));
        }

        if (composite.chache != null) {
            composite.chache.put("allConstraints", constraints.toArray(Constraint[]::new));
        }

        return constraints.toArray(Constraint[]::new);
    }

    /**
     * returns all composites in the given composite
     * 
     * @param composite composite
     * @return all the composites
     */
    static public Composite[] allComposites(Composite composite) {
        if (composite.chache != null && composite.chache.get("allComposites") != null) {
            return (Composite[]) composite.chache.get("allComposites");
        }

        ArrayList<Composite> composites = new ArrayList<Composite>();
        composites.addAll(composite.composites);

        for (int i = 0; i < composite.composites.size(); i++) {
            composites.addAll(Arrays.asList(Composite.allComposites(composite.composites.get(i))));
        }

        if (composite.chache != null) {
            composite.chache.put("allComposites", composites.toArray(Composite[]::new));
        }

        return composites.toArray(Composite[]::new);
    }

    /**
     * search the composite for a body match with the id
     * 
     * @param composite composite
     * @param id        id
     * @return the body if found
     */
    static public Body getBody(Composite composite, int id) {
        Body[] bodies = Composite.allBodies(composite);

        if (bodies == null || bodies.length == 0)
            return null;
        for (Body body : bodies) {
            if (body.id == id)
                return body;
        }

        return null;
    }

    /**
     * search the composite for a constraint match with the id
     * 
     * @param composite composite
     * @param id        id
     * @return the constraint if found
     */
    static public Constraint getConstraint(Composite composite, int id) {
        Constraint[] constraints = Composite.allConstraints(composite);

        if (constraints == null || constraints.length == 0)
            return null;

        for (Constraint constraint : constraints) {
            if (constraint.id == id)
                return constraint;
        }

        return null;
    }

    /**
     * search the composite for a composite match with the id
     * 
     * @param composite composite
     * @param id        id
     * @return the composite if found
     */
    static public Composite getComposite(Composite composite, int id) {
        Composite[] composites = Composite.allComposites(composite);

        if (composites == null || composites.length == 0)
            return null;

        for (Composite composite2 : composites) {
            if (composite2.id == id)
                return composite2;
        }

        return null;
    }

    /**
     * move the given body from A to B
     * 
     * @param compositeA compositeA
     * @param body       body
     * @param compositeB compositeB
     * @return compositeA
     */
    static public Composite moveBody(Composite compositeA, Body body, Composite compositeB) {
        Composite.removeBody(compositeA, body);
        Composite.addBody(compositeB, body);
        return compositeA;
    }

    /**
     * move the given bodies from A to B
     * 
     * @param compositeA compositeA
     * @param bodies     bodies
     * @param compositeB compositeB
     * @return compositeA
     */
    static public Composite moveBody(Composite compositeA, Body[] bodies, Composite compositeB) {
        for (Body body : bodies) {
            Composite.moveBody(compositeA, body, compositeB);
        }
        return compositeA;
    }

    /**
     * move the constraint from A to B
     * 
     * @param compositeA compositeA
     * @param constraint constraint
     * @param compositeB compositeB
     * @return compositeA
     */
    static public Composite moveConstraint(Composite compositeA, Constraint constraint, Composite compositeB) {
        Composite.removeConstraint(compositeA, constraint);
        Composite.addConstraint(compositeB, constraint);
        return compositeA;
    }

    /**
     * move the constraints from A to B
     * 
     * @param compositeA  compositeA
     * @param constraints constraints
     * @param compositeB  compositeB
     * @return compositeA
     */
    static public Composite moveConstraint(Composite compositeA, Constraint[] constraints, Composite compositeB) {
        for (Constraint constraint : constraints) {
            Composite.moveConstraint(compositeA, constraint, compositeB);
        }
        return compositeA;
    }

    /**
     * move the composite from A to B
     * 
     * @param compositeA compositeA
     * @param composite  composite
     * @param compositeB compositeB
     * @return compositeA
     */
    static public Composite moveComposite(Composite compositeA, Composite composite, Composite compositeB) {
        Composite.removeComposite(compositeA, composite);
        Composite.addComposite(compositeB, composite);
        return compositeA;
    }

    /**
     * move the composites from A to B
     * 
     * @param compositeA compositeA
     * @param composites composites
     * @param compositeB compositeB
     * @return compositeA
     */
    static public Composite moveComposite(Composite compositeA, Composite[] composites, Composite compositeB) {
        for (Composite composite : composites) {
            Composite.moveComposite(compositeA, composite, compositeB);
        }
        return compositeA;
    }

    /**
     * assign new ids for all objects in the composite
     * 
     * @param composite composite
     * @return composite
     */
    static public Composite rebase(Composite composite) {
        Body[] bodies = Composite.allBodies(composite);
        Constraint[] constraints = Composite.allConstraints(composite);
        Composite[] composites = Composite.allComposites(composite);

        for (int i = 0; i < bodies.length; i++) {
            bodies[i].id = Common.nextId();
        }

        for (int i = 0; i < constraints.length; i++) {
            constraints[i].id = Common.nextId();
        }

        for (int i = 0; i < composites.length; i++) {
            composites[i].id = Common.nextId();
        }

        return composite;
    }

    /**
     * translate all children in the composite
     * 
     * @param composite   composite
     * @param translation translation
     * @return composite
     */
    static public Composite translate(Composite composite, Vector translation) {
        return Composite.translate(composite, translation, true);
    }

    /**
     * translate all children in the composite
     * 
     * @param composite   composite
     * @param translation translation
     * @param recursive   recursive
     * @return composite
     */
    static public Composite translate(Composite composite, Vector translation, boolean recursive) {
        Body[] bodies = recursive ? Composite.allBodies(composite) : composite.bodies.toArray(Body[]::new);

        for (int i = 0; i < bodies.length; i++) {
            Body.translate(bodies[i], translation);
        }

        return composite;
    }

    /**
     * rotate children in the composite by a given angle about the given point
     * 
     * @param composite composite
     * @param rotation  rotation
     * @param point     point
     * @return the composite
     */
    static public Composite rotate(Composite composite, double rotation, Vector point) {
        return Composite.rotate(composite, rotation, point, true);
    }

    /**
     * rotate children in the composite by a given angle about the given point
     * 
     * @param composite composite
     * @param rotation  rotation
     * @param point     point
     * @param recursive recursive
     * @return the composite
     */
    static public Composite rotate(Composite composite, double rotation, Vector point, boolean recursive) {
        double cos = Math.cos(rotation),
                sin = Math.sin(rotation);
        Body[] bodies = recursive ? Composite.allBodies(composite) : composite.bodies.toArray(Body[]::new);

        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            double dx = body.position.x - point.x,
                    dy = body.position.y - point.y;
            Body.setPosition(body, new Vector(point.x + (dx * cos - dy * sin), point.y + (dx * sin + dy * cos)));

            Body.rotate(body, rotation);
        }

        return composite;
    }

    /**
     * scale all children in the composite, including updating physical properties
     * 
     * @param composite composite
     * @param scaleX    scaleX
     * @param scaleY    scaleY
     * @param point     point
     * @return the composite
     */
    static public Composite scale(Composite composite, double scaleX, double scaleY, Vector point) {
        return Composite.scale(composite, scaleX, scaleY, point, true);
    }

    /**
     * scale all children in the composite, including updating physical properties
     * 
     * @param composite composite
     * @param scaleX    scaleX
     * @param scaleY    scaleY
     * @param point     point
     * @param recursive recursive
     * @return the composite
     */
    static public Composite scale(Composite composite, double scaleX, double scaleY, Vector point, boolean recursive) {
        Body[] bodies = recursive ? Composite.allBodies(composite) : composite.bodies.toArray(Body[]::new);

        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            double dx = body.position.x - point.x,
                    dy = body.position.y - point.y;
            Body.setPosition(body, new Vector(point.x + dx * scaleX, point.y + dy * scaleY));
            Body.scale(body, scaleX, scaleY);
        }
        return composite;
    }

    /**
     * return the union of the bounds of all of the composite's bodies
     * 
     * @param composite composite
     * @return bounds
     */
    static public Bounds bounds(Composite composite) {
        Body[] bodies = Composite.allBodies(composite);
        ArrayList<Vector> vertices = new ArrayList<Vector>();

        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            vertices.add(body.bounds.min);
            vertices.add(body.bounds.max);
        }

        return Bounds.create(vertices.toArray(Vector[]::new));
    }

    static public Composite create() {
        return new Composite();
    }
}

/*
 *
 * Events Documentation
 *
 */

/**
 * Fired when a call to `Composite.add` is made, before objects have been added.
 *
 * @event beforeAdd
 * @param event        An event object
 * @param event.object The object(s) to be added (may be a single body,
 *                     constraint, composite or a mixed array of these)
 * @param event.source The source object of the event
 * @param event.name   The name of the event
 */

/**
 * Fired when a call to `Composite.add` is made, after objects have been added.
 *
 * @event afterAdd
 * @param event        An event object
 * @param event.object The object(s) that have been added (may be a single body,
 *                     constraint, composite or a mixed array of these)
 * @param event.source The source object of the event
 * @param event.name   The name of the event
 */

/**
 * Fired when a call to `Composite.remove` is made, before objects have been
 * removed.
 *
 * @event beforeRemove
 * @param event        An event object
 * @param event.object The object(s) to be removed (may be a single body,
 *                     constraint, composite or a mixed array of these)
 * @param event.source The source object of the event
 * @param event.name   The name of the event
 */

/**
 * Fired when a call to `Composite.remove` is made, after objects have been
 * removed.
 *
 * @event afterRemove
 * @param event        An event object
 * @param event.object The object(s) that have been removed (may be a single
 *                     body, constraint, composite or a mixed array of these)
 * @param event.source The source object of the event
 * @param event.name   The name of the event
 */
