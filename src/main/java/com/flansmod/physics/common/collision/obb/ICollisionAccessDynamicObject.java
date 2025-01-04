package com.flansmod.physics.common.collision.obb;

import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.collision.TransformedBBCollection;
import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public interface ICollisionAccessDynamicObject  extends IConstDynamicObject
{
	@Nonnull Transform getPendingLocation();
	@Nonnull TransformedBB getPendingBB();
	@Nonnull TransformedBBCollection getPendingColliders();
	@Nonnull AABB getPendingWorldBounds();
	void extrapolatePendingFrame(double parametricTicks);
	default void extrapolatePendingFrame() { extrapolatePendingFrame(1.0d); }
	boolean isPendingFrameEvaluated();
	void setPendingLinearVelocity(@Nonnull LinearVelocity linearVelocity);
	void setPendingAngularVelocity(@Nonnull AngularVelocity angularVelocity);
	void commitPendingFrame();
	void discardPendingFrame();
	boolean pendingFrameIsTeleport();


}
