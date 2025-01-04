package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

// An "instant" force, i.e. the time is hard-coded to be 1 tick
public record Impulse(@Nonnull Vec3 momentumDelta) implements IForce
{
    @Nonnull
    public LinearVelocity applyTo(double inverseMass)
    {
        return new LinearVelocity(momentumDelta.scale(inverseMass));
    }
    @Nonnull
    public LinearVelocity applyTo(@Nonnull LinearVelocity v, double inverseMass)
    {
        return new LinearVelocity(v.Velocity().add(momentumDelta.scale(inverseMass)));
    }
    @Nonnull
    public AngularVelocity applyAtOffset(@Nonnull Vec3 offset, @Nonnull Vec3 inertiaTensor)
    {
        // Both the axis and the angle (magnitude)
        Vec3 angleAxis = momentumDelta.cross(offset);

        angleAxis = angleAxis.multiply(inertiaTensor);
        if(Maths.approx(angleAxis, Vec3.ZERO))
            return AngularVelocity.Zero;

        return AngularVelocity.radiansPerTick(angleAxis);
    }
    @Nonnull
    public AngularVelocity applyTo(@Nonnull Vec3 center, @Nonnull Vec3 inertiaTensor, @Nonnull Vec3 applyAtPoint)
    {
        return applyAtOffset(center.subtract(applyAtPoint), inertiaTensor);
    }
    @Nonnull
    public AngularVelocity applyAtOffset(@Nonnull AngularVelocity v, @Nonnull Vec3 offset, @Nonnull Vec3 inertiaTensor)
    {
        // Both the axis and the angle (magnitude)
        Vec3 angleAxis = momentumDelta.cross(offset);

        angleAxis = angleAxis.multiply(inertiaTensor);
        if(Maths.approx(angleAxis, Vec3.ZERO))
            return v;

        AngularVelocity deltaAngularV = AngularVelocity.radiansPerTick(angleAxis);
        return v.compose(deltaAngularV);
    }
    @Nonnull
    public AngularVelocity applyTo(@Nonnull AngularVelocity v, @Nonnull Vec3 center, @Nonnull Vec3 inertiaTensor, @Nonnull Vec3 applyAtPoint)
    {
        return applyAtOffset(v, center.subtract(applyAtPoint), inertiaTensor);
    }

    @Nonnull
    public static Vec3 vectorTripleProduct(@Nonnull Vec3 a, @Nonnull Vec3 b, @Nonnull Vec3 c)
    {
        //return a.cross(b).cross(c);
        // Lagrange's Formula (a x b) x c = -(c.b)a + (c.a)b
        return a.scale(-c.dot(b)).add(b.scale(c.dot(a)));
    }
    @Nonnull
    public static Impulse calculateAtOffsets(@Nonnull CompoundVelocity vA, double inverseMassA, @Nonnull Vec3 inertiaTensorA, @Nonnull Vec3 collisionRelA,
                                             @Nonnull CompoundVelocity vB, double inverseMassB, @Nonnull Vec3 inertiaTensorB, @Nonnull Vec3 collisionRelB,
                                             @Nonnull Vec3 normal,
                                             double coefficientOfRestitution)
    {
        LinearVelocity linearVAtA = vA.linearAtPoint(collisionRelA);
        LinearVelocity linearVAtB = vB.linearAtPoint(collisionRelB);
        LinearVelocity deltaV = linearVAtB.subtract(linearVAtA);

        double collisionSpeedLinear = -(1 + coefficientOfRestitution) * deltaV.Velocity().dot(normal);

        Vec3 tangentA = vectorTripleProduct(collisionRelA, normal, collisionRelA);
        Vec3 thetaA = inertiaTensorA.multiply(tangentA);
        Vec3 tangentB = vectorTripleProduct(collisionRelB, normal, collisionRelB);
        Vec3 thetaB = inertiaTensorB.multiply(tangentB);

        double impulseMagnitude = collisionSpeedLinear / (inverseMassA + inverseMassB + thetaA.add(thetaB).dot(normal));
        return new Impulse(normal.scale(impulseMagnitude));
    }
    @Nonnull
    public static Impulse calculateAtOffset(@Nonnull CompoundVelocity vA, double inverseMassA, @Nonnull Vec3 inertiaTensorA, @Nonnull Vec3 collisionRelA,
                                            @Nonnull Vec3 normal,
                                            double coefficientOfRestitution)
    {
        LinearVelocity linearVAtA = vA.linearAtPoint(collisionRelA);
        LinearVelocity deltaV = linearVAtA;//.subtract(linearVAtA);

        double collisionSpeedLinear = -(1 + coefficientOfRestitution) * deltaV.Velocity().dot(normal);

        Vec3 tangentA = vectorTripleProduct(collisionRelA, normal, collisionRelA);
        Vec3 thetaA = inertiaTensorA.multiply(tangentA);
        //Vec3 tangentB = vectorTripleProduct(collisionRelB, normal, collisionRelB);
        //Vec3 thetaB = inertiaTensorB.multiply(tangentB);

        double impulseMagnitude = collisionSpeedLinear / (inverseMassA + thetaA.dot(normal));
        impulseMagnitude = Maths.max(impulseMagnitude, 0d);

        return new Impulse(normal.scale(impulseMagnitude));
    }
    @Nonnull
    public static Impulse calculateLinear(@Nonnull LinearVelocity vA, double inverseMassA,
                                         @Nonnull LinearVelocity vB, double inverseMassB,
                                         @Nonnull Vec3 normal,
                                         double coefficientOfRestitution)
    {
        LinearVelocity deltaV = vB.subtract(vA);
        double collisionSpeed = -(1 + coefficientOfRestitution) * deltaV.Velocity().dot(normal);
        return new Impulse(normal.scale(collisionSpeed / (inverseMassA + inverseMassB)));
    }

