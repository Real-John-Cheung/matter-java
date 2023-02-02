package matter.collision;

import matter.geometry.Bounds;
import matter.geometry.Vector;
import matter.geometry.Vertices;
import matter.geometry.Vertices.Vertex;

import java.util.ArrayList;

import matter.body.Body;

/**
 * methods for resolving collision pairs
 * 
 * @author JohnC
 */
public class Resolver {
    private static double _restingThresh = 4;
    private static double _restingThreshTangent = 6;
    private static double _positionDampen = 0.9;
    private static double _positionWarming = 0.8;
    private static double _frictionNormalMultiplier = 5;

    /**
     * prepare pairs for position solving
     * 
     * @param pairs pairs
     */
    static public void preSolvePosition(Pair[] pairs) {
        Pair pair;
        int activeCount, pairsLength = pairs.length;

        for (int i = 0; i < pairsLength; i++) {
            pair = pairs[i];
            if (!pair.isActive)
                continue;
            activeCount = pair.activeContacts.size();
            pair.collision.parentA.totalContacts += activeCount;
            pair.collision.parentB.totalContacts += activeCount;
        }
    }

    /**
     * find a solution for pair position
     * 
     * @param pairs     pairs
     * @param timeScale timeScale
     */
    static public void solvePosition(Pair[] pairs, double timeScale) {
        Pair pair;
        Collision collision;
        Body bodyA, bodyB;
        double contactShare;
        double positionImpulse;
        Vector normal;
        double positionDampen = Resolver._positionDampen;
        int pairsLength = pairs.length;

        for (int i = 0; i < pairsLength; i++) {
            pair = pairs[i];
            if (!pair.isActive || pair.isSensor)
                continue;
            collision = pair.collision;
            bodyA = collision.parentA;
            bodyB = collision.parentB;
            normal = collision.normal;

            pair.separation = normal.x * (bodyB.positionImpulse.x + collision.penetration.x - bodyA.positionImpulse.x)
                    + normal.y * (bodyB.positionImpulse.y + collision.penetration.y - bodyA.positionImpulse.y);

        }

        for (int i = 0; i < pairsLength; i++) {
            pair = pairs[i];

            if (!pair.isActive || pair.isSensor)
                continue;
            collision = pair.collision;
            bodyA = collision.parentA;
            bodyB = collision.parentB;
            normal = collision.normal;
            positionImpulse = (pair.separation - pair.slop) * timeScale;

            if (bodyA.isStatic || bodyB.isStatic)
                positionImpulse *= 2;

            if (!(bodyA.isStatic || bodyA.isSleeping)) {
                contactShare = positionDampen / bodyA.totalContacts;
                bodyA.positionImpulse.x += normal.x * positionImpulse * contactShare;
                bodyA.positionImpulse.y += normal.y * positionImpulse * contactShare;
            }

            if (!(bodyB.isStatic || bodyB.isSleeping)) {
                contactShare = positionDampen / bodyB.totalContacts;
                bodyB.positionImpulse.x -= normal.x * positionImpulse * contactShare;
                bodyB.positionImpulse.y -= normal.y * positionImpulse * contactShare;
            }
        }
    }

    /**
     * apply position resolution
     * 
     * @param bodies bodies
     */
    static public void postSolvePosition(Body[] bodies) {
        double positionWarming = Resolver._positionWarming;
        int bodiesLength = bodies.length;
        // verticesTranslate = Vertices.translate
        // boundsUpdate = Bounds.update

        for (int i = 0; i < bodiesLength; i++) {
            Body body = bodies[i];
            Vector positionImpulse = body.positionImpulse;
            double positionImpulseX = positionImpulse.x,
                    positionImpulseY = positionImpulse.y;
            Vector velocity = body.velocity;

            body.totalContacts = 0;
            if (positionImpulseX != 0 || positionImpulseY != 0) {
                for (int j = 0; j < body.parts.size(); j++) {
                    Body part = body.parts.get(j);
                    Vertices.translate(part.vertices, positionImpulse);
                    Bounds.update(part.bounds, part.vertices, velocity);
                    part.position.x += positionImpulseX;
                    part.position.y += positionImpulseY;
                }

                body.positionPrev.x += positionImpulseX;
                body.positionPrev.y += positionImpulseY;

                if (positionImpulseX * velocity.x + positionImpulseY * velocity.y < 0) {
                    positionImpulse.x = 0;
                    positionImpulse.y = 0;
                } else {
                    positionImpulse.x *= positionWarming;
                    positionImpulse.y *= positionWarming;
                }
            }
        }
    }

