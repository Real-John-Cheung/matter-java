package matter.geometry;

/**
 * Vector object {x: 0, y: 0}
 * 
 * @class Vector
 * @author JohnC
 */
public class Vector {
    public double x;
    public double y;

    /**
     * init a vector object with x = 0 and y = 0
     */
    public Vector() {
        this(0, 0);
    }

    /**
     * init a vector object with specified x and y
     * 
     * @param x the x value of the vector
     * @param y the y value of the vector
     */
    public Vector(double x, double y) {

    }

    /**
     * create a vector object with x = 0 and y = 0
     * 
     * @return the vector
     */
    static public Vector create() {
        return new Vector();
    }

    /**
     * create a vector object with specified x and y
     * 
     * @param x the x value of the vector
     * @param y the y value of the vector
     * @return the vector
     */
    static public Vector create(double x, double y) {
        return new Vector(x, y);
    }

    /**
     * return a new vector with x and y copied from the given vector
     * 
     * @param vector the vector to be cloned
     * @return the vector copy
     */
    static public Vector clone(Vector vector) {
        return new Vector(vector.x, vector.y);
    }

    /**
     * return the length of a vector
     * 
     * @param vector vector
     * @return the magnitude of the vector
     */
    static public double magnitude(Vector vector) {
        return Math.sqrt((vector.x * vector.x) + (vector.y * vector.y));
    }

    /**
     * return the length of this vector
     * 
     * @return the magnitude of the vector
     */
    public double magnitude() {
        return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    /**
     * return the squared length of a vector
     * 
     * @param vector vector
     * @return the squared length of the vector
     */
    static public double magnitudeSquared(Vector vector) {
        return (vector.x * vector.x) + (vector.y * vector.y);
    }

    /**
     * return the squared length of this vector
     * 
     * @return the squared length of the vector
     */
    public double magnitudeSquared() {
        return (this.x * this.x) + (this.y * this.y);
    }

    /**
     * rotate a vector about (0, 0) by specified angle
     * 
     * @param vector the vector to be rotated
     * @param angle angle
     * @return a new rotated vector 
     */
    static public Vector rotate(Vector vector, double angle) {
        return Vector.rotate(vector, angle, null);
    }
    
    /**
     * rotate a vector about (0, 0) by specified angle
     * 
     * @param vector the vector to be rotated
     * @param angle angle
     * @param output the vector to be output to
     * @return the output vector
     */
    static public Vector rotate(Vector vector, double angle, Vector output) {
        if (output == null)
            output = new Vector();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.x * cos - vector.y * sin;
        output.y = vector.x * sin + vector.y * cos;
        output.x = x;
        return output;
    }

    /**
     * rotate the vector about (0, 0) by specified angle
     * 
     * @param angle angle
     */
    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = this.x * cos - this.y * sin;
        double y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
    }
    
    /**
     * rotate a vector about a specified point by specified angle
     * 
     * @param vector the vector to be rotate
     * @param angle angle 
     * @param point point
     * @return a new rotated vector
     */
    static public Vector rotateAbout(Vector vector, double angle, Vector point) {
        return Vector.rotateAbout(vector, angle, point, null);
    }
    
    /**
     * rotate a vector about a specified point by specified angle
     * 
     * @param vector the vector to be rotate
     * @param angle angle 
     * @param point point
     * @param output the vector to be output to
     * @return the output vector
     */
    static public Vector rotateAbout(Vector vector, double angle, Vector point, Vector output) {
        if (output == null)
            output = new Vector();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = point.x + ((vector.x - point.x) * cos - (vector.y - point.y) * sin);
        output.y = point.y + ((vector.x - point.x) * sin + (vector.y - point.y) * cos);
        output.x = x;
        return output;
    }