    @Nonnull
    public static Impulse calculateAtPoint(@Nonnull CompoundVelocity vA, @Nonnull Vec3 centerA, double invMassA, @Nonnull Vec3 inertiaTensorA,
                                           @Nonnull CompoundVelocity vB, @Nonnull Vec3 centerB, double invMassB, @Nonnull Vec3 inertiaTensorB,
                                           @Nonnull Vec3 collisionPoint, @Nonnull Vec3 collisionNormal,
                                           double coefficientOfRestitution)
    {
        return calculateAtOffsets(vA, invMassA, inertiaTensorA, collisionPoint.subtract(centerA),
                                  vB, invMassB, inertiaTensorB, collisionPoint.subtract(centerB),
                                  collisionNormal, coefficientOfRestitution);
    }

    @Nonnull
    public static Impulse calculateAtPoint(@Nonnull CompoundVelocity vA, @Nonnull Vec3 centerA, double invMassA, @Nonnull Vec3 inertiaTensorA,
                                           @Nonnull Vec3 collisionPoint, @Nonnull Vec3 collisionNormal,
                                           double coefficientOfRestitution)
    {
        return calculateAtOffset(vA, invMassA, inertiaTensorA, collisionPoint.subtract(centerA),
                collisionNormal, coefficientOfRestitution);
    }



    @Nonnull
    public static Impulse linearImpulse(@Nonnull LinearVelocity from, @Nonnull LinearVelocity to, double mass)
    {
        // How much do we need to do in one tick to stop this mass
        // J = m * deltaV, where deltaV = (to-from)/dt
        return new Impulse(from.subtract(to).scale(mass).applyOneTick());
    }



    @Nonnull
    public Impulse project(@Nonnull Vec3 ontoAxis)
    {
        return new Impulse(ontoAxis.scale(ontoAxis.dot(momentumDelta)));
    }






    @Override
    public boolean isApproxZero() { return Maths.approx(momentumDelta, Vec3.ZERO); }
    @Nonnull @Override
    public Impulse inverse() { return new Impulse(momentumDelta.scale(-1d)); }
    @Override
    public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
    @Nonnull @Override
    public LinearForce getLinearComponent(@Nonnull Transform actingOn) { return LinearForce.Zero; }
    @Override
    public boolean hasAngularComponent(@Nonnull Transform actingOn) { return false; }
    @Nonnull @Override
    public Torque getTorqueComponent(@Nonnull Transform actingOn) { return Torque.Zero; }
    @Override
    public String toString() { return "Impulse ["+momentumDelta+"] kgm/tick"; }
    @Override @Nonnull
    public Component toFancyString() { return Component.translatable("flansphysicsmod.impulse", momentumDelta.x, momentumDelta.y, momentumDelta.z); }
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof Impulse otherImpulse)
            return otherImpulse.momentumDelta.equals(momentumDelta);
        return false;
    }
}
