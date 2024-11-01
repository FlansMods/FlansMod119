package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record Impulse(@Nonnull Vec3 vector) implements IForce
{
    @Nonnull
    public LinearVelocity applyTo(@Nonnull LinearVelocity v, double inverseMass)
    {
        return new LinearVelocity(v.Velocity().add(vector.scale(inverseMass)));
    }
    @Nonnull
    public AngularVelocity applyTo(@Nonnull AngularVelocity v, @Nonnull Vec3 offset, @Nonnull Vec3 inertiaTensor)
    {
        Vec3 rotationAxis = vector.cross(offset);



        return new AngularVelocity(v.A().add(vector.scale(inertiaTensor)));
    }


    @Override
    public boolean isApproxZero() { return Maths.approx(vector, Vec3.ZERO); }
    @Nonnull @Override
    public Impulse inverse() { return new Impulse(vector.scale(-1d)); }
    @Override
    public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
    @Nonnull @Override
    public LinearForce getLinearComponent(@Nonnull Transform actingOn) { return LinearForce.Zero; }
    @Override
    public boolean hasAngularComponent(@Nonnull Transform actingOn) { return false; }
    @Nonnull @Override
    public Torque getTorqueComponent(@Nonnull Transform actingOn) { return Torque.Zero; }
    @Override
    public String toString() { return "Impulse ["+vector+"]"; }
    @Override @Nonnull
    public Component toFancyString() { return Component.translatable("flansphysicsmod.impulse", vector.x, vector.y, vector.z); }
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof Impulse otherImpulse)
            return otherImpulse.vector.equals(vector);
        return false;
    }
}
