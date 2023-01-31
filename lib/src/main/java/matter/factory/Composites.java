package matter.factory;

import matter.body.Composite;
import matter.constraint.Constraint;
import matter.geometry.Vector;
import matter.Common;
import matter.body.Body;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * factory methods for creating composite bodies
 * 
 * @author JohnC
 */
public class Composites {

    /**
     * create a new composite containing bodies created in the callback in a grid
     * arrangement
     * 
     * @param xx        xx
     * @param yy        yy
     * @param columns   columns
     * @param rows      rows
     * @param columnGap columnGap
     * @param rowGap    rowGap
     * @param callback  a implementation of the Callback interface
     * @return a new composite
     */
    static public Composite stack(double xx, double yy, int columns, int rows, double columnGap, double rowGap,
            Callback callback) {
        Composite stack = Composite.create(Common.opts("label", "Stack"));
        double x = xx;
        double y = yy;
        Body lastBody = null;
        int i = 0;

        for (int row = 0; row < rows; row++) {
            double maxHeight = 0;
            for (int column = 0; column < columns; column++) {
                Body body = callback.callback(x, y, column, row, lastBody, i);

                if (body != null) {
                    double bodyHeight = body.bounds.max.y - body.bounds.min.y,
                            bodyWidth = body.bounds.max.x - body.bounds.min.x;
                    if (bodyHeight > maxHeight)
                        maxHeight = bodyHeight;
                    Body.translate(body, new Vector(bodyWidth * 0.5, bodyHeight * 0.5));

                    x = body.bounds.max.x + columnGap;

                    Composite.addBody(stack, body);

                    lastBody = body;
                    i += 1;
                } else {
                    x += columnGap;
                }
            }
            y += maxHeight + rowGap;
            x = xx;
        }
        return stack;
    }

    /**
     * chain all bodies in the given composite together using constraints
     * 
     * @param composite composite
     * @param xOffsetA  xOffsetA
     * @param yOffsetA  yOffsetA
     * @param xOffsetB  xOffsetB
     * @param yOffsetB  yOffsetB
     * @returna a new composite containing objects chained together with constraints
     */
    static public Composite chain(Composite composite, double xOffsetA, double yOffsetA, double xOffsetB,
            double yOffsetB) {
        return Composites.chain(composite, xOffsetA, yOffsetA, xOffsetB, yOffsetB, new HashMap<String, Object>());
    }

    /**
     * chain all bodies in the given composite together using constraints
     * 
     * @param composite composite
     * @param xOffsetA  xOffsetA
     * @param yOffsetA  yOffsetA
     * @param xOffsetB  xOffsetB
     * @param yOffsetB  yOffsetB
     * @param options   options
     * @returna a new composite containing objects chained together with constraints
     */
    static public Composite chain(Composite composite, double xOffsetA, double yOffsetA, double xOffsetB,
            double yOffsetB, HashMap<String, Object> options) {
        ArrayList<Body> bodies = composite.bodies;

        for (int i = 1; i < bodies.size(); i++) {
            Body bodyA = bodies.get(i - 1),
                    bodyB = bodies.get(i);
            double bodyAHeight = bodyA.bounds.max.y - bodyA.bounds.min.y,
                    bodyAWidth = bodyA.bounds.max.x - bodyA.bounds.min.x,
                    bodyBHeight = bodyB.bounds.max.y - bodyB.bounds.min.y,
                    bodyBWidth = bodyB.bounds.max.x - bodyB.bounds.min.x;
            options.put("bodyA", bodyA);
            options.put("bodyB", bodyB);
            options.put("pointA", new Vector(bodyAWidth * xOffsetA, bodyAHeight * yOffsetA));
            options.put("pointB", new Vector(bodyBWidth * xOffsetB, bodyBHeight * yOffsetB));
            Constraint constraint = Constraint.create(options);
            Composite.addConstraint(composite, constraint);
        }

        composite.label += " Chain";

        return composite;
    }

    /**
     * connect bodies in the composite with constraints in a grid pattern
     * 
     * @param composite composite
     * @param columns columns
     * @param rows rows
     * @return composite containing objects meshed together with constraints
     */
    static public Composite mesh(Composite composite, int columns, int rows) {
        return Composites.mesh(composite, columns, rows, false);
    }