    /**
     * prepare pairs for velocity solving
     * 
     * @param pairs pairs
     */
    static public void preSolveVelocity(Pair[] pairs) {
        int pairsLength = pairs.length;

        for (int i = 0; i < pairsLength; i++) {
            Pair pair = pairs[i];

            if (!pair.isActive || pair.isSensor)
                continue;
            ArrayList<Contact> contacts = pair.activeContacts;
            int contactLength = contacts.size();
            Collision collision = pair.collision;
            Body bodyA = collision.parentA, bodyB = collision.parentB;
            Vector normal = collision.normal, tangent = collision.tangent;

            for (int j = 0; j < contactLength; j++) {
                Contact contact = contacts.get(j);
                Vertex contactVertex = contact.vertex;
                double normalImpulse = contact.normalImpulse,
                        tangentImpulse = contact.tangentImpulse;
                if (normalImpulse != 0 || tangentImpulse != 0) {
                    double impulseX = normal.x * normalImpulse + tangent.x * tangentImpulse,
                            impulseY = normal.y * normalImpulse + tangent.y * tangentImpulse;
                    if (!(bodyA.isStatic || bodyA.isSleeping)) {
                        bodyA.positionPrev.x += impulseX * bodyA.inverseMass;
                        bodyA.positionPrev.y += impulseY * bodyA.inverseMass;
                        bodyA.anglePrev += bodyA.inverseInertia * ((contactVertex.x - bodyA.position.x) * impulseY
                                - (contactVertex.y - bodyA.position.y) * impulseX);
                    }

                    if (!(bodyB.isStatic || bodyB.isSleeping)) {
                        bodyB.positionPrev.x -= impulseX * bodyB.inverseMass;
                        bodyB.positionPrev.y -= impulseY * bodyB.inverseMass;
                        bodyB.anglePrev -= bodyB.inverseInertia * ((contactVertex.x - bodyB.position.x) * impulseY
                                - (contactVertex.y - bodyB.position.y) * impulseX);
                    }
                }
            }
        }
    }

