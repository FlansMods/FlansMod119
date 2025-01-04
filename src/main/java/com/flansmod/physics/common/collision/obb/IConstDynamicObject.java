package com.flansmod.physics.common.collision.obb;

import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.collision.TransformedBBCollection;
import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IConstDynamicObject
{
    boolean isInvalid();

    @Nonnull AABB getLocalBounds();

    double getMass();
    double getInverseMass();
    @Nonnull Vec3 getMomentOfInertia();
    @Nonnull Vec3 getInertiaTensor();

    double getLinearDrag();
    default double getLinearDecayPerTick() { return 1.0d - getLinearDrag(); }
    double getAngularDrag();
    default double getAngularDecayPerTick() { return 1.0d - getAngularDrag(); }

    @Nonnull AABB getSweepTestAABB();
    @Nonnull Transform getCurrentLocation();
    @Nonnull TransformedBB getCurrentBB();
    @Nonnull TransformedBBCollection getCurrentColliders();
    @Nonnull AABB getCurrentWorldBounds();
    @Nonnull LinearVelocity getLinearVelocity();
    @Nonnull AngularVelocity getAngularVelocity();
}