    /**
     * connect bodies in the composite with constraints in a grid pattern
     * 
     * @param composite composite
     * @param columns columns
     * @param rows rows
     * @param crossBrace crossBrace
     * @return composite containing objects meshed together with constraints
     */
    static public Composite mesh(Composite composite, int columns, int rows, boolean crossBrace) {
        return Composites.mesh(composite, columns, rows, crossBrace, new HashMap<>());
    }

    /**
     * bodies in the composite with constraints in a grid pattern
     *  
     * @param composite composite
     * @param columns columns
     * @param rows rows
     * @param crossBrace crossBrace
     * @param options options
     * @return composite containing objects meshed together with constraints
     */
    static public Composite mesh(Composite composite, int columns, int rows, boolean crossBrace,
            HashMap<String, Object> options) {
        ArrayList<Body> bodies = composite.bodies;
        int row, col;
        Body bodyA, bodyB, bodyC;

        for (row = 0; row < rows; row++) {
            for (col = 1; col < columns; col++) {
                bodyA = bodies.get((col - 1) + (row * columns));
                bodyB = bodies.get(col + (row * columns));
                options.put("bodyA", bodyA);
                options.put("bodyB", bodyB);
                Constraint tem1 = Constraint.create(options);
                Composite.addConstraint(composite, tem1);
            }

            if (row > 0) {
                for (col = 0; col < columns; col++) {
                    bodyA = bodies.get(col + ((row - 1) * columns));
                    bodyB = bodies.get(col + (row * columns));
                    options.put("bodyA", bodyA);
                    options.put("bodyB", bodyB);
                    Constraint tem1 = Constraint.create(options);
                    Composite.addConstraint(composite, tem1);

                    if (crossBrace && col > 0) {
                        bodyC = bodies.get((col - 1) + ((row - 1) * columns));
                        options.put("bodyA", bodyC);
                        options.put("bodyB", bodyB);
                        Constraint tem2 = Constraint.create(options);
                        Composite.addConstraint(composite, tem2);
                    }

                    if (crossBrace && col < columns - 1) {
                        bodyC = bodies.get((col + 1) + ((row - 1) * columns));
                        options.put("bodyA", bodyC);
                        options.put("bodyB", bodyB);
                        Constraint tem2 = Constraint.create(options);
                        Composite.addConstraint(composite, tem2);
                    }
                }
            }
        }

        composite.label += " Mesh";
        return composite;
    }
    
    /**
     * create a new composite containing bodies created in the callback in a pyramid arrangement
     * 
     * @param xx xx
     * @param yy yy
     * @param columns columns
     * @param rows rows
     * @param columnGap columnGap
     * @param rowGap rowGap
     * @param callback a implementation of the Callback interface
     * @return a new composite containing objects created in the callback
     */
    static public Composite pyramid(double xx, double yy, int columns, int rows, double columnGap, double rowGap, Callback callback) {
        Callback cb = (x, y, column, row, lastBody, i) -> {
            int actualRows = Math.min(rows, (int) Math.ceil(columns / 2));
            double lastBodyWidth = lastBody != null ? lastBody.bounds.max.x - lastBody.bounds.min.x : 0;
            if (row > actualRows)
                return null;
            row = actualRows - row;
            int start = row,
                    end = columns - 1 - row;
            if (column < start || column > end)
                return null;
            if (i == 1) {
                Body.translate(lastBody, new Vector((column + (columns % 2 == 1 ? 1 : -1)) * lastBodyWidth, 0));
            }
            double xOffset = lastBody != null ? column * lastBodyWidth : 0;
            return callback.callback(xx + xOffset + column * columnGap, y, column, row, lastBody, i);
        };
        return Composites.stack(xx, yy, columns, rows, columnGap, rowGap, cb);
    }

    /**
     * Interface for implementing callbacks
     */
    static public interface Callback {
        /**
         * single abstract function
         * 
         * @param x        x
         * @param y        y
         * @param column   column
         * @param row      row
         * @param lastBody lastBody
         * @param i        i
         * @return a body
         */
        Body callback(double x, double y, int column, int row, Body lastBody, int i);
    }
}