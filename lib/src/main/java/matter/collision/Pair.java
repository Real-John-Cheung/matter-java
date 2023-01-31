package matter.collision;

import java.util.ArrayList;

import matter.body.Body;
import matter.geometry.Vertices.Vertex;

/**
 * methods for creating and manipulating collision pairs
 * 
 * @author JohnC
 */
public class Pair {
    public String id;
    public Body bodyA;
    public Body bodyB;
    public Collision collision;
    public ArrayList<Contact> contacts;
    public ArrayList<Contact> activeContacts;
    public double separation;
    public boolean isActive;
    public boolean confirmedActive;
    public boolean isSensor;
    public double timeCreated;
    public double timeUpdated;
    public double inverseMass;
    public double friction;
    public double frictionStatic;
    public double restitution;
    public double slop;

    /**
     * create a pair
     * 
     * @param collision collision
     * @param timestamp timestamp
     */
    public Pair(Collision collision, double timestamp) {
        Body bodyA = collision.bodyA;
        Body bodyB = collision.bodyB;

        this.id = Pair.id(bodyA, bodyB);
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.collision = collision;
        this.contacts = new ArrayList<Contact>();
        this.activeContacts = new ArrayList<Contact>();
        this.separation = 0;
        this.isActive = true;
        this.confirmedActive = true;
        this.isSensor = bodyA.isSensor || bodyB.isSensor;
        this.timeCreated = timestamp;
        this.timeUpdated = timestamp;
        this.inverseMass = 0;
        this.friction = 0;
        this.frictionStatic = 0;
        this.restitution = 0;
        this.slop = 0;

        Pair.update(this, collision, timestamp);
    }

    /**
     * create a pair
     * 
     * @param collision collision
     * @param timestamp timestamp
     * @return a new pair
     */
    static public Pair create(Collision collision, double timestamp) {
        return new Pair(collision, timestamp);
    }

    /**
     * update a pair
     * 
     * @param pair pair
     * @param collision collision
     * @param timestamp timestamp
     */
    static public void update(Pair pair, Collision collision, double timestamp) {
        ArrayList<Contact> contacts = pair.contacts;
        ArrayList<Vertex> supports = collision.supports;
        ArrayList<Contact> activeContacts = pair.activeContacts;
        Body parentA = collision.parentA;
        Body parentB = collision.parentB;
        int parentAVerticesLength = parentA.vertices.length;

        pair.isActive = true;
        pair.timeUpdated = timestamp;
        pair.collision = collision;
        pair.separation = collision.depth;
        pair.inverseMass = parentA.inverseMass + parentB.inverseMass;
        pair.friction = parentA.friction < parentB.friction ? parentA.friction : parentB.friction;
        pair.frictionStatic = parentA.frictionStatic > parentB.frictionStatic ? parentA.frictionStatic
                : parentB.frictionStatic;
        pair.restitution = parentA.restitution > parentB.restitution ? parentA.restitution : parentB.restitution;
        pair.slop = parentA.slop > parentB.slop ? parentA.slop : parentB.slop;
        collision.pair = pair;
        activeContacts.clear();

        for (int i = 0; i < supports.size(); i++) {
            Vertex support = supports.get(i);
            int contactId = support.body == parentA ? support.index : parentAVerticesLength + support.index;
            Contact contact = contacts.get(contactId);
            if (contact != null) {
                activeContacts.add(contact);
            } else {
                Contact tem = Contact.create(support);
                contacts.set(contactId, tem);
                activeContacts.add(tem);
            }
        }
    }
    
    /**
     * set a pair as active or inactive
     * 
     * @param pair pair
     * @param isActive isActive
     * @param timestamp timestamp
     */
    static public void setActive(Pair pair, boolean isActive, double timestamp) {
        if (isActive) {
            pair.isActive = true;
            pair.timeUpdated = timestamp;
        } else {
            pair.isActive = false;
            pair.activeContacts.clear();
        }
    }

    /**
     * get the id for the given pair
     * 
     * @param bodyA bodyA
     * @param bodyB bodyB
     * @return Unique pairId
     */
    static public String id(Body bodyA, Body bodyB) {
        if (bodyA.id < bodyB.id) {
            return "A" + bodyA.id + "B" + bodyB.id;
        } else {
            return "A" + bodyB.id + "B" + bodyA.id;
        }
    }
}