    /**
     * rotate the vector about a specified point by specified angle
     * 
     * @param angle angloe
     * @param point point
     */
    public void rotateAbout(double angle, Vector point) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = point.x + ((this.x - point.x) * cos - (this.y - point.y) * sin);
        double y = point.y + ((this.x - point.x) * sin + (this.y - point.y) * cos);
        this.x = x;
        this.y = y;
    }

    /**
     * normalise a vector
     * 
     * @param vector vector
     * @return a new normalised vector
     */
    static public Vector normalise(Vector vector) {
        double magnitude = vector.magnitude();
        if (magnitude == 0)
            return new Vector();
        return new Vector(vector.x / magnitude, vector.y / magnitude);
    }

    /**
     * return the dot product of two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @return the dot product of two vectors
     */
    static public double dot(Vector vectorA, Vector vectorB) {
        return (vectorA.x * vectorB.x) + (vectorA.y * vectorB.y);
    }

    /**
     * return the dot product of this vector and another vector
     * 
     * @param vector another vector
     * @return he dot product of two vectors
     */
    public double dot(Vector vector) {
        return (this.x * vector.x) + (this.y * vector.y);
    }

    /**
     * return the cross product of two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @return the cross product of two vectors
     */
    static public double cross(Vector vectorA, Vector vectorB) {
        return (vectorA.x * vectorB.y) - (vectorA.y * vectorB.x);
    }

    /**
     * return the cross product of this vector and another vector
     * 
     * @param vector another vector
     * @return he cross product of two vectors
     */
    public double cross(Vector vector) {
        return (this.x * vector.y) + (this.y * vector.x);
    }

    /**
     * return the cross product of three vectors
     * 
     * @param vectorA
     * @param vectorB
     * @param vectorC
     * @return
     */
    static public double cross3(Vector vectorA, Vector vectorB, Vector vectorC) {
        return (vectorB.x - vectorA.x) * (vectorC.y - vectorA.y) - (vectorB.y - vectorA.y) * (vectorC.x - vectorA.x);
    }

    /**
     * add the two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @return a new vector as the result
     */
    static public Vector add(Vector vectorA, Vector vectorB) {
        return Vector.add(vectorA, vectorB, null);
    }

    /**
     * add the two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @param output the vector to output to
     * @return the output vector
     */
    static public Vector add(Vector vectorA, Vector vectorB, Vector output) {
        if (output == null)
            output = new Vector();
        output.x = vectorA.x + vectorB.x;
        output.y = vectorA.y + vectorB.y;
        return output;
    }
    
    /**
     * add a vector to this vector
     * 
     * @param vector vector to be added
     */
    public void add(Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
    }

    /**
     * subtract the two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @return a new vector as the result
     */
    static public Vector sub(Vector vectorA, Vector vectorB) {
        return Vector.sub(vectorA, vectorB, null);
    }

    /**
     * subtract the two vectors
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @param output the vector to output to
     * @return the output vector
     */
    static public Vector sub(Vector vectorA, Vector vectorB, Vector output) {
        if (output == null)
            output = new Vector();
        output.x = vectorA.x - vectorB.x;
        output.y = vectorA.y - vectorB.y;
        return output;
    }
    
    /**
     * subtract a vector to this vector
     * 
     * @param vector vector to be subtracted
     */
    public void sub(Vector vector) {
        this.x -= vector.x;
        this.y -= vector.y;
    }

    /**
     * multiply a vector with a scalar
     * 
     * @param vector vector
     * @param scalar scalar
     * @return a new vector as the result
     */
    static public Vector mult(Vector vector, double scalar) {
        return new Vector(vector.x * scalar, vector.y * scalar);
    }

    /**
     * multiply this vector with a scalar
     * 
     * @param scalar scalar
     */
    public void mult(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    /**
     * divide a vector by a scalar
     * 
     * @param vector vector
     * @param scalar scalar
     * @return
     */
    static public Vector div(Vector vector, double scalar) {
        return new Vector(vector.x / scalar, vector.y / scalar);
    }

    /**
     * divide this vector by a scalar
     * 
     * @param scalar scalar
     */
    public void div(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
    }

    /**
     * return the perpendicular vector
     * 
     * @param vector vector
     * @return the perpendicular vector
     */
    static public Vector perp(Vector vector) {
        return Vector.perp(vector, false);
    }

    /**
     * return the perpendicular vector
     * 
     * @param vector vector
     * @param negate if true, return the perpendicular in the opposite direction
     * @return the perpendicular vector
     */
    static public Vector perp(Vector vector, boolean negate) {
        double nag = negate ? -1 : 1;
        return new Vector(nag * -vector.y, nag * vector.x);
    }
    
    /**
     * negates both components of a vector
     * 
     * @param vector vector
     * @return the negated vector
     */
    static public Vector neg(Vector vector) {
        return new Vector(-vector.x, -vector.y);
    }

    /**
     * return the angle between the vector: vectorB - vectorA and the x-axis.
     * 
     * @param vectorA vectorA
     * @param vectorB vectorB
     * @return the angle
     */
    static public double angle(Vector vectorA, Vector vectorB) {
        return Math.atan2(vectorB.y - vectorA.y, vectorB.x - vectorA.x);
    }

    /**
     * return a copy of this vector
     * 
     * @return a copy of this vector
     */
    public Vector copy() {
        return new Vector(this.x, this.y);
    }

    /**
     * return a copy of a vector
     * 
     * @return a copy of a vector
     */
    static public Vector copy(Vector vector) {
        return new Vector(vector.x, vector.y);
    }

    // /**
    //  * temporary vector pool
    //  */
    // private Vector[] _temp = new Vector[]{
    //     new Vector(), new Vector(), new Vector(), new Vector(),new Vector(), new Vector()
    // };
}