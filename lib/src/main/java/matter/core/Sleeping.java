package matter.core;

import matter.body.Body;
import matter.collision.Collision;
import matter.collision.Pair;

public class Sleeping {
    
    static double _motionWakeThreshold = 0.18;
    static double _motionSleepThreshold = 0.08;
    static double _minBias = 0.9;

    /**
     * put bodies to sleep or wakes them up depending on their motion
     * 
     * @param bodies bodies
     * @param timeScale timeScale
     */
    static public void update(Body[] bodies, double timeScale) {
        double timeFactor = timeScale * timeScale * timeScale;
        for (int i = 0; i < bodies.length; i++) {
            Body body = bodies[i];
            double motion = body.speed * body.speed + body.angularSpeed * body.angularSpeed;

            if (body.force.x != 0 || body.force.y != 0) {
                Sleeping.set(body, false);
                continue;
            }

            double minMotion = Math.min(body.motion, motion),
                    maxMotion = Math.max(body.motion, motion);
            body.motion = Sleeping._minBias * minMotion + (1 - Sleeping._minBias) * maxMotion;

            if (body.sleepThreshold > 0 && body.motion < Sleeping._motionSleepThreshold * timeFactor) {
                body.sleepCounter += 1;

                if (body.sleepCounter >= body.sleepThreshold)
                    Sleeping.set(body, true);
            } else if (body.sleepCounter > 0) {
                body.sleepCounter -= 1;
            }
        }
    }
    
    /**
     * wake the sleeping bodies involved in colliding
     * 
     * @param pairs pairs
     * @param timeScale timeScale
     */
    static public void afterCollisions(Pair[] pairs, double timeScale) {
        double timeFactor = timeScale * timeScale * timeScale;

        for (int i = 0; i < pairs.length; i++) {
            Pair pair = pairs[i];

            if (!pair.isActive)
                continue;
            Collision collision = pair.collision;
            Body bodyA = collision.bodyA.parent,
                    bodyB = collision.bodyB.parent;
            if ((bodyA.isSleeping && bodyB.isSleeping) || bodyA.isStatic || bodyB.isStatic)
                continue;
            if (bodyA.isSleeping || bodyB.isSleeping) {
                Body sleepingBody = (bodyA.isSleeping && !bodyA.isStatic) ? bodyA : bodyB,
                        movingBody = sleepingBody == bodyA ? bodyB : bodyA;
                if (!sleepingBody.isStatic && movingBody.motion > Sleeping._motionWakeThreshold * timeFactor) {
                    Sleeping.set(sleepingBody, false);
                }
            }
        }
    }
    
    /**
     * set a body as sleeping or awake
     * 
     * @param body body
     * @param isSleeping isSleeping
     */
    public static void set(Body body, boolean isSleeping) {
        boolean wasSleeping = body.isSleeping;
        if (isSleeping) {
            body.isSleeping = true;
            body.sleepCounter = body.sleepThreshold;

            body.positionImpulse.x = 0;
            body.positionImpulse.y = 0;

            body.positionPrev.x = body.position.x;
            body.positionPrev.y = body.position.y;

            body.anglePrev = body.angle;
            body.speed = 0;
            body.angularSpeed = 0;
            body.motion = 0;

            if (!wasSleeping) {
                Events.trigger(body, "sleepStart");
            }
        } else {
            body.isSleeping = false;
            body.sleepCounter = 0;

            if (wasSleeping) {
                Events.trigger(body, "sleepEnd");
            }
        }
    }
}