    /**
     * find a solution for pair velocities
     * 
     * @param pairs     pairs
     * @param timeScale timeScale
     */
    static public void solveVelocity(Pair[] pairs, double timeScale) {
        double timeScaleSquared = timeScale * timeScale, restingThresh = Resolver._restingThresh * timeScaleSquared,
                frictionNormalMultiplier = Resolver._frictionNormalMultiplier,
                restingThreshTangent = Resolver._restingThreshTangent * timeScaleSquared,
                NumberMaxValue = Double.MAX_VALUE;
        int pairsLength = pairs.length;
        double tangentImpulse, maxFriction;

        for (int i = 0; i < pairsLength; i++) {
            Pair pair = pairs[i];
            if (!pair.isActive || pair.isSensor)
                continue;
            Collision collision = pair.collision;
            Body bodyA = collision.parentA, bodyB = collision.parentB;
            Vector bodyAVelocity = bodyA.velocity, bodyBVelocity = bodyB.velocity;
            double normalX = collision.normal.x, normalY = collision.normal.y,
                    tangentX = collision.tangent.x,
                    tangentY = collision.tangent.y;
            ArrayList<Contact> contacts = pair.contacts;
            int contactsLength = contacts.size();
            double contactShare = 1 / contactsLength, inverseMassTotal = bodyA.inverseMass + bodyB.inverseMass,
                    friction = pair.friction * pair.frictionStatic * frictionNormalMultiplier * timeScaleSquared;
            bodyAVelocity.x = bodyA.position.x - bodyA.positionPrev.x;
            bodyAVelocity.y = bodyA.position.y - bodyA.positionPrev.y;
            bodyBVelocity.x = bodyB.position.x - bodyB.positionPrev.x;
            bodyBVelocity.y = bodyB.position.y - bodyB.positionPrev.y;
            bodyA.angularVelocity = bodyA.angle - bodyA.anglePrev;
            bodyB.angularVelocity = bodyB.angle - bodyB.anglePrev;
            for (int j = 0; j < contactsLength; j++) {
                Contact contact = contacts.get(j);
                if (contact == null)
                    continue;
                Vertex contactVertex = contact.vertex;
                double offsetAX = contactVertex.x - bodyA.position.x,
                        offsetAY = contactVertex.y - bodyA.position.y,
                        offsetBX = contactVertex.x - bodyB.position.x,
                        offsetBY = contactVertex.y - bodyB.position.y,
                        velocityPointAX = bodyAVelocity.x - offsetAY * bodyA.angularVelocity,
                        velocityPointAY = bodyAVelocity.y + offsetAX * bodyA.angularVelocity,
                        velocityPointBX = bodyBVelocity.x - offsetBY * bodyB.angularVelocity,
                        velocityPointBY = bodyBVelocity.y + offsetBX * bodyB.angularVelocity,
                        relativeVelocityX = velocityPointAX - velocityPointBX,
                        relativeVelocityY = velocityPointAY - velocityPointBY,
                        normalVelocity = normalX * relativeVelocityX + normalY * relativeVelocityY,
                        tangentVelocity = tangentX * relativeVelocityX + tangentY * relativeVelocityY,
                        normalOverlap = pair.separation + normalVelocity,
                        normalForce = Math.min(normalOverlap, 1);
                normalForce = normalOverlap < 0 ? 0 : normalForce;
                double frictionLimit = normalForce * friction;
                if (tangentVelocity > frictionLimit || -tangentVelocity > frictionLimit) {
                    maxFriction = tangentVelocity > 0 ? tangentVelocity : -tangentVelocity;
                    tangentImpulse = pair.friction * (tangentVelocity > 0 ? 1 : -1) * timeScaleSquared;
                    
                    if (tangentImpulse < -maxFriction) {
                        tangentImpulse = -maxFriction;
                    } else if (tangentImpulse > maxFriction) {
                        tangentImpulse = maxFriction;
                    }
                } else {
                    tangentImpulse = tangentVelocity;
                    maxFriction = NumberMaxValue;
                }

                double oAcN = offsetAX * normalY - offsetAY * normalX,
                        oBcN = offsetBX * normalY - offsetBY * normalX,
                        share = contactShare / (inverseMassTotal + bodyA.inverseInertia * oAcN * oAcN
                                + bodyB.inverseInertia * oBcN * oBcN);
                double normalImpulse = (1 + pair.restitution) * normalVelocity * share;
                tangentImpulse *= share;
                if (normalVelocity * normalVelocity > restingThresh && normalVelocity < 0) {
                    contact.normalImpulse = 0;
                } else {
                    double contactNormalImpulse = contact.normalImpulse;
                    contact.normalImpulse += normalImpulse;
                    contact.normalImpulse = Math.min(contact.normalImpulse, 0);
                    normalImpulse = contact.normalImpulse - contactNormalImpulse;
                }
                if (tangentVelocity * tangentVelocity > restingThreshTangent) {
                    contact.tangentImpulse = 0;
                } else {
                    double contactTangentImpulse = contact.tangentImpulse;
                    contact.tangentImpulse += tangentImpulse;
                    if (contact.tangentImpulse < -maxFriction)
                        contact.tangentImpulse = -maxFriction;
                    if (contact.tangentImpulse > maxFriction)
                        contact.tangentImpulse = maxFriction;
                    tangentImpulse = contact.tangentImpulse - contactTangentImpulse;
                }
                
                double impulseX = normalX * normalImpulse + tangentX * tangentImpulse,
                        impulseY = normalY * normalImpulse + tangentY * tangentImpulse;
                if (!(bodyA.isStatic || bodyA.isSleeping)) {
                    bodyA.positionPrev.x += impulseX * bodyA.inverseMass;
                    bodyA.positionPrev.y += impulseY * bodyA.inverseMass;
                    bodyA.anglePrev += (offsetAX * impulseY - offsetAY * impulseX) * bodyA.inverseInertia;
                }

                if (!(bodyB.isStatic || bodyB.isSleeping)) {
                    bodyB.positionPrev.x -= impulseX * bodyB.inverseMass;
                    bodyB.positionPrev.y -= impulseY * bodyB.inverseMass;
                    bodyB.anglePrev -= (offsetBX * impulseY - offsetBY * impulseX) * bodyB.inverseInertia;
                }
            }
        }
    }
}