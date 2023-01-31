package matter.collision;

import matter.geometry.Vertices.Vertex;

/**
 * methods for creating and manipulating collision contacts
 * @author JohnC
 */
public class Contact {

    public Vertex vertex;
    public double normalImpulse;
    public double tangentImpulse;

    /**
     * create a new contact
     * 
     * @param vertex vertex
     */
    public Contact(Vertex vertex) {
        this.vertex = vertex;
        this.normalImpulse = 0;
        this.tangentImpulse = 0;
    }

    /**
     * create a new contact
     * 
     * @param vertex vertex
     * @return a new contace
     */
    static public Contact create(Vertex vertex){
        return new Contact(vertex);
    }
}